package org.eclipse.tradista.core.pricing.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.daycountconvention.model.DayCountConvention;
import org.eclipse.tradista.core.marketdata.model.Curve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.marketdata.model.RatePoint;
import org.eclipse.tradista.core.marketdata.service.CurveBusinessDelegate;
import org.eclipse.tradista.core.marketdata.service.InterestRateCurveBusinessDelegate;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;
import org.eclipse.tradista.core.pricing.exception.PricerException;
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

public final class PricerUtil {

	private static final String CURVE_ID_MUST_BE_POSITIVE = "The curve id must be positive.%n";

	private static final String DATE_CANNOT_BE_NULL = "The date cannot be null.%n";

	private static final String QUOTE_SET_ID_MUST_BE_POSITIVE = "The quote set id must be positive.%n";

	private PricerUtil() {
	}

	private static ConfigurationBusinessDelegate configurationBusinessDelegate = new ConfigurationBusinessDelegate();

	/**
	 * Gets a value from: - the specified quotes if the value date is in the past or
	 * the current date - the specified curve if it is in the future
	 * 
	 * @param quoteName  the name of the quote where to look for the value if the
	 *                   value date is in the past or the present date
	 * @param quoteSetId the quote set where to look for the value if the value date
	 *                   is in the past or the present date
	 * @param quoteType  the quote type where to look for the value if the value
	 *                   date is in the past or the present date
	 * @param curveId    the id of the curve where to look for the value if the
	 *                   value date is in the future
	 * @param valueDate  the value date
	 * @return the searched value
	 * @throws PricerException when it is impossible to get the value
	 * 
	 */
	public static BigDecimal getValueAsOfDate(String quoteName, long quoteSetId, QuoteType quoteType,
			String quoteValueType, long curveId, LocalDate valueDate) throws PricerException {
		valueDate = (valueDate == null) ? LocalDate.now() : valueDate;

		if (valueDate.isAfter(LocalDate.now())) {
			// Look into the curve
			return getValueAsOfDateFromCurve(curveId, valueDate);
		} else {
			BigDecimal value;
			value = getValueAsOfDateFromQuote(quoteName, quoteSetId, quoteType, quoteValueType, valueDate);
			// If we found nothing in the quote, we try in the curve.
			if (value == null) {
				return getValueAsOfDateFromCurve(curveId, valueDate);
			}
		}
		return null;
	}

	/**
	 * Gets a rate: from the quotes if the value date is in the past (or present)
	 * and deduced from a curve if it is in the future
	 * 
	 * @param quoteName    the name of the quote where to look for the value if the
	 *                     value date is in the past or the present date
	 * @param quoteSetId   the quote set where to look for the value if the value
	 *                     date is in the past or the present date
	 * @param indexCurveId the id of the curve where to look for the value if the
	 *                     value date is in the future
	 * @param tenor        the tenor of the index rate
	 * @param dcc          the day count convention used to calculate the forward
	 *                     rate.
	 * @param valueDate    the value date
	 * @return the searched index rate
	 * @throws PricerException when it is impossible to get the value
	 * 
	 */
	public static BigDecimal getInterestRateAsOfDate(String quoteName, long quoteSetId, long indexCurveId, Tenor tenor,
			DayCountConvention dcc, LocalDate valueDate) throws PricerException {
		valueDate = (valueDate == null) ? LocalDate.now() : valueDate;
		if (valueDate.isAfter(LocalDate.now())) {
			// Look into the curve
			if (tenor.equals(Tenor.NO_TENOR)) {
				throw new PricerException(String.format(
						"Impossible to calculate a forward rate with a Tenor being equals to %s", Tenor.NO_TENOR));
			}
			try {
				return getForwardRate(indexCurveId, valueDate, DateUtil.addTenor(valueDate, tenor), dcc);
			} catch (TradistaBusinessException tbe) {
				// Should not happen here.
			}
		} else {
			BigDecimal rate = getValueAsOfDateFromQuote(quoteName, quoteSetId, QuoteType.INTEREST_RATE,
					QuoteValue.CLOSE, valueDate);
			if (rate == null) {
				rate = getValueAsOfDateFromQuote(quoteName, quoteSetId, QuoteType.INTEREST_RATE, QuoteValue.LAST,
						valueDate);
			}

			if (rate == null) {
				// If valueDate is today and no quotes are available, we try to look in the
				// curve as of today + Tenor
				if (valueDate.equals(LocalDate.now())) {
					if (tenor.equals(Tenor.NO_TENOR)) {
						throw new PricerException(
								String.format("Impossible to look for a value in curve with a Tenor being equals to %s",
										Tenor.NO_TENOR));
					}
					try {
						rate = PricerUtil.getValueAsOfDateFromCurve(indexCurveId, DateUtil.addTenor(valueDate, tenor));
					} catch (TradistaBusinessException tbe) {
						// Should not happen here.
					}
				}
			}
			return rate;
		}
		return null;
	}

