package org.eclipse.tradista.core.marketdata.service;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.marketdata.model.RatePoint;
import org.eclipse.tradista.core.marketdata.validator.DefaultFXCurveValidator;
import org.eclipse.tradista.core.marketdata.validator.FXCurveValidator;

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

public class FXCurveBusinessDelegate {

	private FXCurveService fxCurveService;

	private FXCurveValidator fxCurveValidator;

	public FXCurveBusinessDelegate() {
		fxCurveService = TradistaServiceLocator.getInstance().getFXCurveService();
		fxCurveValidator = new DefaultFXCurveValidator();
	}

	public Set<FXCurve> getAllFXCurves() {
		return SecurityUtil.run(() -> fxCurveService.getAllFXCurves());
	}

	public List<RatePoint> getFXCurvePointsByCurveIdAndDate(long curveId, Year year, Month month)
			throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (curveId <= 0) {
			errMsg.append(String.format("The curve id (%s) must be positive.%n", curveId));
		}
		if (year == null) {
			errMsg.append(String.format("The year cannot be null.%n"));
		}
		if (month == null) {
			errMsg.append(String.format("The month cannot be null.%n"));
		}
		return SecurityUtil.runEx(() -> fxCurveService.getFXCurvePointsByCurveIdAndDate(curveId, year, month));
	}

	public boolean saveFXCurvePoints(long curveId, List<RatePoint> ratePoints, Year year, Month month)
			throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (curveId <= 0) {
			errMsg.append(String.format("The curve id (%s) must be positive.%n", curveId));
		}
		if (ratePoints == null) {
			errMsg.append(String.format("The rate points list cannot be null.%n"));
		} else {
			if (ratePoints.isEmpty()) {
				errMsg.append(String.format("The rate points list cannot be empty.%n"));
			} else {
				boolean found = false;
				for (RatePoint ratePoint : ratePoints) {
					if (ratePoint != null && ratePoint.getRate() != null) {
						found = true;
						break;
					}
				}
				if (!found) {
					errMsg.append(String.format("The rate points list must at least contain one rate value.%n"));
				}
			}
		}
		if (year == null) {
			errMsg.append(String.format("The year cannot be null.%n"));
		}
		if (month == null) {
			errMsg.append(String.format("The month cannot be null.%n"));
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil.runEx(() -> fxCurveService.saveFXCurvePoints(curveId, ratePoints, year, month));
	}

	public boolean deleteFXCurve(long curveId) throws TradistaBusinessException {
		if (curveId <= 0) {
			throw new TradistaBusinessException("The curve id must be positive.");
		}
		return SecurityUtil.runEx(() -> fxCurveService.deleteFXCurve(curveId));
	}

	public List<RatePoint> getFXCurvePointsByCurveIdAndDates(long curveId, LocalDate min, LocalDate max) {
		return SecurityUtil.run(() -> fxCurveService.getFXCurvePointsByCurveIdAndDates(curveId, min, max));
	}

	public long saveFXCurve(FXCurve curve) throws TradistaBusinessException {
		fxCurveValidator.validateCurve(curve);
		return SecurityUtil.runEx(() -> fxCurveService.saveFXCurve(curve));
	}

	public List<RatePoint> getFXCurvePointsByCurveId(long curveId) {
		return SecurityUtil.run(() -> fxCurveService.getFXCurvePointsByCurveId(curveId));
	}

	public List<RatePoint> generate(String algorithm, String interpolator, String instance, LocalDate quoteDate,
			QuoteSet quoteSet, Currency primaryCurrency, Currency quoteCurrency,
			InterestRateCurve primaryCurrencyIRCurve, InterestRateCurve quoteCurrencyIRCurve)
			throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (algorithm == null || algorithm.isEmpty()) {
			errMsg.append(String.format("The algorithm is mandatory.%n"));
		}
		if (instance == null || instance.isEmpty()) {
			errMsg.append(String.format("The instance is mandatory.%n"));
		}
		if (quoteDate == null) {
			errMsg.append(String.format("The quote date is mandatory.%n"));
		}
		if (quoteSet == null) {
			errMsg.append(String.format("The quote set is mandatory.%n"));
		}
		if (primaryCurrency == null) {
			errMsg.append(String.format("The primary currency is mandatory.%n"));
		}
		if (quoteCurrency == null) {
			errMsg.append(String.format("The quote currency is mandatory.%n"));
		}
		if (primaryCurrencyIRCurve == null) {
			errMsg.append(String.format("The primary currency IR curve is mandatory.%n"));
		}
		if (quoteCurrencyIRCurve == null) {
			errMsg.append(String.format("The quote currency IR curve is mandatory.%n"));
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil.runEx(() -> fxCurveService.generate(algorithm, interpolator, instance, quoteDate, quoteSet,
				primaryCurrency, quoteCurrency, primaryCurrencyIRCurve, quoteCurrencyIRCurve));
	}

	public Set<String> getAllGenerationAlgorithms() {
		return SecurityUtil.run(() -> fxCurveService.getAllGenerationAlgorithms());
	}

	public Set<String> getAllInterpolators() {
		return SecurityUtil.run(() -> fxCurveService.getAllInterpolators());
	}

	public Set<String> getAllInstances() {
		Set<String> instances = new HashSet<String>();
		instances.add(QuoteValue.CLOSE);
		instances.add(QuoteValue.OPEN);
		instances.add(QuoteValue.BID);
		instances.add(QuoteValue.ASK);
		instances.add("Mid");
		return instances;
	}

	public FXCurve getFXCurveById(long id) {
		return SecurityUtil.run(() -> fxCurveService.getFXCurveById(id));
	}

}