package org.eclipse.tradista.commercial.commercial.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.eclipse.tradista.commercial.commercial.model.CommercialTrade;
import org.eclipse.tradista.commercial.commercial.pricer.CommercialPricerUtil;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class CommercialPricerServiceBean implements CommercialPricerService {

	@Override
	public BigDecimal npvDiscountedCashFlow(PricingParameter params, CommercialTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		try {
			return CommercialPricerUtil.npvDiscountedCashFlow(params, trade, currency, pricingDate);
		} catch (PricerException pe) {
			throw new TradistaBusinessException(pe.getMessage());
		}
	}

	@Override
	public List<CashFlow> generateCashFlows(PricingParameter params, CommercialTrade trade, LocalDate pricingDate)
			throws TradistaBusinessException {
		try {
			return CommercialPricerUtil.generateCashFlows(params, trade, pricingDate);
		} catch (PricerException pe) {
			throw new TradistaBusinessException(pe.getMessage());
		}
	}

}
