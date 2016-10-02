package edu.uiuc.zenvisage.data.remotedb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import edu.uiuc.zenvisage.zqlcomplete.executor.Constraints;
import edu.uiuc.zenvisage.zqlcomplete.executor.VizColumn;
import edu.uiuc.zenvisage.zqlcomplete.executor.XColumn;
import edu.uiuc.zenvisage.zqlcomplete.executor.YColumn;
import edu.uiuc.zenvisage.zqlcomplete.executor.ZColumn;
import edu.uiuc.zenvisage.zqlcomplete.executor.ZQLRow;


/**
 * PostgreSQL database connection portal for my local machine
 * need to change to in general
 *
 */
public class SQLQueryExecutor {
	
	/**
	 * Settings specific to local PSQL database, need to change this!!!!
	 */
	private String database = "postgres";
	private String host = "jdbc:postgresql://localhost:5432/"+database;
	private String username = "postgres";
	private String password = "zenvisage";
	Connection c = null;
	private VisualComponentList visualComponentList;
	
	// Initialize connection
	public SQLQueryExecutor() {
	      try {
		         Class.forName("org.postgresql.Driver");
		         c = DriverManager
		            .getConnection(host, username, password);
		      } catch (Exception e) {
		    	 System.out.println("Connection Failed! Check output console");
		         e.printStackTrace();
		         System.err.println(e.getClass().getName()+": "+e.getMessage());
		         System.exit(0);
		      }
	      System.out.println("Opened database successfully");
	}
	
	// Query database and return result
	public ResultSet query(String sQLQuery) throws SQLException {
	      Statement stmt = c.createStatement();
	      ResultSet ret = stmt.executeQuery(sQLQuery);
	      stmt.close();
	      return ret;
	}
	
	public int createTable(String sQLQuery) throws SQLException {
	      Statement stmt = c.createStatement();
	      int ret = stmt.executeUpdate(sQLQuery);
	      stmt.close();
	      return ret;
	}
	
	public void dropTable(String tableName) throws SQLException {
		Statement stmt = c.createStatement();
		String sql = "DROP TABLE " + tableName;
	    stmt.executeUpdate(sql);
	    System.out.println("Table " + tableName + " deleted in given database...");
	    stmt.close();
	}	
	
	public void ZQLQuery(String Z, String X, String Y, String table, String whereCondition) throws SQLException{
		Statement st = c.createStatement();
		String sql = null;	
		
		if (whereCondition == null) {
			sql = "SELECT " + Z + "," + X + "," + "avg(" + Y + ")"
					+ " FROM " + table
					+ " GROUP BY " + Z + ", "+ X
					+ " ORDER BY " + Z + ", "+ X;
		} else {
			sql = "SELECT " + Z + "," + X
			+ " FROM " + table
			+ " WHERE " + whereCondition
			+ " GROUP BY " + Z + ", "+ X
			+ " ORDER BY " + Z + ", "+ X;
		}
		
		ResultSet rs = st.executeQuery(sql);
		System.out.println("Running ZQL Query ...");
		
		this.visualComponentList = new VisualComponentList();
		this.visualComponentList.setVisualComponentList(new ArrayList<VisualComponent>());
		
		WrapperType zValue = null;
		ArrayList <WrapperType> xList = null;
		ArrayList <WrapperType> yList = null;
		VisualComponent tempVisualComponent = null;
		
		while (rs.next())
		{
			
			WrapperType tempZValue = new WrapperType(rs.getString(1));

			if(tempZValue.equals(zValue)){
				xList.add(new WrapperType(rs.getString(2)));
				yList.add(new WrapperType(rs.getString(3)));
			} else {
				zValue = tempZValue;
				xList = new ArrayList<WrapperType>();
				yList = new ArrayList<WrapperType>();
				xList.add(new WrapperType(rs.getString(2)));
				yList.add(new WrapperType(rs.getString(3)));
				tempVisualComponent = new VisualComponent(zValue, new Points(xList, yList));
				this.visualComponentList.addVisualComponent(tempVisualComponent);
			}

		}

		/* Testing below */
        System.out.println("Printing Visual Groups:\n" + this.visualComponentList.toString());
		rs.close();
		st.close();
	}
	
