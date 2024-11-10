package org.eclipse.tradista.core.marketdata.service;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.TradistaUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.marketdata.constants.MarketDataConstants;
import org.eclipse.tradista.core.marketdata.generationalgorithm.FXCurveGenerationAlgorithm;
import org.eclipse.tradista.core.marketdata.interpolator.UnivariateInterpolator;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.RatePoint;
import org.eclipse.tradista.core.marketdata.persistence.FXCurveSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

/********************************************************************************
 * Copyright (c) 2016 Olivier Asuncion
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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class FXCurveServiceBean implements FXCurveService {

	@Interceptors(CurveFilteringInterceptor.class)
	@Override
	public Set<FXCurve> getAllFXCurves() {
		return FXCurveSQL.getAllFXCurves();
	}

	@Interceptors(CurveFilteringInterceptor.class)
	public List<RatePoint> getFXCurvePointsByCurveIdAndDate(long curveId, Year year, Month month)
			throws TradistaBusinessException {
		return FXCurveSQL.getAllFXCurvePointsByCurveIdAndDate(curveId, year, month);
	}

	@Interceptors(CurveFilteringInterceptor.class)
	@Override
	public boolean saveFXCurvePoints(long id, List<RatePoint> ratePoints, Year year, Month month)
			throws TradistaBusinessException {
		return FXCurveSQL.saveFXCurvePoints(id, ratePoints, year, month);
	}

	@Interceptors(CurveFilteringInterceptor.class)
	@Override
	public boolean deleteFXCurve(long curveId) throws TradistaBusinessException {
		return FXCurveSQL.deleteFXCurve(curveId);
	}

	@Override
	public List<RatePoint> getFXCurvePointsByCurveIdAndDates(long curveId, LocalDate min, LocalDate max) {
		return FXCurveSQL.getFXCurvePointsByCurveIdAndDates(curveId, min, max);
	}

	@Interceptors(CurveFilteringInterceptor.class)
	@Override
	public long saveFXCurve(FXCurve curve) throws TradistaBusinessException {
		if (curve.getId() == 0) {
			checkCurveExistence(curve);
		} else {
			FXCurve oldFXCurve = FXCurveSQL.getFXCurveById(curve.getId());
			if (!oldFXCurve.getName().equals(oldFXCurve.getName())) {
				checkCurveExistence(curve);
			}
		}
		return FXCurveSQL.saveFXCurve(curve);
	}

	private void checkCurveExistence(FXCurve curve) throws TradistaBusinessException {
		if (FXCurveSQL.getFXCurveByNameAndPo(curve.getName(),
				curve.getProcessingOrg() == null ? 0 : curve.getProcessingOrg().getId()) != null) {
			String errMsg;
			if (curve.getProcessingOrg() == null) {
				errMsg = "A global fx curve named %s already exists in the system.";
			} else {
				errMsg = "A fx curve named %s already exists in the system for the PO %s.";
			}
			throw new TradistaBusinessException(String.format(errMsg, curve.getName(), curve.getProcessingOrg()));
		}
	}

	@Override
	public List<RatePoint> getFXCurvePointsByCurveId(long curveId) {
		return FXCurveSQL.getFXCurvePointsByCurveId(curveId);
	}

	@Override
	public List<RatePoint> generate(String algorithm, String interpolator, String instance, LocalDate quoteDate,
			QuoteSet quoteSet, Currency primaryCurrency, Currency quoteCurrency,
			InterestRateCurve primaryCurrencyIRCurve, InterestRateCurve quoteCurrencyIRCurve)
			throws TradistaBusinessException {

		// Controls
		// Check if algorithm is supported
		if (!getAllGenerationAlgorithms().contains(algorithm)) {
			throw new IllegalArgumentException("The '" + algorithm + "' algorithm doesn't exist.");
		}
		if (!StringUtils.isEmpty(interpolator)) {
			// Check interpolator is supported
			if (!getAllInterpolators().contains(interpolator)) {
				throw new IllegalArgumentException("The '" + interpolator + "' interpolator doesn't exist.");
			}
		}
		// Check instance is supported
		if (!new FXCurveBusinessDelegate().getAllInstances().contains(instance)) {
			throw new IllegalArgumentException("The '" + instance + "' instance doesn't exist.");
		}

		// Get the generation algorithm
		FXCurveGenerationAlgorithm genAlgorithm = TradistaUtil.getInstance(FXCurveGenerationAlgorithm.class,
				MarketDataConstants.GENERATION_ALGORITHM_PACKAGE + "." + algorithm);
		// Get the interpolator
		UnivariateInterpolator interpolatorObject = TradistaUtil.getInstance(UnivariateInterpolator.class,
				MarketDataConstants.INTERPOLATOR_PACKAGE + "." + interpolator);

		List<RatePoint> ratePoints = genAlgorithm.generate(instance, quoteDate, quoteSet, primaryCurrency,
				quoteCurrency, primaryCurrencyIRCurve, quoteCurrencyIRCurve, interpolatorObject);

		return ratePoints;
	}

	@Override
	public Set<String> getAllGenerationAlgorithms() {
		return TradistaUtil.getAvailableNames(FXCurveGenerationAlgorithm.class,
				MarketDataConstants.GENERATION_ALGORITHM_PACKAGE);
	}

	@Override
	public Set<String> getAllInterpolators() {
		return TradistaUtil.getAvailableNames(UnivariateInterpolator.class, MarketDataConstants.INTERPOLATOR_PACKAGE);
	}

	@Interceptors(CurveFilteringInterceptor.class)
	@Override
	public FXCurve getFXCurveById(long id) {
		return FXCurveSQL.getFXCurveById(id);
	}

}