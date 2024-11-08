package org.eclipse.tradista.ir.irswap.bootstraphandler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.index.service.IndexBusinessDelegate;
import org.eclipse.tradista.core.marketdata.bootstraphandler.BootstrapHandler;
import org.eclipse.tradista.core.marketdata.model.Instrument;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.ir.irswap.model.IRSwapTrade;
import org.eclipse.tradista.ir.irswap.model.SingleCurrencyIRSwapTrade;

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

public class IRSwapTradeBootstrapHandler implements BootstrapHandler {

	@Override
	public String getInstrumentName() {
		return IRSwapTrade.IR_SWAP;
	}

	@Override
	/**
	 * IRSwap quote name : IRSwap.CURVENAME.FREQUENCY.MATURITY IRSwap quote key :
	 * INDEX.FREQUENCY.MATURITY
	 */
	public String getKeyFromQuoteName(String quoteName) {
		return quoteName.substring(IRSwapTrade.IR_SWAP.length() + 1);
	}

	@Override
	public Instrument getInstrumentByKey(String key, LocalDate quoteDate) {
		String[] prop = key.split("\\.");
		String indexName = prop[0];
		Tenor frequency = Tenor.valueOf(prop[1]);
		String maturity = prop[2];
		LocalDate maturityDate = null;

		SingleCurrencyIRSwapTrade irSwap = new SingleCurrencyIRSwapTrade();
		try {
			irSwap.setReceptionReferenceRateIndex(new IndexBusinessDelegate().getIndexByName(indexName));
		} catch (TradistaBusinessException e) {
			// TODO Manage exception
		}
		irSwap.setReceptionFrequency(frequency);
		switch (maturity) {
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
		irSwap.setMaturityDate(maturityDate);

		return irSwap;

	}

	@Override
	public boolean isZeroCoupon(Instrument instrument, LocalDate quoteDate) {
		IRSwapTrade irSwap = ((IRSwapTrade) instrument);
		Tenor frequency = irSwap.getReceptionFrequency();
		LocalDate cfDate = null;
		if (frequency.equals(Tenor.THREE_MONTHS)) {
			cfDate = quoteDate.plus(3, ChronoUnit.MONTHS);
		}
		if (frequency.equals(Tenor.SIX_MONTHS)) {
			cfDate = quoteDate.plus(6, ChronoUnit.MONTHS);
		}
		if (frequency.equals(Tenor.ONE_YEAR)) {
			cfDate = quoteDate.plus(1, ChronoUnit.YEARS);
		}
		return !irSwap.getMaturityDate().isAfter(cfDate);
	}

	@Override
	public LocalDate getMaturityDate(Instrument instrument) {
		return ((IRSwapTrade) instrument).getMaturityDate();
	}

	@Override
	public String getKey(Instrument instrument, LocalDate quoteDate) {
		IRSwapTrade irSwap = ((IRSwapTrade) instrument);
		Period period = Period.between(quoteDate, irSwap.getMaturityDate());
		String maturity = null;
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
		return irSwap.getReceptionReferenceRateIndex().getName() + "." + irSwap.getReceptionFrequency() + "."
				+ maturity;
	}

	/**
	 * To calculate the ir swap pending cfs, we handle it as a Bond. This, because a
	 * par swap rate is equal to the coupon of a par bond that sets the present
	 * value of a bond to 100. Sources : A financial bestiary :
	 * https://books.google.fr/books?id=23LIzAAcp4oC&pg=PA138&lpg=PA138&dq=%
	 * 22swap+par+rate%22+bond&source=bl&ots=OVKaK3Z7rZ&sig=
	 * cCcaC5o1x_2DIin_Y_UKZzZNXGw&hl=en&sa=X&ei=OyKxVIigI4Lj7QaBhIHwAw&ved=
	 * 0CCEQ6AEwAA#v=snippet&q=bootstrap&f=false 1st answer of
	 * http://quant.stackexchange.com/questions/7402/how-to-derive-yield-curve-
	 * from-interest-rate-swap?answertab=oldest#tab-top The book Interest rate
	 * models of Damiano Brigo seems also interesting.
	 */
	@Override
	public List<CashFlow> getPendingCashFlows(Instrument instrument, LocalDate date, BigDecimal price,
			Map<LocalDate, List<BigDecimal>> zeroCouponRates, Map<LocalDate, BigDecimal> interpolatedValues) {
		List<CashFlow> cashFlows = new ArrayList<CashFlow>();
		IRSwapTrade irSwapTrade = ((IRSwapTrade) instrument);
		Tenor frequency = irSwapTrade.getReceptionFrequency();
		LocalDate cashFlowDate = LocalDate.from(date);
		LocalDate maturityDate = irSwapTrade.getMaturityDate();
		// We are calculation cfs , handling the swap as a bond where coupon =
		// price (adjusted with accrual), a principal of 100 and having to have
		// all the discounted cfs equal to price.
		// this because the price is the swap par rate
		BigDecimal coupon = price;

		switch (frequency) {
		case THREE_MONTHS: {
			coupon = coupon.divide(BigDecimal.valueOf(4));
			break;
		}
		case SIX_MONTHS: {
			coupon = coupon.divide(BigDecimal.valueOf(2));
			break;
		}
		case ONE_YEAR: {
			// unchanged
			break;
		}
		case EIGHTEEN_MONTHS: {
			coupon = coupon.multiply(BigDecimal.valueOf(1.5));
			break;
		}

		case ONE_MONTH: {
			coupon = coupon.divide(BigDecimal.valueOf(12));
			break;
		}
		case TWO_MONTHS: {
			coupon = coupon.divide(BigDecimal.valueOf(6));
			break;
		}
		case TWO_YEARS: {
			coupon = coupon.multiply(BigDecimal.valueOf(2));
			break;
		}
		default:
			break;
		}

		while (!cashFlowDate.isAfter(irSwapTrade.getMaturityDate())) {
			if (cashFlowDate.isAfter(date)) {
				CashFlow cashFlow = new CashFlow();

				if (cashFlowDate.isEqual(maturityDate)) {
					cashFlow.setAmount(coupon.add(BigDecimal.valueOf(100)));
				}

				else {
					cashFlow.setAmount(coupon);
				}

				cashFlow.setDate(cashFlowDate);
				cashFlows.add(cashFlow);
			}

			if (frequency.equals(Tenor.THREE_MONTHS)) {
				cashFlowDate = cashFlowDate.plus(3, ChronoUnit.MONTHS);
			}
			if (frequency.equals(Tenor.SIX_MONTHS)) {
				cashFlowDate = cashFlowDate.plus(6, ChronoUnit.MONTHS);
			}
			if (frequency.equals(Tenor.ONE_YEAR)) {
				cashFlowDate = cashFlowDate.plus(1, ChronoUnit.YEARS);
			}

		}

		return cashFlows;
	}

	@Override
	public Tenor getFrequency(Instrument instrument) {
		return ((IRSwapTrade) instrument).getReceptionFrequency();
	}

	@Override
	public BigDecimal calcZeroCoupon(Instrument instrument, BigDecimal price, LocalDate date) {
		// In case of ZC Swaps (ie = 1 single payment), the zc rate is the swap
		// fixed rate (equals to the price, which is the par swap rate)
		return price.divide(BigDecimal.valueOf(100));
	}

	@Override
	/**
	 * 100 because a par swap rate is equal to the coupon of a par bond that sets
	 * the present value of a bond to 100.
	 */
	public BigDecimal getPrice(BigDecimal price) {
		return BigDecimal.valueOf(100);
	}

}
