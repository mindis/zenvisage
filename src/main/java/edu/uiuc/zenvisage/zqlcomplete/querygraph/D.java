/**
 * @author Tarique
 */
package edu.uiuc.zenvisage.zqlcomplete.querygraph;

import java.util.List;

import edu.uiuc.zenvisage.data.remotedb.VisualComponentList;

/**
 * @author tarique
 *
 */
public interface D {
	public  AxisVariableScores  execute(VisualComponentList f1, VisualComponentList f2, List<String> axisVariables);
}