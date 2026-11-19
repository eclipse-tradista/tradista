package org.eclipse.tradista.commercial.commercial.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricerMeasure;
import org.eclipse.tradista.core.pricing.pricer.Pricing;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;

public class PricerMeasureNPV extends PricerMeasure {

	private static final long serialVersionUID = 1L;

	public PricerMeasureNPV() {
		super();
	}

	@Pricing
	public BigDecimal discountedCashFlow(PricingParameter params, CommercialTrade trade, Currency currency,
			LocalDate pricingDate) throws PricerException, TradistaBusinessException {
		return BigDecimal.ZERO;
	}

	@Override
	public String toString() {
		return "NPV";
	}

}
