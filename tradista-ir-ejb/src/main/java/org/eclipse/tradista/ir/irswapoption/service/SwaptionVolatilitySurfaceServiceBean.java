package org.eclipse.tradista.ir.irswapoption.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
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
import org.eclipse.tradista.core.marketdata.generationalgorithm.SurfaceGenerationAlgorithm;
import org.eclipse.tradista.core.marketdata.interpolator.MultivariateInterpolator;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.RatePoint;
import org.eclipse.tradista.core.marketdata.model.SurfacePoint;
import org.eclipse.tradista.core.marketdata.service.SurfaceBusinessDelegate;
import org.eclipse.tradista.core.marketdata.service.VolatilitySurfaceFilteringInterceptor;
import org.eclipse.tradista.ir.irswapoption.model.IRSwapOptionTrade;
import org.eclipse.tradista.ir.irswapoption.model.SwaptionVolatilitySurface;
import org.eclipse.tradista.ir.irswapoption.persistence.SwaptionVolatilitySurfaceSQL;
import org.eclipse.tradista.ir.irswapoption.service.SwaptionVolatilitySurfaceService;

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
public class SwaptionVolatilitySurfaceServiceBean implements SwaptionVolatilitySurfaceService {

	@Interceptors(VolatilitySurfaceFilteringInterceptor.class)
	@Override
	public Set<SwaptionVolatilitySurface> getAllSwaptionVolatilitySurfaces() {
		return SwaptionVolatilitySurfaceSQL.getAllSwaptionVolatilitySurfaces();
	}

	@Override
	public SwaptionVolatilitySurface getSwaptionVolatilitySurfaceByName(String name) {
		return SwaptionVolatilitySurfaceSQL.getSwaptionVolatilitySurfaceByName(name);
	}

	@Interceptors(VolatilitySurfaceFilteringInterceptor.class)
	@Override
	public SwaptionVolatilitySurface getSwaptionVolatilitySurfaceById(long id) {
		return SwaptionVolatilitySurfaceSQL.getSwaptionVolatilitySurfaceById(id);
	}

	@Override
	public List<RatePoint> getInterestRateCurvePointsByCurveIdAndDate(long id, Integer year, Integer month) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean saveSwaptionVolatilitySurfacePoints(long id, List<SurfacePoint<Float, Float, Float>> surfacePoints,
			Float optionLifeTime, Float swapLifetime) {
		// TODO Auto-generated method stub
		return SwaptionVolatilitySurfaceSQL.saveSwaptionVolatilitySurfacePoints(id, surfacePoints, optionLifeTime,
				swapLifetime);
	}

	@Interceptors(VolatilitySurfaceFilteringInterceptor.class)
	@Override
	public boolean deleteSwaptionVolatilitySurface(long surfaceId) throws TradistaBusinessException {
		return SwaptionVolatilitySurfaceSQL.deleteSwaptionVolatilitySurface(surfaceId);
	}

	@Override
	public List<RatePoint> getInterestRateCurvePointsByCurveNameAndDates(String curveName, Date min, Date max) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RatePoint> getInterestRateCurvePointsByCurveAndDates(InterestRateCurve curve, Date min, Date max) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigDecimal getVolatility(String surfaceName, int timeToMaturity, int tenor) {
		return SwaptionVolatilitySurfaceSQL.getVolatilityBySurfaceNameTimeToMaturityAndTenor(surfaceName,
				timeToMaturity, tenor);
	}

	@Override
	public List<SurfacePoint<Number, Number, Number>> getSwaptionVolatilitySurfacePointsBySurfaceIdOptionAndSwapLifetimes(
			long currentSwaptionVolatilitySurfaceId, Float optionLifetime, Float swapLifetime) {
		return SwaptionVolatilitySurfaceSQL.getSwaptionVolatilitySurfacePointsBySurfaceIdOptionAndSwapLifetimes(
				currentSwaptionVolatilitySurfaceId, optionLifetime, swapLifetime);
	}

