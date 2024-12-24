package org.eclipse.tradista.core.marketdata.generationalgorithm;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.TradistaUtil;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.marketdata.bootstraphandler.BootstrapHandler;
import org.eclipse.tradista.core.marketdata.interpolator.UnivariateInterpolator;
import org.eclipse.tradista.core.marketdata.model.Instrument;
import org.eclipse.tradista.core.marketdata.model.Quote;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.marketdata.model.RatePoint;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;
import org.eclipse.tradista.core.tenor.model.Tenor;

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

public class Bootstrap implements InterestRateCurveGenerationAlgorithm {

	private ConfigurationBusinessDelegate configurationBusinessDelegate;

	public Bootstrap() {
		configurationBusinessDelegate = new ConfigurationBusinessDelegate();
	}

	/**
	 * 
	 * IMPORTANT NOTE
	 * 
	 * FURTHER TESTS TO DO ON THIS ALGORITHM
	 * 
	 * 
	 * WE NEED MORE WARNING FOR END USERS WHEN IT IS NOT POSSIBLE TO INTERPOLATE FOR
	 * A DATE. THIS CAN HAPPEN WHEN WE DON'T HAVE SUFFICIENT ZERO COUPON BONDS AND
	 * WHEN WE NEED INTERPOLATION TO DISCOUNT A COUPON WHEN BOOTSTRAPPING A BOND. Ex
	 * : curve based on two bonds having 3M coupon frequency. 1 is ZC with a
	 * maturity date in 3 months. the other is not zc with a maturity date in 1
	 * year. Impossible to bootstrap the non zc because its coupon number 2 cannot
	 * be interpolated. (a kind of "business" error and not a bug).
	 * 
	 * 
	 * ALGO COMPLEXITY CAN ALSO BE IMPROVED QUESTION : WILL WE MANAGE OTHER PRODUCTS
	 * THAN BONDS ?
	 * 
	 * @throws TradistaBusinessException
	 */
	public List<RatePoint> generate(String instance, List<Long> quoteIds, LocalDate quoteDate, QuoteSet quoteSet,
			UnivariateInterpolator interpolator) {
		List<RatePoint> ratePoints = new ArrayList<RatePoint>();
		List<Quote> quotes = new ArrayList<Quote>(quoteIds.size());
		Map<String, Instrument> instruments = new HashMap<String, Instrument>();
		Map<String, QuoteValue> quoteValues = new HashMap<String, QuoteValue>();
		Map<LocalDate, List<BigDecimal>> zeroCouponRates = new TreeMap<LocalDate, List<BigDecimal>>();
		SortedMap<LocalDate, List<Instrument>> bondsByMatDate = new TreeMap<LocalDate, List<Instrument>>();
		Map<String, BootstrapHandler> handlerByInstrument = new TreeMap<String, BootstrapHandler>();

		// Load the quote (and quote values) from Quote Id
		for (long quoteId : quoteIds) {
			Quote quote = new QuoteBusinessDelegate().getQuoteById(quoteId);
			if (quote != null) {
				quotes.add(quote);
			}
		}

		List<BootstrapHandler> handlers = TradistaUtil.getAllInstancesByType(BootstrapHandler.class,
				"org.eclipse.tradista");
		for (BootstrapHandler handler : handlers) {
			handlerByInstrument.put(handler.getInstrumentName(), handler);
		}
		// 1. Load Product defs from Quotes
		// Assuming Bond quotes have the following name pattern : Bond.ISIN.ExchangeCode
		// and
		// IRSwap quotes this one: IRSwap.ReferenceRate.Frequency.Maturity
		for (Quote quote : quotes) {
			String quoteName = quote.getName();

			for (BootstrapHandler handler : handlers) {

				// If this is a quote of an available instrument type, we don't
				// use it
				if (!quoteName.contains(handler.getInstrumentName())) {
					continue;
				}
				// Extract the instrument key
				String key = handler.getKeyFromQuoteName(quoteName);

				// Get the Bond from key: ISIN + exchange
				try {
					instruments.put(key, handler.getInstrumentByKey(key, quoteDate));
					quoteValues.put(key, quoteSet.getQuoteValueByQuoteAndDate(quote, quoteDate));
				} catch (TradistaBusinessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		for (Instrument instrument : instruments.values()) {
			BootstrapHandler handler = handlerByInstrument.get(instrument.getInstrumentName());

			if (handler.isZeroCoupon(instrument, quoteDate)) {
				LocalDate maturityDate = handler.getMaturityDate(instrument);
				List<BigDecimal> zcRates = zeroCouponRates.get(maturityDate);
				if (zcRates == null) {
					zcRates = new ArrayList<BigDecimal>();
				}
				QuoteValue quoteValue = quoteValues.get(handler.getKey(instrument, quoteDate));
				BigDecimal price = quoteValue.getValue(instance);
				zcRates.add(handler.calcZeroCoupon(instrument, price, quoteValue.getDate()));
				zeroCouponRates.put(maturityDate, zcRates);
			} else {
				LocalDate maturityDate = handler.getMaturityDate(instrument);
				List<Instrument> bondList = bondsByMatDate.get(maturityDate);
				if (bondList == null) {
					bondList = new ArrayList<Instrument>();
				}
				bondList.add(instrument);
				bondsByMatDate.put(maturityDate, bondList);
			}
		}

		// Loop in BondsByMatDate and compute, bond by bond, the zc rate. Add
		// the rate to the zeroCouponRates's map for the concerned date
		for (List<Instrument> bondsbyMat : bondsByMatDate.values()) {
			BootstrapHandler helper = handlerByInstrument.get(bondsbyMat.get(0).getInstrumentName());
			// Compute averages for dates before current bond maturity date
			for (Iterator<Map.Entry<LocalDate, List<BigDecimal>>> it = zeroCouponRates.entrySet().iterator(); it
					.hasNext();) {
				Map.Entry<LocalDate, List<BigDecimal>> entry = it.next();
				if (helper.getMaturityDate(bondsbyMat.get(0)).isAfter(entry.getKey())) {
					List<BigDecimal> zcs = entry.getValue();
					zcs = computeAverage(zcs);
					entry.setValue(zcs);
				}
			}
			List<BigDecimal> zcRates = zeroCouponRates.get(helper.getMaturityDate(bondsbyMat.get(0)));
			if (zcRates == null) {
				zcRates = new ArrayList<BigDecimal>();
			}

			// Bootstrapping the zero coupons
			for (Instrument b : bondsbyMat) {
				QuoteValue quoteValue = quoteValues.get(helper.getKey(b, quoteDate));
				BigDecimal price = quoteValue.getValue(instance);
				try {
					zcRates.add(calcZeroCoupon(b, price, zeroCouponRates, quoteValue.getDate(), interpolator, helper));
				} catch (TradistaBusinessException e) {
					// TODO log warning
				}
			}
			zeroCouponRates.put(helper.getMaturityDate(bondsbyMat.get(0)), zcRates);
		}

		// Compute the average zc for the last bond maturity date
		// the map is empty if there were only zc instruments.
		if (!bondsByMatDate.isEmpty()) {
			LocalDate lastMaturityDate = bondsByMatDate.lastKey();
			List<BigDecimal> zcs = zeroCouponRates.get(lastMaturityDate);
			zcs = computeAverage(zcs);
			zeroCouponRates.put(lastMaturityDate, zcs);
		}

		// Assuming we use the Commons Math interpolators, the dates must be
		// converted in a array of int. the big decimal in an array of double
		Map<LocalDate, BigDecimal> interpolatedValues = interpolator
				.interpolate(prepareForInterpolation(zeroCouponRates));
		for (Map.Entry<LocalDate, BigDecimal> entry : interpolatedValues.entrySet()) {
			ratePoints.add(new RatePoint(entry.getKey(), entry.getValue()));
		}

		return ratePoints;
	}

	/**
	 * Bootstrap the Zero Coupon (cf Hull)
	 * 
	 * @param bond
	 * @param price
	 * @param zeroCouponRates
	 * @param date
	 * @return
	 * @throws TradistaBusinessException
	 */
	private BigDecimal calcZeroCoupon(Instrument instrument, BigDecimal price,
			Map<LocalDate, List<BigDecimal>> zeroCouponRates, LocalDate date, UnivariateInterpolator interpolator,
			BootstrapHandler helper) throws TradistaBusinessException {
		int scale = configurationBusinessDelegate.getScale();
		// Interpolate in order to have zc values for all the dates
		Map<LocalDate, BigDecimal> interpolatedValues = interpolator
				.interpolate(prepareForInterpolation(zeroCouponRates));
		// 1. Calculate the pending cashflows
		List<CashFlow> coupons = helper.getPendingCashFlows(instrument, date, price, zeroCouponRates,
				interpolatedValues);
		// 2. Discount them using the zero coupon rates
		BigDecimal discountFactor;
		BigDecimal discount = BigDecimal.ZERO;
		BigDecimal discountInc = BigDecimal.ZERO;
		Tenor frequency = helper.getFrequency(instrument);
		if (frequency.equals(Tenor.THREE_MONTHS)) {
			discountInc = new BigDecimal("0.25");
		}
		if (frequency.equals(Tenor.SIX_MONTHS)) {
			discountInc = new BigDecimal("0.5");
		}
		if (frequency.equals(Tenor.ONE_YEAR)) {
			discountInc = BigDecimal.ONE;
		}
		// Frequency is once
		if (frequency.equals(Tenor.NO_TENOR)) {
			discountInc = BigDecimal.valueOf(ChronoUnit.DAYS.between(date, helper.getMaturityDate(instrument)) / 365);
		}
		BigDecimal discountedCoupons = BigDecimal.ZERO;
		CashFlow lastCf = new CashFlow();
		// Loop in the coupons excepted the last one (paid at the maturity date)
		for (CashFlow cf : coupons) {
			discount = discount.add(discountInc);
			if (!cf.getDate().isEqual(helper.getMaturityDate(instrument))) {
				List<BigDecimal> zc = zeroCouponRates.get(cf.getDate());
				if (zc != null) {
					discountFactor = zc.get(0);
					System.out.println("discount factor (from zc map): " + discountFactor);
				} else {
					discountFactor = interpolatedValues.get(cf.getDate());
					System.out.println("discount factor (from interpolatedValue, coupon date: " + cf.getDate() + "): "
							+ discountFactor);
				}

				discountedCoupons = discountedCoupons.add(cf.getAmount()).multiply(
						BigDecimal.valueOf(Math.exp(discountFactor.negate().multiply(discount).doubleValue())));
			} else {
				lastCf = cf;
			}
		}

		BigDecimal zeroCoupon = BigDecimal.valueOf(Math.log((helper.getPrice(price).subtract(discountedCoupons))
				.divide(lastCf.getAmount(), scale, configurationBusinessDelegate.getRoundingMode()).doubleValue()))
				.divide(discount).negate();
		return zeroCoupon;
	}

	private SortedMap<LocalDate, BigDecimal> prepareForInterpolation(Map<LocalDate, List<BigDecimal>> zeroCouponRates) {
		SortedMap<LocalDate, BigDecimal> preparedMap = new TreeMap<LocalDate, BigDecimal>();
		for (LocalDate date : zeroCouponRates.keySet()) {
			List<BigDecimal> values = zeroCouponRates.get(date);
			if (values != null) {
				// Case where we reach a date where average zc is not yet
				// computed : we compute the average and put it in the prepared
				// map
				if (values.size() > 1) {
					preparedMap.put(date, computeAverage(values).get(0));
				} else {
					preparedMap.put(date, values.get(0));
				}
			}
		}

		return preparedMap;
	}

	private List<BigDecimal> computeAverage(List<BigDecimal> values) {
		List<BigDecimal> avg = new ArrayList<BigDecimal>();
		BigDecimal val = BigDecimal.valueOf(0);
		for (BigDecimal dec : values) {
			val = val.add(dec);
		}
		val = val.divide(BigDecimal.valueOf(values.size()));
		avg.add(val);
		return avg;
	}

}