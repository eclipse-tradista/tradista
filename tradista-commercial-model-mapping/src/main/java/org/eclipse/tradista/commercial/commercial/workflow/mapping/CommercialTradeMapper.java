package org.eclipse.tradista.commercial.commercial.workflow.mapping;

import finance.tradista.flow.model.Workflow;

public final class CommercialTradeMapper {

	private CommercialTradeMapper() {
	}

	public static CommercialTrade map(org.eclipse.tradista.commercial.commercial.model.CommercialTrade trade,
			Workflow<CommercialTrade> wkf) {
		CommercialTrade result = new CommercialTrade(wkf);
		result.setTrade(trade);
		return result;
	}

}
