package org.eclipse.tradista.commercial.commercial.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.eclipse.tradista.commercial.commercial.model.CommercialTrade;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;

public class CommercialPricerBusinessDelegate {

	private CommercialPricerService commercialPricerService;

	public CommercialPricerBusinessDelegate() {
		commercialPricerService = TradistaServiceLocator.getInstance().getCommercialPricerService();
	}

	public BigDecimal npvDiscountedCashFlow(PricingParameter params, CommercialTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		return SecurityUtil
				.runEx(() -> commercialPricerService.npvDiscountedCashFlow(params, trade, currency, pricingDate));
	}

	public List<CashFlow> generateCashFlows(CommercialTrade trade, PricingParameter pp, LocalDate pricingDate)
			throws TradistaBusinessException {
		if (trade == null) {
			throw new TradistaBusinessException("The trade cannot be null.");
		}
		if (pricingDate == null) {
			throw new TradistaBusinessException("The pricing date cannot be null.");
		}
		return SecurityUtil.runEx(() -> commercialPricerService.generateCashFlows(pp, trade, pricingDate));
	}

}
