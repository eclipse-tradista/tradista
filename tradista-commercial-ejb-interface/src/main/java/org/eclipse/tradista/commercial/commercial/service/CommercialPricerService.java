package org.eclipse.tradista.commercial.commercial.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.eclipse.tradista.commercial.commercial.model.CommercialTrade;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;

import jakarta.ejb.Remote;

@Remote
public interface CommercialPricerService {

	BigDecimal npvDiscountedCashFlow(PricingParameter params, CommercialTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException;

	List<CashFlow> generateCashFlows(PricingParameter params, CommercialTrade trade, LocalDate pricingDate)
			throws TradistaBusinessException;

}
