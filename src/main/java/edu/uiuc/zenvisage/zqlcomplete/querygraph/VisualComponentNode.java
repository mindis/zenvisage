package edu.uiuc.zenvisage.zqlcomplete.querygraph;

import java.sql.SQLException;
import java.util.List;

import edu.uiuc.zenvisage.data.remotedb.SQLQueryExecutor;
import edu.uiuc.zenvisage.zqlcomplete.executor.XColumn;
import edu.uiuc.zenvisage.zqlcomplete.executor.YColumn;
import edu.uiuc.zenvisage.zqlcomplete.executor.ZColumn;
import edu.uiuc.zenvisage.zqlcomplete.executor.ZQLRow;

/**
 * @author Edward Xue
 * The visual component node to be executed
 */
public class VisualComponentNode extends QueryNode{

	private VisualComponentQuery vc;
	private SQLQueryExecutor sqlQueryExecutor;
	// private vc output
	//TODO: build separate result node
	// call QueryGraphResults
	// VisualComponentResultNode
		// should not have the visual componentquery
	// ProcessResultNode
		//should have top 5 visualcomponents from list (for now)
	
	public VisualComponentNode(VisualComponentQuery vc) {
		this.vc = vc;
	}
	
	public VisualComponentNode(VisualComponentQuery vc, LookUpTable table, SQLQueryExecutor sqlQueryExecutor) {
		super(table);
		this.vc = vc;
		this.sqlQueryExecutor = sqlQueryExecutor;
	}
	
	@Override
	public void execute() {
		if (isBlocked()) {
			this.state = State.BLOCKED;
			return;
		}
		this.state = State.RUNNING;
		
		LookUpTable lookuptable = this.getLookUpTable();
		// update lookup table with axisvariables
		XColumn x = this.getVc().getX();
		YColumn y = this.getVc().getY();
		ZColumn z = this.getVc().getZ();
		
		// e.g., x1 <- 'year'
		if (!x.getVariable().equals("") && !x.getValues().isEmpty()) {
			AxisVariable axisVar = new AxisVariable(x.getVariable(), x.getValues());
			lookuptable.put(x.getVariable(), axisVar);
		}
		if (!y.getVariable().equals("") && !y.getValues().isEmpty()) {
			AxisVariable axisVar = new AxisVariable(y.getVariable(), y.getValues());
			lookuptable.put(y.getVariable(), axisVar);
		}
		// For z, use type variable = z.getColumn!
		if (!z.getVariable().equals("") && !z.getValues().isEmpty()) {
			AxisVariable axisVar = new AxisVariable(z.getColumn(), z.getValues());
			lookuptable.put(z.getVariable(), axisVar);
		}
		// call SQL backend
		ZQLRow row = buildRowFromNode();
		try {
			sqlQueryExecutor.ZQLQueryEnhanced(row, "realestate");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		this.state = State.FINISHED;
		//update the look table with name variable, e.g, f1)
		String name = this.getVc().getName().getName();
		
		// CHECK THIS OUTPUT
		this.getLookUpTable().put(name, sqlQueryExecutor.getVisualComponentList());
		System.out.println("vcList for node "+ name);
		System.out.println(sqlQueryExecutor.getVisualComponentList());
		System.out.println("hi");
	}

	public VisualComponentQuery getVc() {
		return vc;
	}
	
	public void setVc(VisualComponentQuery vc) {
		this.vc = vc;
	}
	
	@Override
	/**
	 * Four cases to deal with: 
	 * Column has variable and values. Can use as is.
	 * Column has variable, but no values. Need to fill in values from lookup
	 * Column hs no variable name, but values. Can use as is
	 * Column has no variable name, and no value. Send as is (columns may be optional)
	 */
	public ZQLRow buildRowFromNode() {
		
		XColumn x = vc.getX();	
		// x1 (variable, no values)
		if(!x.getVariable().equals("") && x.getValues().isEmpty()) {
			// The lookup table for x should have value = AxisVariable
			List<String> values = ((AxisVariable) lookuptable.get(x.getVariable())).getValues();
			x.setValues(values);
		}
		// Stripping out '' from first value
		String var = x.getValues().get(0);
		var = var.replace("'", "");
		x.getValues().set(0, var);
		
		// Some debuf info
		System.out.println("x information:");
		System.out.println(x.getVariable());
		System.out.println(x.getValues());
		System.out.println(x.getValues().get(0));
		
		YColumn y = vc.getY();		
		// y1 (variable, no values)
		if(!y.getVariable().equals("") && y.getValues().isEmpty()) {
			List<String> values = ((AxisVariable) lookuptable.get(y.getVariable())).getValues();
			y.setValues(values);
		}		
		// Stripping out '' from first value
		var = y.getValues().get(0);
		var = var.replace("'", "");
		y.getValues().set(0, var);
		
		ZColumn z = vc.getZ();		
		// z1 (variable, no values)
		AxisVariable zAxisVariable = (AxisVariable) lookuptable.get(z.getVariable());
		if(!z.getVariable().equals("") && z.getValues().isEmpty()) {
			List<String> values = zAxisVariable.getValues();
			z.setValues(values);
		}
		// if z is missing column information, grab from axisVariable type! (Special case!)
		if(!z.getVariable().equals("") && z.getColumn().isEmpty()) {
			z.setColumn(zAxisVariable.getType());
		}
		// update the z column to make sure it strips extra '' out (so will be state, not 'state')
		String str = z.getColumn();
		str = str.replace("'", "");
		z.setColumn(str);

		System.out.println("z information:");
		System.out.println(z.getVariable());
		System.out.println(z.getValues());
		System.out.println(z.getColumn());
		vc.getViz().setVariable("AVG");
		ZQLRow result = new ZQLRow(x, y, z, vc.getConstraints(), vc.getViz());
		// null processe and sketchPoints (for now)
		return result;
	}
	
	public void updateAxisVaribles(){
		//TODO
	}
	
}