	public void ZQLQueryEnhanced(ZQLRow zqlRow, String databaseName) throws SQLException{
		Statement st = c.createStatement();
		String sql = null;	
		
		
		//zqlRow.getConstraint() has replaced the whereCondiditon
		if (zqlRow.getConstraint() == null || zqlRow.getConstraint().size() == 0) {
			sql = "SELECT " + zqlRow.getZ().getColumn() + "," + zqlRow.getX().getVariable() + " ," + zqlRow.getViz().getVariable() + "(" + zqlRow.getY().getVariable() + ")" //zqlRow.getViz() should replace the avg() function
					+ " FROM " + databaseName
					+ " GROUP BY " + zqlRow.getZ().getColumn() + ", "+ zqlRow.getX().getVariable()
					+ " ORDER BY " + zqlRow.getZ().getColumn() + ", "+ zqlRow.getX().getVariable();
		} else {
			sql = "SELECT " + zqlRow.getZ().getColumn()+ "," + zqlRow.getX().getVariable()
			+ " FROM " + databaseName
			+ " WHERE " + zqlRow.getConstraint() //zqlRow.getConstraint() has replaced the whereCondiditon
			+ " GROUP BY " + zqlRow.getZ().getColumn() + ", "+ zqlRow.getX().getVariable()
			+ " ORDER BY " + zqlRow.getZ().getColumn() + ", "+ zqlRow.getX().getVariable();
		}
		System.out.println("Running ZQL Query :"+sql);
		ResultSet rs = st.executeQuery(sql);
		
		
		this.visualComponentList = new VisualComponentList();
		this.visualComponentList.setVisualComponentList(new ArrayList<VisualComponent>());
		
		WrapperType zValue = null;
		ArrayList <WrapperType> xList = null;
		ArrayList <WrapperType> yList = null;
		VisualComponent tempVisualComponent = null;
		
		String zType = null, xType = null, yType = null;
		while (rs.next())
		{
			if(zType == null) zType = getMetaType(zqlRow.getZ().getColumn(), databaseName);
			if(xType == null) xType = getMetaType(zqlRow.getX().getVariable(), databaseName);
			if(yType == null) yType = getMetaType(zqlRow.getY().getVariable(), databaseName);
			
			WrapperType tempZValue = new WrapperType(rs.getString(1), zType);

			if(tempZValue.equals(zValue)){
				xList.add(new WrapperType(rs.getString(2), xType));
				yList.add(new WrapperType(rs.getString(3), yType));
			} else {
				zValue = tempZValue;
				xList = new ArrayList<WrapperType>();
				yList = new ArrayList<WrapperType>();
				xList.add(new WrapperType(rs.getString(2), xType));
				yList.add(new WrapperType(rs.getString(3), yType));
				tempVisualComponent = new VisualComponent(zValue, new Points(xList, yList));
				this.visualComponentList.addVisualComponent(tempVisualComponent);
			}

		}

		/* Testing below */
        //System.out.println("Printing Visual Groups:\n" + this.visualComponentList.toString());
		rs.close();
		st.close();
	}
	
	public String getMetaType(String variable, String table) throws SQLException{
		Statement st = c.createStatement();
		String sql = null;	
		sql = "SELECT " + "type"
			+ " FROM " + "zenvisage_metatable"
			+ " WHERE " + "tablename = '" + table
			+ "' AND attribute = '" + variable + "'";
		System.out.println(sql);
		ResultSet rs = st.executeQuery(sql);
		while (rs.next())
		{
			return rs.getString(1);
		}
		return null;
	}
	
	public String getMetaFileLocation(String database) throws SQLException {
		Statement st = c.createStatement();
		String sql = null;	
		sql = "SELECT " + "metafilelocation"
			+ " FROM " + "zenvisage_metafilelocation"
			+ " WHERE " + "database = '" + database + "'";
		System.out.println(sql);
		ResultSet rs = st.executeQuery(sql);
		while (rs.next())
		{
			System.out.println( "/data/" + rs.getString(1));
			return "/data/" + rs.getString(1);
		}
		return null;
	}
	
	
	
	public static void main(String[] args){
		SQLQueryExecutor sqlQueryExecutor= new SQLQueryExecutor();
		try {
			sqlQueryExecutor.dropTable("COMPANY");
			sqlQueryExecutor.createTable("CREATE TABLE COMPANY " +
	                "(ID INT PRIMARY KEY     NOT NULL," +
	                " NAME           TEXT    NOT NULL, " +
	                " AGE            INT     NOT NULL, " +
	                " ADDRESS        CHAR(50), " +
	                " SALARY         REAL)");
			//sqlQueryExecutor.query("SELECT * FROM COMPANY");
			
			//sqlQueryExecutor.ZQLQuery("State", "Quarter", "SoldPrice", "real_estate", null);
			List<Constraints> constraints = new ArrayList<Constraints>();
			ZQLRow zqlRow = new ZQLRow(new XColumn("Quarter"), new YColumn("SoldPrice"), new ZColumn("State"), constraints, new VizColumn("avg"));
			sqlQueryExecutor.ZQLQueryEnhanced(zqlRow, "real_estate");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public VisualComponentList getVisualComponentList() {
		return visualComponentList;
	}

	public void setVisualComponentList(VisualComponentList visualComponentList) {
		this.visualComponentList = visualComponentList;
	}

}