	public static BigDecimal getValueAsOfDateFromQuote(String quoteName, long quoteSetId, QuoteType quoteType,
			String quoteValueType, LocalDate valueDate) {
		QuoteBusinessDelegate quoteBusinessDelegate = new QuoteBusinessDelegate();
		QuoteValue quoteValue = quoteBusinessDelegate.getQuoteValueByQuoteSetIdQuoteNameTypeAndDate(quoteSetId,
				quoteName, quoteType, valueDate);
		if (quoteValue != null) {
			if (quoteValueType != null) {
				return quoteValue.getValue(quoteValueType);
			}
		}

		// No quote found at this date, we try to interpolate
		LocalDate min = valueDate.minus(1, ChronoUnit.YEARS);
		LocalDate max = valueDate.plus(1, ChronoUnit.YEARS);
		LocalDate minDate = valueDate;
		LocalDate maxDate = valueDate;
		BigDecimal minValue = null;
		BigDecimal maxValue = null;
		int daysNumberMin = 0;
		int daysNumberMax = 0;

		while (!minDate.isBefore(min)) {
			quoteValue = quoteBusinessDelegate.getQuoteValueByQuoteSetIdQuoteNameTypeAndDate(quoteSetId, quoteName,
					quoteType, minDate);
			if (quoteValue != null) {
				if (quoteValueType != null) {
					minValue = quoteValue.getValue(quoteValueType);
					break;
				}
			}
			minDate = minDate.minusDays(1);
			daysNumberMin++;
		}

		while (!maxDate.isAfter(max)) {
			quoteValue = quoteBusinessDelegate.getQuoteValueByQuoteSetIdQuoteNameTypeAndDate(quoteSetId, quoteName,
					quoteType, maxDate);
			if (quoteValue != null) {
				if (quoteValueType != null) {
					maxValue = quoteValue.getValue(quoteValueType);
					break;
				}
			}
			maxDate = maxDate.plusDays(1);
		}

		if (minValue != null && maxValue != null) {
			BigDecimal progressionByDay = (maxValue.subtract(minValue)).divide(
					new BigDecimal(daysNumberMin + daysNumberMax), configurationBusinessDelegate.getRoundingMode());
			return minValue.add(progressionByDay.multiply(new BigDecimal(daysNumberMin)));
		}

		return null;
	}

	public static Map<String, BigDecimal> getValuesAsOfDateFromQuote(long quoteSetId, QuoteType quoteType,
			String quoteValueType, LocalDate valueDate, boolean interpolate, String... quoteNames) {
		QuoteBusinessDelegate quoteBusinessDelegate = new QuoteBusinessDelegate();
		Map<String, BigDecimal> values = null;
		Set<QuoteValue> quoteValues = quoteBusinessDelegate.getQuoteValuesByQuoteSetIdTypeDateAndQuoteNames(quoteSetId,
				quoteType, valueDate, quoteNames);
		if (quoteValues != null) {
			if (quoteValueType != null) {
				for (QuoteValue qv : quoteValues) {
					BigDecimal qValue = qv.getValue(quoteValueType);
					if (qValue != null) {
						if (values == null) {
							values = new HashMap<>();
						}
						values.put(qv.getQuote().getName(), qValue);
					}
				}
				return values;
			}
		}
		// No quote found at this date, we try to interpolate
		if (interpolate) {
			for (String name : quoteNames) {
				LocalDate min = valueDate.minus(1, ChronoUnit.YEARS);
				LocalDate max = valueDate.plus(1, ChronoUnit.YEARS);
				LocalDate minDate = valueDate;
				LocalDate maxDate = valueDate;
				BigDecimal minValue = null;
				BigDecimal maxValue = null;
				int daysNumberMin = 0;
				int daysNumberMax = 0;
				QuoteValue quoteValue;
				while (!minDate.isBefore(min)) {
					quoteValue = quoteBusinessDelegate.getQuoteValueByQuoteSetIdQuoteNameTypeAndDate(quoteSetId, name,
							quoteType, minDate);
					if (quoteValue != null) {
						if (quoteValueType != null) {
							minValue = quoteValue.getValue(quoteValueType);
							break;
						}
					}
					minDate = minDate.minusDays(1);
					daysNumberMin++;
				}

				while (!maxDate.isAfter(max)) {
					quoteValue = quoteBusinessDelegate.getQuoteValueByQuoteSetIdQuoteNameTypeAndDate(quoteSetId, name,
							quoteType, maxDate);
					if (quoteValue != null) {
						if (quoteValueType != null) {
							maxValue = quoteValue.getValue(quoteValueType);
							break;
						}
					}
					maxDate = maxDate.plusDays(1);
				}

				if (minValue != null && maxValue != null) {
					BigDecimal progressionByDay = (maxValue.subtract(minValue)).divide(
							new BigDecimal(daysNumberMin + daysNumberMax),
							configurationBusinessDelegate.getRoundingMode());
					if (values == null) {
						values = new HashMap<>();
					}
					values.put(name, minValue.add(progressionByDay.multiply(new BigDecimal(daysNumberMin))));
				}
			}
		}
		return values;
	}

