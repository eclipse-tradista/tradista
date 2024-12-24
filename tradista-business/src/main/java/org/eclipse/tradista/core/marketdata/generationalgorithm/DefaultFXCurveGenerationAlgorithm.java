package org.eclipse.tradista.core.marketdata.generationalgorithm;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.marketdata.interpolator.UnivariateInterpolator;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.RatePoint;
import org.eclipse.tradista.core.marketdata.service.InterestRateCurveBusinessDelegate;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.util.PricerUtil;

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

public class DefaultFXCurveGenerationAlgorithm implements FXCurveGenerationAlgorithm {

	private ConfigurationBusinessDelegate configurationBusinessDelegate;

	public DefaultFXCurveGenerationAlgorithm() {
		configurationBusinessDelegate = new ConfigurationBusinessDelegate();
	}

	@Override
	public List<RatePoint> generate(String instance, LocalDate quoteDate, QuoteSet quoteSet, Currency primaryCurrency,
			Currency quoteCurrency, InterestRateCurve primaryCurrencyIRCurve, InterestRateCurve quoteCurrencyIRCurve,
			UnivariateInterpolator interpolator) throws TradistaBusinessException {

		InterestRateCurveBusinessDelegate interestRateCurveBusinessDelegate = new InterestRateCurveBusinessDelegate();

		List<RatePoint> ratePoints = new ArrayList<RatePoint>();

		String quoteName = "FX." + primaryCurrency.getIsoCode() + "." + quoteCurrency.getIsoCode();
		BigDecimal quoteValue = PricerUtil.getValueAsOfDateFromQuote(quoteName, quoteSet.getId(),
				QuoteType.EXCHANGE_RATE, instance, quoteDate);

		if (quoteValue == null) {
			throw new TradistaBusinessException(String
					.format("The quote value cannot be found as of %tD for the quote '%s'", quoteDate, quoteName));
		}

		List<RatePoint> pCurvePoints = interestRateCurveBusinessDelegate
				.getInterestRateCurvePointsByCurveId(primaryCurrencyIRCurve.getId());
		List<RatePoint> qCurvePoints = interestRateCurveBusinessDelegate
				.getInterestRateCurvePointsByCurveId(quoteCurrencyIRCurve.getId());

		if (pCurvePoints == null || pCurvePoints.isEmpty()) {
			throw new TradistaBusinessException(
					String.format("The interest rate curve %s has no points.", primaryCurrencyIRCurve.getName()));
		}

		if (qCurvePoints == null || qCurvePoints.isEmpty()) {
			throw new TradistaBusinessException(
					String.format("The interest rate curve %s has no points.", quoteCurrencyIRCurve.getName()));
		}

		Collections.sort(pCurvePoints);
		Collections.sort(qCurvePoints);
		LocalDate lastPDate = pCurvePoints.get(pCurvePoints.size() - 1).getDate();
		LocalDate lastQDate = qCurvePoints.get(qCurvePoints.size() - 1).getDate();
		LocalDate minLastDate = lastPDate;

		if (lastQDate.isBefore(lastPDate)) {
			minLastDate = lastQDate;
		}

		if (!minLastDate.isAfter(quoteDate)) {
			throw new TradistaBusinessException(String.format(
					"Min(last date of primary IR Curve, last date of quote IR Curve) (%tD) must be after the quote date (%tD).",
					minLastDate, quoteDate));
		}

		LocalDate varDate = quoteDate.plusDays(1);

		while (!varDate.isAfter(minLastDate)) {
			BigDecimal pCurveValue;
			try {
				pCurveValue = PricerUtil.getValueAsOfDateFromCurve(primaryCurrencyIRCurve.getId(), varDate);

				BigDecimal qCurveValue = PricerUtil.getValueAsOfDateFromCurve(quoteCurrencyIRCurve.getId(), varDate);

				BigDecimal value = quoteValue.multiply((qCurveValue.add(BigDecimal.ONE))
						.divide(pCurveValue.add(BigDecimal.ONE), configurationBusinessDelegate.getRoundingMode()));

				RatePoint ratePoint = new RatePoint(varDate, value);
				ratePoints.add(ratePoint);
				varDate = varDate.plusDays(1);
			} catch (PricerException e) {
				throw new TradistaBusinessException(e.getMessage());
			}
		}

		return ratePoints;
	}

}