package org.eclipse.tradista.commercial.commercial.service;

import org.eclipse.tradista.commercial.commercial.model.CommercialTrade;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;

public class CommercialTradeBusinessDelegate {

	private CommercialTradeService commercialTradeService;

	private CommercialTradeValidator validator;

	public CommercialTradeBusinessDelegate() {
		commercialTradeService = TradistaServiceLocator.getInstance().getCommercialTradeService();
		validator = new CommercialTradeValidator();
	}

	public long saveCommercialTrade(CommercialTrade trade, String action) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> commercialTradeService.saveCommercialTrade(trade, action));
	}

	public CommercialTrade getCommercialTradeById(long id) {
		return SecurityUtil.run(() -> commercialTradeService.getCommercialTradeById(id));
	}

}
