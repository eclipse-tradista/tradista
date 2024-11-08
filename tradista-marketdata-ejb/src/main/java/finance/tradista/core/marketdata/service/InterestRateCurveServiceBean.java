package org.eclipse.tradista.core.marketdata.service;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

import org.jboss.ejb3.annotation.SecurityDomain;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.TradistaUtil;
import org.eclipse.tradista.core.marketdata.constants.MarketDataConstants;
import org.eclipse.tradista.core.marketdata.generationalgorithm.InterestRateCurveGenerationAlgorithm;
import org.eclipse.tradista.core.marketdata.interpolator.UnivariateInterpolator;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.RatePoint;
import org.eclipse.tradista.core.marketdata.model.ZeroCouponCurve;
import org.eclipse.tradista.core.marketdata.persistence.InterestRateCurveSQL;
import org.eclipse.tradista.core.marketdata.service.CurveFilteringInterceptor;
import org.eclipse.tradista.core.marketdata.service.InterestRateCurveBusinessDelegate;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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
public class InterestRateCurveServiceBean implements InterestRateCurveService {

	@Interceptors(CurveFilteringInterceptor.class)
	@Override
	public Set<InterestRateCurve> getAllInterestRateCurves() {
		return InterestRateCurveSQL.getAllInterestRateCurves();
	}

	@Override
	public Set<ZeroCouponCurve> getAllZeroCouponCurves() {
		return InterestRateCurveSQL.getAllZeroCouponCurves();
	}

	@Interceptors(CurveFilteringInterceptor.class)
	public List<RatePoint> getInterestRateCurvePointsByCurveIdAndDate(long curveId, Year year, Month month)
			throws TradistaBusinessException {
		return InterestRateCurveSQL.getAllInterestRateCurvePointsByCurveIdAndDate(curveId, year, month);
	}

	@Interceptors(CurveFilteringInterceptor.class)
	@Override
	public boolean saveInterestRateCurvePoints(long id, List<RatePoint> ratePoints, Year year, Month month)
			throws TradistaBusinessException {
		return InterestRateCurveSQL.saveInterestRateCurvePoints(id, ratePoints, year, month);
	}

	@Interceptors(CurveFilteringInterceptor.class)
	@Override
	public boolean deleteInterestRateCurve(long curveId) throws TradistaBusinessException {
		return InterestRateCurveSQL.deleteInterestRateCurve(curveId);
	}

	@Override
	public List<RatePoint> getInterestRateCurvePointsByCurveIdAndDates(long curveId, LocalDate min, LocalDate max) {
		return InterestRateCurveSQL.getInterestRateCurvePointsByCurveIdAndDates(curveId, min, max);
	}

	@Override
	public List<RatePoint> getInterestRateCurvePointsByCurveAndDates(InterestRateCurve curve, LocalDate min,
			LocalDate max) {
		return InterestRateCurveSQL.getInterestRateCurvePointsByCurveIdAndDates(curve.getId(), min, max);
	}

	@Override
	public InterestRateCurve getInterestRateCurveByName(String curveName) {
		return InterestRateCurveSQL.getInterestRateCurveByName(curveName);
	}

	@Override
	public Set<String> getAllInterestRateCurveTypes() {
		Set<String> types = new HashSet<String>();
		types.add(InterestRateCurve.INTEREST_RATE_CURVE);
		types.add(ZeroCouponCurve.ZERO_COUPON_CURVE);
		return types;
	}

	@Interceptors(CurveFilteringInterceptor.class)
	@Override
	public long saveInterestRateCurve(InterestRateCurve curve) throws TradistaBusinessException {
		if (curve.getId() == 0) {
			checkCurveExistence(curve);
		} else {
			InterestRateCurve oldInterestRateCurve = InterestRateCurveSQL.getInterestRateCurveById(curve.getId());
			if (!oldInterestRateCurve.getName().equals(oldInterestRateCurve.getName())
					|| !oldInterestRateCurve.getProcessingOrg().equals(oldInterestRateCurve.getProcessingOrg())) {
				checkCurveExistence(curve);
			}
		}
		return InterestRateCurveSQL.saveInterestRateCurve(curve);
	}

	private void checkCurveExistence(InterestRateCurve curve) throws TradistaBusinessException {
		if (InterestRateCurveSQL.getInterestRateCurveByNameAndPo(curve.getName(),
				curve.getProcessingOrg() == null ? 0 : curve.getProcessingOrg().getId()) != null) {
			String errMsg;
			if (curve.getProcessingOrg() == null) {
				errMsg = "A global interest rate curve named %s already exists in the system.";
			} else {
				errMsg = "An interest rate curve named %s already exists in the system for the PO %s.";
			}
			throw new TradistaBusinessException(String.format(errMsg, curve.getName(), curve.getProcessingOrg()));
		}
	}

	@Override
	public List<RatePoint> getInterestRateCurvePointsByCurveId(long curveId) {
		return InterestRateCurveSQL.getInterestRateCurvePointsByCurveId(curveId);
	}

	@Override
	public List<RatePoint> generate(String algorithm, String interpolator, String instance, LocalDate quoteDate,
			QuoteSet quoteSet, List<Long> quoteIds) {

		// Controls
		// Check if algorithm is supported
		if (!getAllGenerationAlgorithms().contains(algorithm)) {
			throw new IllegalArgumentException("The '" + algorithm + "' algorithm doesn't exist.");
		}
		// Check interpolator is supported
		if (!getAllInterpolators().contains(interpolator)) {
			throw new IllegalArgumentException("The '" + interpolator + "' interpolator doesn't exist.");
		}
		// Check instance is supported
		if (new InterestRateCurveBusinessDelegate().getAllInstances().contains(instance)) {
			throw new IllegalArgumentException("The '" + instance + "' instance doesn't exist.");
		}
		// Check quoteids are valid
		if (quoteIds == null || quoteIds.isEmpty()) {
			throw new IllegalArgumentException("The quote Ids list is null or empty.");
		}

		// Get the generation algorithm
		InterestRateCurveGenerationAlgorithm genAlgorithm = TradistaUtil.getInstance(
				InterestRateCurveGenerationAlgorithm.class,
				MarketDataConstants.GENERATION_ALGORITHM_PACKAGE + "." + algorithm);
		// Get the interpolator
		UnivariateInterpolator interpolatorObject = TradistaUtil.getInstance(UnivariateInterpolator.class,
				MarketDataConstants.INTERPOLATOR_PACKAGE + "." + interpolator);

		List<RatePoint> ratePoints = genAlgorithm.generate(instance, quoteIds, quoteDate, quoteSet, interpolatorObject);

		return ratePoints;
	}

	@Override
	public Set<String> getAllGenerationAlgorithms() {
		return TradistaUtil.getAvailableNames(InterestRateCurveGenerationAlgorithm.class,
				MarketDataConstants.GENERATION_ALGORITHM_PACKAGE);
	}

	@Override
	public Set<String> getAllInterpolators() {
		return TradistaUtil.getAvailableNames(UnivariateInterpolator.class, MarketDataConstants.INTERPOLATOR_PACKAGE);
	}

	@Interceptors(CurveFilteringInterceptor.class)
	@Override
	public InterestRateCurve getInterestRateCurveById(long id) {
		return InterestRateCurveSQL.getInterestRateCurveById(id);
	}

}