	@Interceptors({ IRSwapOptionTradeProductScopeFilteringInterceptor.class,
			VolatilitySurfaceFilteringInterceptor.class })
	@Override
	public long saveSwaptionVolatilitySurface(SwaptionVolatilitySurface surface) throws TradistaBusinessException {
		if (surface.getId() == 0) {
			checkSurfaceExistence(surface);
		} else {
			SwaptionVolatilitySurface oldSurface = SwaptionVolatilitySurfaceSQL
					.getSwaptionVolatilitySurfaceById(surface.getId());
			if (!oldSurface.getName().equals(surface.getName())
					|| !oldSurface.getProcessingOrg().equals(surface.getProcessingOrg())) {
				checkSurfaceExistence(surface);
			}
		}
		return SwaptionVolatilitySurfaceSQL.saveSwaptionVolatilitySurface(surface);
	}

	private void checkSurfaceExistence(SwaptionVolatilitySurface surface) throws TradistaBusinessException {
		if (new SurfaceBusinessDelegate().surfaceExists(surface, IRSwapOptionTrade.IR_SWAP_OPTION)) {
			throw new TradistaBusinessException(
					String.format("An %s volatility surface named %s already exists for the PO %s.",
							IRSwapOptionTrade.IR_SWAP_OPTION, surface.getName(), surface.getProcessingOrg()));
		}
	}

	@Override
	public List<SurfacePoint<Integer, Integer, BigDecimal>> generate(String algorithm, String interpolator,
			String instance, LocalDate quoteDate, QuoteSet quoteSet, List<Long> quoteIds) {
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
		if (!getAllInstances().contains(instance)) {
			throw new IllegalArgumentException("The '" + instance + "' instance doesn't exist.");
		}
		// Check quoteids are valid
		if (quoteIds == null || quoteIds.isEmpty()) {
			throw new IllegalArgumentException("The quote Ids list is null or empty.");
		}

		// Get the generation algorithm
		SurfaceGenerationAlgorithm genAlgorithm = TradistaUtil.getInstance(SurfaceGenerationAlgorithm.class,
				MarketDataConstants.GENERATION_ALGORITHM_PACKAGE + "." + algorithm);
		// Get the interpolator
		MultivariateInterpolator interpolatorObject = TradistaUtil.getInstance(MultivariateInterpolator.class,
				MarketDataConstants.INTERPOLATOR_PACKAGE + "." + interpolator);

		List<SurfacePoint<Number, Number, Number>> surfacePoints = genAlgorithm.generate("IR", instance, quoteIds,
				quoteDate, quoteSet, interpolatorObject);

		return transform(surfacePoints);
	}

	private List<SurfacePoint<Integer, Integer, BigDecimal>> transform(
			List<SurfacePoint<Number, Number, Number>> surfacePoints) {
		List<SurfacePoint<Integer, Integer, BigDecimal>> result;
		if (surfacePoints == null) {
			return null;
		}

		result = new ArrayList<SurfacePoint<Integer, Integer, BigDecimal>>(surfacePoints.size());

		if (surfacePoints.isEmpty()) {
			return result;
		}

		for (SurfacePoint<Number, Number, Number> point : surfacePoints) {
			SurfacePoint<Integer, Integer, BigDecimal> pt = new SurfacePoint<Integer, Integer, BigDecimal>(
					(Integer) point.getxAxis(), (Integer) point.getyAxis(), (BigDecimal) point.getzAxis());
			result.add(pt);
		}
		return result;
	}

	@Override
	public Set<String> getAllGenerationAlgorithms() {
		return TradistaUtil.getAvailableNames(SurfaceGenerationAlgorithm.class,
				MarketDataConstants.GENERATION_ALGORITHM_PACKAGE);
	}

	@Override
	public Set<String> getAllInterpolators() {
		return TradistaUtil.getAvailableNames(MultivariateInterpolator.class, MarketDataConstants.INTERPOLATOR_PACKAGE);
	}

	@Override
	public Set<String> getAllInstances() {
		Set<String> instances = new HashSet<String>();
		instances.add("CLOSE");
		instances.add("OPEN");
		instances.add("BID");
		instances.add("ASK");
		instances.add("MID");
		return instances;
	}

	@Override
	public List<SurfacePoint<Integer, Integer, BigDecimal>> getSwaptionVolatilitySurfacePointsBySurfaceId(
			long volatilitySurfaceId) {
		return SwaptionVolatilitySurfaceSQL.getSwaptionVolatilitySurfacePointsBySurfaceId(volatilitySurfaceId);
	}
}