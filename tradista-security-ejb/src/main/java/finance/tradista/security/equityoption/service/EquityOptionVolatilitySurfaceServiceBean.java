package org.eclipse.tradista.security.equityoption.service;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.SurfacePoint;
import org.eclipse.tradista.core.marketdata.service.SurfaceBusinessDelegate;
import org.eclipse.tradista.core.marketdata.service.VolatilitySurfaceFilteringInterceptor;
import org.eclipse.tradista.security.equityoption.model.EquityOption;
import org.eclipse.tradista.security.equityoption.model.EquityOptionVolatilitySurface;
import org.eclipse.tradista.security.equityoption.persistence.EquityOptionVolatilitySurfaceSQL;

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
public class EquityOptionVolatilitySurfaceServiceBean implements EquityOptionVolatilitySurfaceService {

	@Interceptors(VolatilitySurfaceFilteringInterceptor.class)
	@Override
	public Set<EquityOptionVolatilitySurface> getAllEquityOptionVolatilitySurfaces() {
		return EquityOptionVolatilitySurfaceSQL.getAllEquityOptionVolatilitySurfaces();
	}

	@Interceptors(VolatilitySurfaceFilteringInterceptor.class)
	@Override
	public EquityOptionVolatilitySurface getEquityOptionVolatilitySurfaceById(long id) {
		return EquityOptionVolatilitySurfaceSQL.getEquityOptionVolatilitySurfaceById(id);
	}

	@Interceptors(VolatilitySurfaceFilteringInterceptor.class)
	@Override
	public boolean deleteEquityOptionVolatilitySurface(long surfaceId) throws TradistaBusinessException {
		return EquityOptionVolatilitySurfaceSQL.deleteEquityOptionVolatilitySurface(surfaceId);
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
	public EquityOptionVolatilitySurface getEquityOptionVolatilitySurfaceByName(String name) {
		return EquityOptionVolatilitySurfaceSQL.getEquityOptionVolatilitySurfaceByName(name);
	}

	@Override
	public boolean saveEquityOptionVolatilitySurfacePoints(long id,
			List<SurfacePoint<Long, BigDecimal, BigDecimal>> ratePoints, Long optionExpiry, BigDecimal delta) {
		// TODO Auto-generated method stub
		return false;
	}

	@Interceptors({ EquityOptionProductScopeFilteringInterceptor.class, VolatilitySurfaceFilteringInterceptor.class })
	@Override
	public long saveEquityOptionVolatilitySurface(EquityOptionVolatilitySurface surface)
			throws TradistaBusinessException {
		if (surface.getId() == 0) {
			checkSurfaceExistence(surface);
		} else {
			EquityOptionVolatilitySurface oldSurface = EquityOptionVolatilitySurfaceSQL
					.getEquityOptionVolatilitySurfaceById(surface.getId());
			if (!oldSurface.getName().equals(surface.getName())
					|| !oldSurface.getProcessingOrg().equals(surface.getProcessingOrg())) {
				checkSurfaceExistence(surface);
			}
		}
		return EquityOptionVolatilitySurfaceSQL.saveEquityOptionVolatilitySurface(surface);
	}

	private void checkSurfaceExistence(EquityOptionVolatilitySurface surface) throws TradistaBusinessException {
		if (new SurfaceBusinessDelegate().surfaceExists(surface, EquityOption.EQUITY_OPTION)) {
			throw new TradistaBusinessException(
					String.format("An %s volatility surface named %s already exists for the PO %s.",
							EquityOption.EQUITY_OPTION, surface.getName(), surface.getProcessingOrg()));
		}
	}

	@Override
	public BigDecimal getVolatility(String volatilitySurfaceName, int optionExpiry) {
		return EquityOptionVolatilitySurfaceSQL.getVolatility(volatilitySurfaceName, optionExpiry);
	}

	@Override
	public List<SurfacePoint<Number, Number, Number>> getEquityOptionVolatilitySurfacePointsBySurfaceIdOptionExpiryAndStrike(
			long surfaceId, Long optionExpiry, BigDecimal strike) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SurfacePoint<Integer, BigDecimal, BigDecimal>> generate(String algorithm, String interpolator,
			String instance, LocalDate quoteDate, QuoteSet quoteSet, List<String> quoteNames,
			List<BigDecimal> strikes) {
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
		if (quoteNames == null || quoteNames.isEmpty()) {
			throw new IllegalArgumentException("The quote Names list is null or empty.");
		}

		// Get the generation algorithm
		SurfaceGenerationAlgorithm genAlgorithm = TradistaUtil.getInstance(SurfaceGenerationAlgorithm.class,
				MarketDataConstants.GENERATION_ALGORITHM_PACKAGE + "." + algorithm);
		// Get the interpolator
		MultivariateInterpolator interpolatorObject = TradistaUtil.getInstance(MultivariateInterpolator.class,
				MarketDataConstants.INTERPOLATOR_PACKAGE + "." + interpolator);

		List<SurfacePoint<Integer, BigDecimal, BigDecimal>> surfacePoints = genAlgorithm.generate("EquityOption",
				instance, quoteNames, quoteDate, quoteSet, interpolatorObject, strikes);

		return surfacePoints;
	}

	@Override
	public List<SurfacePoint<Long, BigDecimal, BigDecimal>> getEquityOptionVolatilitySurfacePointsBySurfaceId(
			long volatilitySurfaceId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigDecimal getVolatility(String volativitycurveName, int maturity, double tenor) {
		// TODO Auto-generated method stub
		return null;
	}
}
