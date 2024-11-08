package org.eclipse.tradista.ir.fra.bootstraphandler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.index.service.IndexBusinessDelegate;
import org.eclipse.tradista.core.marketdata.bootstraphandler.BootstrapHandler;
import org.eclipse.tradista.core.marketdata.model.Instrument;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.ir.fra.model.FRATrade;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

public class FRABootstrapHandler implements BootstrapHandler {

	private ConfigurationBusinessDelegate configurationBusinessDelegate;

	public FRABootstrapHandler() {
		configurationBusinessDelegate = new ConfigurationBusinessDelegate();
	}

	@Override
	public String getInstrumentName() {
		return FRATrade.FRA;
	}

	@Override
	public String getKeyFromQuoteName(String quoteName) {
		return quoteName.substring(FRATrade.FRA.length() + 1, quoteName.length());
	}

	@Override
	public Instrument getInstrumentByKey(String key, LocalDate quoteDate) {
		String[] prop = key.split("\\.");
		String indexName = prop[0];
		String periods = prop[1];
		String[] periodsProp = periods.split("x");
		String startTime = periodsProp[0];
		String interestTime = periodsProp[1];
		LocalDate maturityDate = null;
		LocalDate settlementDate = null;

		FRATrade fra = new FRATrade();
		try {
			fra.setReferenceRateIndex(new IndexBusinessDelegate().getIndexByName(indexName));
		} catch (TradistaBusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		switch (startTime) {
		case "1M": {
			settlementDate = quoteDate.plus(1, ChronoUnit.MONTHS);
			break;
		}
		case "2M": {
			settlementDate = quoteDate.plus(2, ChronoUnit.MONTHS);
			break;
		}
		case "3M": {
			settlementDate = quoteDate.plus(3, ChronoUnit.MONTHS);
			break;
		}
		case "4M": {
			settlementDate = quoteDate.plus(4, ChronoUnit.MONTHS);
			break;
		}
		case "5M": {
			settlementDate = quoteDate.plus(5, ChronoUnit.MONTHS);
			break;
		}
		case "6M": {
			settlementDate = quoteDate.plus(6, ChronoUnit.MONTHS);
			break;
		}
		case "1Y": {
			settlementDate = quoteDate.plus(1, ChronoUnit.YEARS);
			break;
		}
		}
		fra.setSettlementDate(settlementDate);

		switch (interestTime) {
		case "3M": {
			maturityDate = quoteDate.plus(3, ChronoUnit.MONTHS);
			break;
		}
		case "6M": {
			maturityDate = quoteDate.plus(6, ChronoUnit.MONTHS);
			break;
		}
		case "1Y": {
			maturityDate = quoteDate.plus(1, ChronoUnit.YEARS);
			break;
		}
		}
		fra.setMaturityDate(maturityDate);

		return fra;
	}

	@Override
	public boolean isZeroCoupon(Instrument instrument, LocalDate quoteDate) {
		return false;
	}

	@Override
	public LocalDate getMaturityDate(Instrument instrument) {
		return ((FRATrade) instrument).getMaturityDate();
	}

	@Override
	public String getKey(Instrument instrument, LocalDate quoteDate) {
		FRATrade fra = ((FRATrade) instrument);
		Period period = Period.between(quoteDate, fra.getSettlementDate());
		String settlement = null;
		String maturity = null;
		if (period.getYears() == 1) {
			settlement = Tenor.ONE_YEAR.toString();
		} else {
			if (period.getMonths() == 6) {
				settlement = Tenor.SIX_MONTHS.toString();
			}
			if (period.getMonths() == 5) {
				settlement = Tenor.FIVE_MONTHS.toString();
			}
			if (period.getMonths() == 4) {
				settlement = Tenor.FOUR_MONTHS.toString();
			}
			if (period.getMonths() == 3) {
				settlement = Tenor.THREE_MONTHS.toString();
			}
			if (period.getMonths() == 2) {
				settlement = Tenor.TWO_MONTHS.toString();
			}
			if (period.getMonths() == 1) {
				settlement = Tenor.ONE_MONTH.toString();
			}

		}

		period = Period.between(quoteDate, fra.getMaturityDate());
		if (period.getYears() == 1) {
			maturity = Tenor.ONE_YEAR.toString();
		} else {
			if (period.getMonths() == 6) {
				maturity = Tenor.SIX_MONTHS.toString();
			}
			if (period.getMonths() == 3) {
				maturity = Tenor.THREE_MONTHS.toString();
			}

		}
		return fra.getReferenceRateIndex().getName() + "." + settlement + "x" + maturity;
	}

	@Override
	public List<CashFlow> getPendingCashFlows(Instrument instrument, LocalDate quoteDate, BigDecimal price,
			Map<LocalDate, List<BigDecimal>> zeroCouponRates, Map<LocalDate, BigDecimal> interpolatedValues) {
		// Idea : consider the FRA as a ZC instrument, deducing the spot rate at
		// maturity from the forward rate.
		// from John Hull's book:
		// F = (R2T2 - R1T1) / (T2 - T1)
		FRATrade fra = ((FRATrade) instrument);
		List<CashFlow> coupons = new ArrayList<CashFlow>();
		CashFlow coupon = new CashFlow();
		coupon.setDate(fra.getMaturityDate());
		LocalDate settlementDate = getSettlementtDate(getKey(instrument, quoteDate), quoteDate);
		BigDecimal t2 = PricerUtil.daysToYear(quoteDate, getMaturityDate(instrument));
		BigDecimal t1 = PricerUtil.daysToYear(quoteDate, settlementDate);
		BigDecimal r1;
		List<BigDecimal> zcsSettlementDate = zeroCouponRates.get(fra.getSettlementDate());
		if (zcsSettlementDate != null) {
			r1 = zcsSettlementDate.get(0);
		} else {
			r1 = interpolatedValues.get(fra.getSettlementDate());
		}

		BigDecimal zc = (price.divide(BigDecimal.valueOf(100)).multiply(t2.subtract(t1)).add(r1.multiply(t1)))
				.divide(t2, configurationBusinessDelegate.getRoundingMode());
		// this coupon is made of the deduced spot rate at maturity * 100 + 100. These
		// last 100 is the nominal to give back.
		coupon.setAmount(
				zc.multiply(BigDecimal.valueOf(100).divide(t2, configurationBusinessDelegate.getRoundingMode()))
						.add(BigDecimal.valueOf(100)));
		coupons.add(coupon);

		return coupons;

	}

	@Override
	public Tenor getFrequency(Instrument instrument) {
		return Tenor.NO_TENOR;
	}

	@Override
	public BigDecimal calcZeroCoupon(Instrument instrument, BigDecimal price, LocalDate quoteDate) {
		// not used, as FRA, even handled as ZC instrument, will need spot rate
		// at settlement date to deduce the spot rate at maturity date

		return null;

	}

	private LocalDate getSettlementtDate(String key, LocalDate quoteDate) {
		String[] prop = key.split("\\.");
		String periods = prop[1];
		String[] periodsProp = periods.split("x");
		String startTime = periodsProp[0];
		LocalDate settlementDate = null;
		switch (startTime) {
		case "1M": {
			settlementDate = quoteDate.plus(1, ChronoUnit.MONTHS);
			break;
		}
		case "2M": {
			settlementDate = quoteDate.plus(2, ChronoUnit.MONTHS);
			break;
		}
		case "3M": {
			settlementDate = quoteDate.plus(3, ChronoUnit.MONTHS);
			break;
		}
		case "4M": {
			settlementDate = quoteDate.plus(4, ChronoUnit.MONTHS);
			break;
		}
		case "5M": {
			settlementDate = quoteDate.plus(5, ChronoUnit.MONTHS);
			break;
		}
		case "6M": {
			settlementDate = quoteDate.plus(6, ChronoUnit.MONTHS);
			break;
		}
		case "1Y": {
			settlementDate = quoteDate.plus(1, ChronoUnit.YEARS);
			break;
		}
		}
		return settlementDate;
	}

	@Override
	/**
	 * 100 because the deduced spot rate at maturity is considered at par and, so,
	 * is equal to the coupon of a par bond that sets the present value of a bond to
	 * 100.
	 */
	public BigDecimal getPrice(BigDecimal price) {
		return BigDecimal.valueOf(100);
	}

}
