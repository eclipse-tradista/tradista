package org.eclipse.tradista.commercial.commercial.workflow.mapping;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.workflow.model.mapping.StatusMapper;

import finance.tradista.flow.model.Status;
import finance.tradista.flow.model.Workflow;
import finance.tradista.flow.model.WorkflowObject;

public class CommercialTrade implements WorkflowObject {

	private org.eclipse.tradista.commercial.commercial.model.CommercialTrade trade;

	private Workflow<CommercialTrade> workflow;

	public CommercialTrade(Workflow<CommercialTrade> workflow) {
		this.workflow = workflow;
	}

	public org.eclipse.tradista.commercial.commercial.model.CommercialTrade getTrade() {
		return trade;
	}

	public void setTrade(org.eclipse.tradista.commercial.commercial.model.CommercialTrade trade) {
		this.trade = trade;
	}

	@Override
	public Status<CommercialTrade> getStatus() {
		return StatusMapper.map(trade.getStatus(), workflow);
	}

	@Override
	public void setStatus(Status status) {
		trade.setStatus(StatusMapper.map(status));
	}

	@Override
	public String getWorkflow() {
		return workflow != null ? workflow.getName() : null;
	}

	@Override
	public CommercialTrade clone() {
		CommercialTrade cloned = new CommercialTrade(workflow);
		cloned.setTrade(
				(org.eclipse.tradista.commercial.commercial.model.CommercialTrade) TradistaModelUtil.clone(trade));
		return cloned;
	}

}
