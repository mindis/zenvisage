package edu.uiuc.zenvisage.zqlcomplete.querygraph;

/**
 * @author Edward Xue
 * The visual component node to be executed
 */
public class VisualComponentNode extends QueryNode{

	private VisualComponentQuery vc;
	// private vc output
	//TODO: build separate result node
	// call QueryGraphResults
	// VisualComponentResultNode
		// should not have the visual componentquery
	// ProcessResultNode
		//should have top 5 visualcomponents from list (for now)
	
	public VisualComponentNode(VisualComponentQuery vc, boolean resultNode) {
		this.vc = vc;
	}
	
	@Override
	public void execute() {
		this.state = State.RUNNING;
	}

	public VisualComponentQuery getVc() {
		return vc;
	}

	public void setVc(VisualComponentQuery vc) {
		this.vc = vc;
	}
}