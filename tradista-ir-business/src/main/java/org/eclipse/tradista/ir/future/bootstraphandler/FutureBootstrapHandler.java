package org.eclipse.tradista.ir.future.bootstraphandler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.marketdata.bootstraphandler.BootstrapHandler;
import org.eclipse.tradista.core.marketdata.model.Instrument;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.ir.future.model.Future;
import org.eclipse.tradista.ir.future.model.FutureTrade;
import org.eclipse.tradista.ir.future.service.FutureBusinessDelegate;
import org.eclipse.tradista.ir.future.service.FutureContractSpecificationBusinessDelegate;

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

public class FutureBootstrapHandler implements BootstrapHandler {

	private ConfigurationBusinessDelegate configurationBusinessDelegate;

	public FutureBootstrapHandler() {
		configurationBusinessDelegate = new ConfigurationBusinessDelegate();
	}

	@Override
	public String getInstrumentName() {
		return Future.FUTURE;
	}

	@Override
	public String getKeyFromQuoteName(String quoteName) {
		return quoteName.substring(Future.FUTURE.length() + 1, quoteName.length());
	}

	@Override
	public Instrument getInstrumentByKey(String key, LocalDate quoteDate) throws TradistaBusinessException {
		String[] prop = key.split("\\.");
		String contractSpecificationName = prop[0];
		String symbol = prop[1];
		FutureContractSpecificationBusinessDelegate futureContractSpecificationBusinessDelegate = new FutureContractSpecificationBusinessDelegate();
		FutureBusinessDelegate futureBusinessDelegate = new FutureBusinessDelegate();
		FutureTrade futureTrade = new FutureTrade();
		Future future = futureBusinessDelegate.getFutureByContractSpecificationAndSymbol(contractSpecificationName,
				symbol);
		futureTrade.setProduct(future);
		String monthProp = symbol.substring(0, 3);
		int month = futureBusinessDelegate.getMonth(monthProp).getValue();
		String yearProp = symbol.substring(2);
		int year = 2000 + Integer.parseInt(yearProp);

		// The maturity date of the product is the fixing date for the
		// calculation of the interest rate.
		futureTrade.setSettlementDate(
				futureContractSpecificationBusinessDelegate.getMaturityDate(contractSpecificationName, month, year));

		return futureTrade;
	}

	@Override
	public boolean isZeroCoupon(Instrument instrument, LocalDate quoteDate) {
		return false;
	}

	@Override
	public LocalDate getMaturityDate(Instrument instrument) {
		try {
			return DateUtil.addTenor(((FutureTrade) instrument).getMaturityDate(),
					((FutureTrade) instrument).getProduct().getReferenceRateIndexTenor());
		} catch (TradistaBusinessException abe) {
			// Should not appear here.
		}
		return null;
	}

	@Override
	public String getKey(Instrument instrument, LocalDate quoteDate) {
		FutureTrade futureTrade = ((FutureTrade) instrument);
		Future future = futureTrade.getProduct();
		return future.getContractSpecification().getName() + "." + future.getSymbol();
	}

	@Override
	public List<CashFlow> getPendingCashFlows(Instrument instrument, LocalDate quoteDate, BigDecimal price,
			Map<LocalDate, List<BigDecimal>> zeroCouponRates, Map<LocalDate, BigDecimal> interpolatedValues)
			throws TradistaBusinessException {
		// Idea : like FRA, consider the Future as a ZC instrument, deducing the
		// spot rate at maturity from the forward rate.
		// from John Hull's book:
		// F = (R2T2 - R1T1) / (T2 - T1)
		FutureTrade future = ((FutureTrade) instrument);
		List<CashFlow> coupons = new ArrayList<CashFlow>();
		CashFlow coupon = new CashFlow();
		coupon.setDate(future.getMaturityDate());
		LocalDate settlementDate = getSettlementtDate(getKey(instrument, quoteDate), quoteDate);
		BigDecimal t2 = PricerUtil.daysToYear(future.getDayCountConvention(), quoteDate, getMaturityDate(instrument));
		BigDecimal t1 = PricerUtil.daysToYear(future.getDayCountConvention(), quoteDate, settlementDate);
		BigDecimal r1;
		List<BigDecimal> zcsSettlementDate = zeroCouponRates.get(future.getSettlementDate());
		if (zcsSettlementDate != null) {
			r1 = zcsSettlementDate.get(0);
		} else {
			r1 = interpolatedValues.get(future.getSettlementDate());
		}
		// Implicit fwd rate from price is : 100 - price
		BigDecimal fwdRate = BigDecimal.valueOf(100).subtract(price);

		BigDecimal zc = (fwdRate.divide(BigDecimal.valueOf(100)).multiply(t2.subtract(t1)).add(r1.multiply(t1)))
				.divide(t2, RoundingMode.HALF_EVEN);
		// this coupon is made of the deduced spot rate at maturity * 100 + 100.
		// These last 100 is the nominal to give back.
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
		// not used, as Future, even handled as ZC instrument, will need spot
		// rate
		// at settlement date to deduce the spot rate at maturity date

		return null;

	}

	private LocalDate getSettlementtDate(String key, LocalDate quoteDate) throws TradistaBusinessException {
		String[] prop = key.split("\\.");
		String contractName = prop[0];
		String symbol = prop[1];
		String monthProp = symbol.substring(0, 3);
		int month = new FutureBusinessDelegate().getMonth(monthProp).getValue();
		String yearProp = symbol.substring(2);
		int year = 2000 + Integer.parseInt(yearProp);
		return new FutureContractSpecificationBusinessDelegate().getMaturityDate(contractName, month, year);
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