	public static BigDecimal getValueAsOfDateFromCurve(long curveId, LocalDate valueDate) throws PricerException {
		Curve<LocalDate, BigDecimal> curve;
		CurveBusinessDelegate curveBusinessDelegate = new CurveBusinessDelegate();
		try {
			curve = curveBusinessDelegate.getCurveById(curveId);
		} catch (TradistaBusinessException tbe) {
			throw new PricerException(tbe.getMessage());
		}

		LocalDate min = valueDate.minus(1, ChronoUnit.YEARS);
		LocalDate max = valueDate.plus(1, ChronoUnit.YEARS);
		List<RatePoint> points;
		try {
			points = curveBusinessDelegate.getCurvePointsByCurveAndDates(curve, min, max);
		} catch (TradistaBusinessException tbe) {
			throw new PricerException(tbe.getMessage());
		}
		Map<LocalDate, BigDecimal> pointsMap = new TreeMap<>();
		for (RatePoint point : points) {
			pointsMap.put(point.getDate(), point.getRate());
		}
		BigDecimal value = pointsMap.get(valueDate);
		if (value != null) {
			return value;
		} else // if the interest rate is not present, perform a linear
				// interpolation to estimate it
		{
			LocalDate calcMin = LocalDate.from(valueDate);
			int daysNumberMin = 0;
			BigDecimal valueMin = null;

			while ((calcMin.isAfter(min)) && (valueMin == null)) {
				calcMin = calcMin.minus(1, ChronoUnit.DAYS);
				daysNumberMin++;
				valueMin = pointsMap.get(calcMin);
			}

			if (valueMin != null) {
				LocalDate calcMax = LocalDate.from(valueDate);
				int daysNumberMax = 0;
				BigDecimal valueMax = null;

				while ((calcMax.isBefore(max)) && (valueMax == null)) {
					calcMax = calcMax.plus(1, ChronoUnit.DAYS);
					daysNumberMax++;
					valueMax = pointsMap.get(calcMax);
				}

				if (valueMax != null) {
					BigDecimal progressionByDay = (valueMax.subtract(valueMin)).divide(
							new BigDecimal(daysNumberMin + daysNumberMax),
							configurationBusinessDelegate.getRoundingMode());
					return valueMin.add(progressionByDay.multiply(new BigDecimal(daysNumberMin)));
				}
			}
		}

		throw new PricerException(String.format(
				"'%s' doesn't contain a value for date %tD and it was not possible to interpolate.", curve, valueDate));
	}

	/**
	 * Method to discount an amount. Works whatever the dates are.
	 * 
	 * @param amount      the amount to discount
	 * @param curveId     the curve used to discount
	 * @param pricingDate the pricing date
	 * @param date        the date of the amount to be discounted.
	 * @param dcc         the day count convention used for accrual factor
	 *                    calculation (default value: ACT/365)
	 * @return a discounted amount.
	 * @throws PricerException           if there was a pricer exception.
	 * @throws TradistaBusinessException if a mandatory parameter is missing.
	 */
	public static BigDecimal discountAmount(BigDecimal amount, long curveId, LocalDate pricingDate, LocalDate date,
			DayCountConvention dcc) throws PricerException, TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();

		if (amount == null) {
			errMsg.append(String.format("The amount cannot be null.%n"));
		}

		if (curveId <= 0) {
			errMsg.append(String.format(CURVE_ID_MUST_BE_POSITIVE));
		}

		if (pricingDate == null) {
			errMsg.append(String.format("The pricing date cannot be null.%n"));
		}

		if (date == null) {
			errMsg.append(String.format(DATE_CANNOT_BE_NULL));
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		BigDecimal discountFactor = PricerUtil.getDiscountFactor(curveId, pricingDate, date, dcc);
		return amount.multiply(discountFactor);

	}

	/**
	 * Method to get a discount factor from a curve. Expressed as follows : 0,95 for
	 * a discount of 5 points.
	 * 
	 * @param curveId     the id of the curve used to get the discount factor
	 * @param pricingDate the pricing date
	 * @param date        the date for which we get the discount factor
	 * @param dcc         the day count convention used for accrual factor
	 *                    calculation (default value: ACT/365)
	 * @return a discount factor from a curve.
	 * @throws PricerException           if the curve was not found or if there was
	 *                                   an interpolation issue
	 * @throws TradistaBusinessException if a mandatory parameter is missing
	 */
	public static BigDecimal getDiscountFactor(long curveId, LocalDate pricingDate, LocalDate date,
			DayCountConvention dcc) throws PricerException, TradistaBusinessException {

		StringBuilder errMsg = new StringBuilder();

		if (curveId <= 0) {
			errMsg.append(String.format(CURVE_ID_MUST_BE_POSITIVE));
		}

		if (pricingDate == null) {
			errMsg.append(String.format("The pricing date cannot be null.%n"));
		}

		if (date == null) {
			errMsg.append(String.format(DATE_CANNOT_BE_NULL));
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

		if (dcc == null) {
			dcc = new DayCountConvention(DayCountConvention.ACT_365);
		}
		BigDecimal rateAtDate = PricerUtil.getDiscountFactor(curveId, date).divide(BigDecimal.valueOf(100),
				configurationBusinessDelegate.getScale(), configurationBusinessDelegate.getRoundingMode());
		BigDecimal rateAtPricingDate = PricerUtil.getDiscountFactor(curveId, pricingDate).divide(
				BigDecimal.valueOf(100), configurationBusinessDelegate.getScale(),
				configurationBusinessDelegate.getRoundingMode());
		BigDecimal fractionNowtoPricingDate = daysToYear(dcc, LocalDate.now(), pricingDate);
		BigDecimal fractionNowToDate = daysToYear(dcc, LocalDate.now(), date);
		return BigDecimal.valueOf(Math.exp(rateAtDate.negate().multiply(fractionNowToDate).doubleValue()))
				.divide(BigDecimal
						.valueOf(Math.exp(rateAtPricingDate.negate().multiply(fractionNowtoPricingDate).doubleValue())),
						configurationBusinessDelegate.getScale(), configurationBusinessDelegate.getRoundingMode());

	}

	public static void discountCashFlows(List<CashFlow> cashFlows, LocalDate valueDate, long discountCurveId,
			DayCountConvention dcc) throws PricerException, TradistaBusinessException {

		StringBuilder errMsg = new StringBuilder();

		if (cashFlows == null || cashFlows.isEmpty()) {
			errMsg.append(String.format("CashFlows list is null or empty.%n"));
		}

		if (valueDate == null) {
			errMsg.append(String.format("Value date cannot be null.%n"));
		}

		if (discountCurveId <= 0) {
			errMsg.append(String.format("The Discount curve id must be positive.%n"));
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

		if (cashFlows != null) {
			for (CashFlow cf : cashFlows) {
				BigDecimal discountedAmount = PricerUtil.discountAmount(cf.getAmount(), discountCurveId, valueDate,
						cf.getDate(), dcc);
				cf.setDiscountedAmount(discountedAmount);
				BigDecimal discountFactor = PricerUtil.getDiscountFactor(discountCurveId, valueDate, cf.getDate(),
						null);
				cf.setDiscountFactor(discountFactor);
			}
		}
	}

	public static void discountCashFlow(CashFlow cf, LocalDate valueDate, long discountCurveId, DayCountConvention dcc)
			throws PricerException, TradistaBusinessException {

		StringBuilder errMsg = new StringBuilder();

		if (cf == null) {
			errMsg.append(String.format("CashFlow cannot be null.%n"));
		}

		if (valueDate == null) {
			errMsg.append(String.format("Value date cannot be null.%n"));
		}

		if (discountCurveId <= 0) {
			errMsg.append(String.format("The Discount curve id must be positive.%n"));
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

		BigDecimal discountedAmount = PricerUtil.discountAmount(cf.getAmount(), discountCurveId, valueDate,
				cf.getDate(), dcc);
		cf.setDiscountedAmount(discountedAmount);
		BigDecimal discountFactor = PricerUtil.getDiscountFactor(discountCurveId, valueDate, cf.getDate(), null);
		cf.setDiscountFactor(discountFactor);

	}

	/**
	 * Gets the interest rate as of given date and using given curve. Expressed as
	 * follows: 5 for 5%
	 * 
	 * @param curveId the id of the curve used to get the interest rate
	 * @param date    the date for which we get the interest rate
	 * @return the interest rate as of given date and using given curve
	 * @throws PricerException if the curve was not found or if there was an
	 *                         interpolation issue
	 */
	public static BigDecimal getDiscountFactor(long curveId, LocalDate date) throws PricerException {
		// get the interest rate from the curve (curve defined in the
		// pricingparam)
		InterestRateCurveBusinessDelegate interestRateCurveBusinessDelegate = new InterestRateCurveBusinessDelegate();

		if (date == null) {
			throw new PricerException(String.format(DATE_CANNOT_BE_NULL));
		}

		try {
			if (interestRateCurveBusinessDelegate.getInterestRateCurveById(curveId) == null) {
				throw new PricerException(String.format("The curve '%s' must exist in the system.", curveId));
			}
		} catch (TradistaBusinessException tbe) {
			throw new PricerException(tbe.getMessage());
		}

		LocalDate min = date.minus(1, ChronoUnit.YEARS);
		LocalDate max = date.plus(1, ChronoUnit.YEARS);
		List<RatePoint> points;
		try {
			points = interestRateCurveBusinessDelegate.getInterestRateCurvePointsByCurveIdAndDates(curveId, min, max);
		} catch (TradistaBusinessException tbe) {
			tbe.printStackTrace();
			throw new PricerException(tbe.getMessage());
		}
		Map<LocalDate, BigDecimal> pointsMap = new TreeMap<>();
		if (points != null) {
			for (RatePoint point : points) {
				pointsMap.put(point.getDate(), point.getRate());
			}
		}
		BigDecimal value = pointsMap.get(date);
		if (value != null) {
			return value;
		} else // if the interest rate is not present, perform a linear
				// interpolation to estimate it
		{
			LocalDate calcMin = LocalDate.from(date);
			int daysNumberMin = 0;
			BigDecimal valueMin = null;

			while ((calcMin.isAfter(min)) && (valueMin == null)) {
				calcMin = calcMin.minus(1, ChronoUnit.DAYS);
				daysNumberMin++;
				valueMin = pointsMap.get(calcMin);
			}

			if (valueMin != null) {
				LocalDate calcMax = LocalDate.from(date);
				int daysNumberMax = 0;
				BigDecimal valueMax = null;

				while ((calcMax.isBefore(max)) && (valueMax == null)) {
					calcMax = calcMax.plus(1, ChronoUnit.DAYS);
					daysNumberMax++;
					valueMax = pointsMap.get(calcMax);
				}

				if (valueMax != null) {
					BigDecimal progressionByDay = (valueMax.subtract(valueMin)).divide(
							new BigDecimal(daysNumberMin + daysNumberMax),
							configurationBusinessDelegate.getRoundingMode());
					return valueMin.add(progressionByDay.multiply(new BigDecimal(daysNumberMin)));
				} else {
					// No max date
					throw new PricerException(String.format(
							"The curve with id %d doesn't contain an End value for interpolation and it was not possible to interpolate a value at this date: %s.",
							curveId, date));

				}
			} else {
				// No min value found
				throw new PricerException(String.format(
						"The curve with id %d doesn't contain a Start value for interpolation and it was not possible to interpolate a value at this date: %s.",
						curveId, date));
			}
		}
	}

	public static BigDecimal getDiscountFactor(InterestRateCurve curve, LocalDate date) throws PricerException {
		// get the interest rate from the curve (curve defined in the
		// pricingparam)
		InterestRateCurveBusinessDelegate interestRateCurveBusinessDelegate = new InterestRateCurveBusinessDelegate();
		LocalDate min = date.minus(1, ChronoUnit.YEARS);
		LocalDate max = date.plus(1, ChronoUnit.YEARS);
		List<RatePoint> points;
		try {
			points = interestRateCurveBusinessDelegate.getInterestRateCurvePointsByCurveAndDates(curve, min, max);
		} catch (TradistaBusinessException tbe) {
			tbe.printStackTrace();
			throw new PricerException(tbe.getMessage());
		}
		Map<LocalDate, BigDecimal> pointsMap = new TreeMap<>();
		if (points != null) {
			for (RatePoint point : points) {
				pointsMap.put(point.getDate(), point.getRate());
			}
		}
		BigDecimal value = pointsMap.get(date);
		if (value != null) {
			return value;
		} else // if the interest rate is not present, perform a linear
				// interpolation to estimate it
		{
			LocalDate calcMin = LocalDate.from(date);
			int daysNumberMin = 0;
			BigDecimal valueMin = null;

			while ((calcMin.isAfter(min)) && (valueMin == null)) {
				calcMin = calcMin.minus(1, ChronoUnit.DAYS);
				daysNumberMin++;
				valueMin = pointsMap.get(calcMin);
			}

			if (valueMin != null) {
				LocalDate calcMax = LocalDate.from(date);
				int daysNumberMax = 0;
				BigDecimal valueMax = null;

				while ((calcMax.isBefore(max)) && (valueMax == null)) {
					calcMax = calcMax.plus(1, ChronoUnit.DAYS);
					daysNumberMax++;
					valueMax = pointsMap.get(calcMax);
				}

				if (valueMax != null) {
					BigDecimal progressionByDay = (valueMax.subtract(valueMin)).divide(
							new BigDecimal(daysNumberMin + daysNumberMax), configurationBusinessDelegate.getScale(),

							configurationBusinessDelegate.getRoundingMode());
					return valueMin.add(progressionByDay.multiply(new BigDecimal(daysNumberMin)));
				}
			}
		}

		throw new PricerException(
				String.format("'%s' doesn't contain a value and it was not possible to interpolate.", curve));
	}

	public static BigDecimal daysToYear(LocalDate startDate, LocalDate endDate) {
		DayCountConvention dcc = new DayCountConvention(DayCountConvention.ACT_365);
		return daysToYear(dcc, startDate, endDate);
	}

	public static BigDecimal daysToYear(Tenor tenor) {
		int scale = configurationBusinessDelegate.getScale();
		RoundingMode roundingMode = configurationBusinessDelegate.getRoundingMode();
		BigDecimal daysToYear = null;
		if (tenor.equals(Tenor.EIGHTEEN_MONTHS)) {
			daysToYear = BigDecimal.valueOf(1.5);
		}
		if (tenor.equals(Tenor.FIVE_MONTHS)) {
			daysToYear = BigDecimal.valueOf(5).divide(BigDecimal.valueOf(12), scale, roundingMode);
		}
		if (tenor.equals(Tenor.FOUR_MONTHS)) {
			daysToYear = BigDecimal.valueOf(4).divide(BigDecimal.valueOf(12), scale, roundingMode);
		}
		if (tenor.equals(Tenor.ONE_MONTH)) {
			daysToYear = BigDecimal.ONE.divide(BigDecimal.valueOf(12), scale, roundingMode);
		}
		if (tenor.equals(Tenor.ONE_YEAR)) {
			daysToYear = BigDecimal.ONE;
		}
		if (tenor.equals(Tenor.SIX_MONTHS)) {
			daysToYear = BigDecimal.valueOf(0.5);
		}
		if (tenor.equals(Tenor.THREE_MONTHS)) {
			daysToYear = BigDecimal.valueOf(0.25);
		}
		if (tenor.equals(Tenor.TWO_MONTHS)) {
			daysToYear = BigDecimal.ONE.divide(BigDecimal.valueOf(6), scale, roundingMode);
		}
		if (tenor.equals(Tenor.TWO_YEARS)) {
			daysToYear = BigDecimal.valueOf(2);
		}
		return daysToYear;
	}

	public static BigDecimal daysToYear(DayCountConvention dcc, LocalDate startDate, LocalDate endDate) {
		int scale = configurationBusinessDelegate.getScale();
		RoundingMode roundingMode = configurationBusinessDelegate.getRoundingMode();
		if (dcc == null) {
			dcc = new DayCountConvention(DayCountConvention.ACT_365);
		}
		switch (dcc.getName()) {
		case DayCountConvention.ACT_360: {
			BigDecimal diff = BigDecimal.valueOf(ChronoUnit.DAYS.between(startDate, endDate));
			return diff.divide(BigDecimal.valueOf(360), scale, roundingMode);
		}
		case DayCountConvention.ACT_365: {
			BigDecimal diff = BigDecimal.valueOf(ChronoUnit.DAYS.between(startDate, endDate));
			return diff.divide(BigDecimal.valueOf(365), scale, roundingMode);
		}
		case "ACT/366": {
			BigDecimal diff = BigDecimal.valueOf(ChronoUnit.DAYS.between(startDate, endDate));
			return diff.divide(BigDecimal.valueOf(366), scale, roundingMode);
		}
		case "ACT/ACT": {
			BigDecimal diff = BigDecimal.valueOf(ChronoUnit.DAYS.between(startDate, endDate));
			int divider = Year.isLeap(startDate.getYear()) ? 366 : 365;
			return diff.divide(BigDecimal.valueOf(divider), scale, roundingMode);
		}
		case "ACT Fixed/365": {
			BigDecimal diff = BigDecimal.valueOf(ChronoUnit.DAYS.between(startDate, endDate));
			return diff.divide(BigDecimal.valueOf(365), scale, roundingMode);
		}
		case "30/360": {
			return ((BigDecimal.valueOf(360).multiply(BigDecimal.valueOf(endDate.getYear() - startDate.getYear())))
					.add(BigDecimal.valueOf(30)
							.multiply(BigDecimal.valueOf(endDate.getMonthValue() - startDate.getMonthValue())))
					.add(BigDecimal.valueOf(endDate.getDayOfMonth() - startDate.getDayOfMonth())))
					.divide(BigDecimal.valueOf(360), scale, roundingMode);
		}
		case "30E/360": {
			int startDay = Math.min(30, startDate.getDayOfMonth());
			int endDay = Math.min(30, endDate.getDayOfMonth());
			return ((BigDecimal.valueOf(360).multiply(BigDecimal.valueOf(endDate.getYear() - startDate.getYear())))
					.add(BigDecimal.valueOf(30)
							.multiply(BigDecimal.valueOf(endDate.getMonthValue() - startDate.getMonthValue())))
					.add(BigDecimal.valueOf(endDay - startDay))).divide(BigDecimal.valueOf(360), scale, roundingMode);
		}
		}

		return null;
	}

	// The cumulative normal distribution function
	public static BigDecimal cnd(double x) {
		double l;
		BigDecimal k;
		BigDecimal w;
		BigDecimal a1 = new BigDecimal("0.31938153");
		BigDecimal a2 = new BigDecimal("-0.356563782");
		BigDecimal a3 = new BigDecimal("1.781477937");
		BigDecimal a4 = new BigDecimal("-1.821255978");
		BigDecimal a5 = new BigDecimal("1.330274429");
		int scale = configurationBusinessDelegate.getScale();
		RoundingMode roundingMode = configurationBusinessDelegate.getRoundingMode();

		l = Math.abs(x);
		k = BigDecimal.ONE.divide(BigDecimal.ONE.add(new BigDecimal("0.2316419").multiply(BigDecimal.valueOf(l))),
				scale, roundingMode);
		w = BigDecimal.ONE.subtract(BigDecimal.ONE
				.divide(BigDecimal
						.valueOf(Math.sqrt(BigDecimal.valueOf(2).multiply(BigDecimal.valueOf(Math.PI)).doubleValue())),
						scale, roundingMode)
				.multiply(BigDecimal.valueOf(Math.exp(-l * l / 2))).multiply(a1.multiply(k).add(a2.multiply(k.pow(2)))
						.add(a3.multiply(k.pow(3))).add(a4.multiply(k.pow(4))).add(a5.multiply(k.pow(5)))));

		if (x < 0.0) {
			w = BigDecimal.ONE.subtract(w);
		}
		return w;
	}

	public static BigDecimal inverseCnd(double x) {
		NormalDistribution nd = new NormalDistribution(0, 1);
		return BigDecimal.valueOf(nd.inverseCumulativeProbability(x));
	}

	/**
	 * Calculate a forward rate from a curve. Can only be used when the starting
	 * date is in the future.
	 * 
	 * @param curveId   the id of the curve to be used
	 * @param startDate the start date
	 * @param endDate   the end date
	 * @param dcc       the day count convention (by default, it will be ACT/365)
	 * @return a forward rate between start and end date, deduced from the curve.
	 * @throws PricerException           if a value could not been retrieved from
	 *                                   the curve
	 * @throws TradistaBusinessException if the start date is not in the future
	 */
	public static BigDecimal getForwardRate(long curveId, LocalDate startDate, LocalDate endDate,
			DayCountConvention dcc) throws PricerException, TradistaBusinessException {
		LocalDate now = LocalDate.now();
		StringBuilder errMsg = new StringBuilder();
		if (curveId <= 0) {
			errMsg.append(String.format(CURVE_ID_MUST_BE_POSITIVE));
		}
		if (startDate == null) {
			errMsg.append(String.format("The start date is mandatory.%n"));
		} else {
			if (!now.isBefore(startDate)) {
				errMsg.append(String.format(
						"A forward date is deduced from curves and can only be calculated if the starting date is in the future.%n"));
			}
		}
		if (endDate == null) {
			errMsg.append(String.format("End date is mandatory.%n"));
		}
		if (startDate != null && endDate != null) {
			if (!startDate.isBefore(endDate)) {
				errMsg.append(String.format(
						"A forward date can only be calculated if the starting date is before the end date.%n"));
			}
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

		if (dcc == null) {
			dcc = new DayCountConvention(DayCountConvention.ACT_365);
		}
		BigDecimal r1 = PricerUtil.getValueAsOfDateFromCurve(curveId, startDate);
		BigDecimal r2 = PricerUtil.getValueAsOfDateFromCurve(curveId, endDate);
		BigDecimal t2 = PricerUtil.daysToYear(dcc, now, endDate);
		BigDecimal t1 = PricerUtil.daysToYear(dcc, now, startDate);

		return ((r2.multiply(t2)).subtract(r1.multiply(t1))).divide(t2.subtract(t1),
				configurationBusinessDelegate.getRoundingMode());
	}

	public static BigDecimal getFXClosingRate(Currency primaryCurrency, Currency quoteCurrency, LocalDate date,
			long quoteSetId) throws TradistaBusinessException {
		StringBuilder errorMessage = new StringBuilder();
		if (primaryCurrency == null) {
			errorMessage.append(String.format("The primary currency cannot be null.%n"));
		}
		if (quoteCurrency == null) {
			errorMessage.append(String.format("The quote currency cannot be null.%n"));
		}
		if (date == null) {
			errorMessage.append(String.format(DATE_CANNOT_BE_NULL));
		}
		if (quoteSetId <= 0) {
			errorMessage.append(String.format(QUOTE_SET_ID_MUST_BE_POSITIVE));
		}
		if (errorMessage.length() > 0) {
			throw new TradistaBusinessException(errorMessage.toString());
		}
		String fxQuote = "FX." + primaryCurrency.getIsoCode() + "." + quoteCurrency.getIsoCode();
		String fxQuoteInv = "FX." + quoteCurrency.getIsoCode() + "." + primaryCurrency.getIsoCode();

		Map<String, BigDecimal> exchangeRates = PricerUtil.getValuesAsOfDateFromQuote(quoteSetId,
				QuoteType.EXCHANGE_RATE, QuoteValue.CLOSE, date, false, fxQuote, fxQuoteInv);
		if (exchangeRates != null) {
			if (exchangeRates.containsKey(fxQuote)) {
				return exchangeRates.get(fxQuote);
			} else {
				return BigDecimal.ONE.divide(exchangeRates.get(fxQuoteInv), configurationBusinessDelegate.getScale(),
						configurationBusinessDelegate.getRoundingMode());
			}
		}
		return null;
	}

	public static BigDecimal getEquityClosingPrice(String equityIsin, String equityExchangeCode, LocalDate date,
			long quoteSetId) throws TradistaBusinessException {
		StringBuilder errorMessage = new StringBuilder();
		if (StringUtils.isEmpty(equityIsin)) {
			errorMessage.append(String.format("The equity isin is mandatory.%n"));
		}
		if (StringUtils.isEmpty(equityExchangeCode)) {
			errorMessage.append(String.format("The equity exchange code is mandatory.%n"));
		}
		if (date == null) {
			errorMessage.append(String.format(DATE_CANNOT_BE_NULL));
		}
		if (quoteSetId <= 0) {
			errorMessage.append(String.format(QUOTE_SET_ID_MUST_BE_POSITIVE));
		}
		if (errorMessage.length() > 0) {
			throw new TradistaBusinessException(errorMessage.toString());
		}
		String equityPriceQuote = "Equity." + equityIsin + "." + equityExchangeCode;

		Map<String, BigDecimal> exchangeRates = PricerUtil.getValuesAsOfDateFromQuote(quoteSetId,
				QuoteType.EQUITY_PRICE, QuoteValue.CLOSE, date, false, equityPriceQuote);
		if (exchangeRates != null) {
			if (exchangeRates.containsKey(equityPriceQuote)) {
				return exchangeRates.get(equityPriceQuote);
			}
		}
		return null;
	}

	public static BigDecimal convertAmount(BigDecimal amount, Currency sourceCurrency, Currency targetCurrency,
			LocalDate date, long quoteSetId, long fxCurveId) throws TradistaBusinessException {
		StringBuilder errorMessage = new StringBuilder();
		if (amount == null) {
			errorMessage.append(String.format("The amount cannot be null.%n"));
		}
		if (sourceCurrency == null) {
			errorMessage.append(String.format("The source currency cannot be null.%n"));
		}
		if (targetCurrency == null) {
			errorMessage.append(String.format("The target currency cannot be null.%n"));
		}
		if (date == null) {
			errorMessage.append(String.format(DATE_CANNOT_BE_NULL));
		}
		if (errorMessage.length() > 0) {
			throw new TradistaBusinessException(errorMessage.toString());
		}
		return amount.multiply(BigDecimal.ONE.divide(
				PricerUtil.getFXExchangeRate(targetCurrency, sourceCurrency, date, quoteSetId, fxCurveId),
				configurationBusinessDelegate.getScale(), configurationBusinessDelegate.getRoundingMode()));
	}

	public static BigDecimal getFXExchangeRate(Currency primaryCurrency, Currency quoteCurrency, LocalDate date,
			long quoteSetId, long fxCurveId) throws TradistaBusinessException {
		StringBuilder errorMessage = new StringBuilder();
		if (primaryCurrency == null) {
			errorMessage.append(String.format("The primary currency cannot be null.%n"));
		}
		if (quoteCurrency == null) {
			errorMessage.append(String.format("The quote currency cannot be null.%n"));
		}
		if (date == null) {
			errorMessage.append(String.format(DATE_CANNOT_BE_NULL));
		}
		if (errorMessage.length() > 0) {
			throw new TradistaBusinessException(errorMessage.toString());
		}
		if (!primaryCurrency.equals(quoteCurrency)) {
			BigDecimal exchangeRate = null;
			LocalDate now = LocalDate.now();
			if (date.isBefore(now)) {
				exchangeRate = PricerUtil.getFXClosingRate(primaryCurrency, quoteCurrency, date, quoteSetId);
				if (exchangeRate != null) {
					return exchangeRate;
				}
			}
			if (date.equals(now)) {
				if (quoteSetId <= 0) {
					throw new TradistaBusinessException(
							"The quote set id is mandatory when the conversion date is the current date.");
				}
				String fxQuote = "FX." + primaryCurrency.getIsoCode() + "." + quoteCurrency.getIsoCode();
				String fxQuoteInv = "FX." + quoteCurrency.getIsoCode() + "." + primaryCurrency.getIsoCode();
				Map<String, BigDecimal> exchangeRates = PricerUtil.getValuesAsOfDateFromQuote(quoteSetId,
						QuoteType.EXCHANGE_RATE, QuoteValue.CLOSE, date, false, fxQuote, fxQuoteInv);
				if (exchangeRates != null) {
					if (exchangeRates.containsKey(fxQuote)) {
						return exchangeRates.get(fxQuote);
					} else {
						return BigDecimal.ONE.divide(exchangeRates.get(fxQuoteInv),
								configurationBusinessDelegate.getScale(),
								configurationBusinessDelegate.getRoundingMode());
					}
				}

				exchangeRates = PricerUtil.getValuesAsOfDateFromQuote(quoteSetId, QuoteType.EXCHANGE_RATE,
						QuoteValue.LAST, date, false, fxQuote, fxQuoteInv);
				if (exchangeRates != null) {
					if (exchangeRates.containsKey(fxQuote)) {
						return exchangeRates.get(fxQuote);
					} else {
						return BigDecimal.ONE.divide(exchangeRates.get(fxQuoteInv),
								configurationBusinessDelegate.getScale(),
								configurationBusinessDelegate.getRoundingMode());
					}
				}
			}

			if (fxCurveId > 0) {
				try {
					exchangeRate = PricerUtil.getValueAsOfDateFromCurve(fxCurveId, date);
					if (exchangeRate != null) {
						return exchangeRate;
					}
				} catch (PricerException pe) {
				}
			}

			throw new TradistaBusinessException(String.format(
					"It was not possible to get the FX Exchange rate as of %tD from currency %s to currency %s (FX.%s.%s)",
					date, quoteCurrency.getIsoCode(), primaryCurrency.getIsoCode(), primaryCurrency.getIsoCode(),
					quoteCurrency.getIsoCode()));

		}
		return BigDecimal.ONE;
	}

	public static BigDecimal getEquityPrice(String equityIsin, String equityExchangeCode, LocalDate date,
			long quoteSetId) throws TradistaBusinessException {
		StringBuilder errorMessage = new StringBuilder();
		if (StringUtils.isEmpty(equityIsin)) {
			errorMessage.append(String.format("The equity isin is mandatory.%n"));
		}
		if (StringUtils.isEmpty(equityExchangeCode)) {
			errorMessage.append(String.format("The equity exchange code is mandatory.%n"));
		}
		if (quoteSetId <= 0) {
			errorMessage.append(String.format(QUOTE_SET_ID_MUST_BE_POSITIVE));
		}
		if (date == null) {
			errorMessage.append(String.format(DATE_CANNOT_BE_NULL));
		}
		if (errorMessage.length() > 0) {
			throw new TradistaBusinessException(errorMessage.toString());
		}

		BigDecimal equityPrice = null;
		LocalDate now = LocalDate.now();
		if (date.isBefore(now)) {
			equityPrice = PricerUtil.getEquityClosingPrice(equityIsin, equityExchangeCode, date, quoteSetId);
			if (equityPrice != null) {
				return equityPrice;
			}
		}
		if (date.equals(now)) {
			if (quoteSetId <= 0) {
				throw new TradistaBusinessException(
						"The quote set id is mandatory when the conversion date is the current date.");
			}
			String equityPriceQuote = "Equity." + equityIsin + "." + equityExchangeCode;
			Map<String, BigDecimal> equityPriceMap = PricerUtil.getValuesAsOfDateFromQuote(quoteSetId,
					QuoteType.EQUITY_PRICE, QuoteValue.CLOSE, date, false, equityPriceQuote);
			if (equityPriceMap != null) {
				if (equityPriceMap.containsKey(equityPriceQuote)) {
					return equityPriceMap.get(equityPriceQuote);
				}
			}

			equityPriceMap = PricerUtil.getValuesAsOfDateFromQuote(quoteSetId, QuoteType.EQUITY_PRICE, QuoteValue.LAST,
					date, false, equityPriceQuote);
			if (equityPriceMap != null) {
				if (equityPriceMap.containsKey(equityPriceQuote)) {
					return equityPriceMap.get(equityPriceQuote);
				}
			}
		}

		throw new TradistaBusinessException(
				String.format("It was not possible to get the Equity price as of %tD for %s (Equity.%s.%s)", date,
						equityIsin, equityIsin, equityExchangeCode));

	}

	public static BigDecimal getTotalFlowsAmount(List<CashFlow> cfs, Currency valueCurrency, LocalDate valueDate,
			long curveId, DayCountConvention dcc) throws TradistaBusinessException {
		BigDecimal totalFlowsAmount = BigDecimal.ZERO;
		if (cfs != null) {
			for (CashFlow cf : cfs) {
				try {
					BigDecimal cfAmount;
					if (valueDate != null) {
						cfAmount = PricerUtil.discountAmount(cf.getAmount(), curveId, valueDate, cf.getDate(), dcc);
					} else {
						cfAmount = cf.getAmount();
					}

					if (valueCurrency != null) {
						cfAmount = PricerUtil.convertAmount(cfAmount, cf.getCurrency(), valueCurrency, cf.getDate(), 0,
								0);
					}
					totalFlowsAmount = totalFlowsAmount.add(cfAmount);
				} catch (PricerException pe) {
					throw new TradistaBusinessException(pe.getMessage());
				}
			}
		} else {
			throw new TradistaBusinessException(
					"Cannot compute the total flows amount because the cashflows list is null.");
		}
		return totalFlowsAmount;
	}

	/**
	 * Divides dividend by divisor, using scale and rounding mode from Tradista
	 * configuration.
	 * 
	 * @param dividend the dividend
	 * @param divisor  the divisor (should be different from 0)
	 * @return dividend/divisor (using scale and rounding mode from Tradista
	 *         configuration)
	 */
	public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
		return dividend.divide(divisor, configurationBusinessDelegate.getScale(),
				configurationBusinessDelegate.getRoundingMode());
	}

}