package org.eclipse.tradista.security.equityoption.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.SurfacePoint;
import org.eclipse.tradista.security.equityoption.model.EquityOptionVolatilitySurface;
import org.eclipse.tradista.security.equityoption.validator.EquityOptionVolatilitySurfaceValidator;

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

public class EquityOptionVolatilitySurfaceBusinessDelegate {

	private EquityOptionVolatilitySurfaceService equityOptionVolatilitySurfaceService;

	private EquityOptionVolatilitySurfaceValidator validator;

	private static Map<String, Number> expiries;

	{
		expiries = new HashMap<String, Number>(10);
		expiries.put("1M", 30);
		expiries.put("3M", 91);
		expiries.put("6M", 183);
		expiries.put("1Y", 365);
		expiries.put("2Y", 700);
		expiries.put("3Y", 1065);
		expiries.put("4Y", 1400);
		expiries.put("5Y", 1765);
		expiries.put("7Y", 2465);
		expiries.put("10Y", 3650);
	}

	public EquityOptionVolatilitySurfaceBusinessDelegate() {
		equityOptionVolatilitySurfaceService = TradistaServiceLocator.getInstance()
				.getEquityOptionVolatilitySurfaceService();
		validator = new EquityOptionVolatilitySurfaceValidator();
	}

	public Set<EquityOptionVolatilitySurface> getAllEquityOptionVolatilitySurfaces() {
		return SecurityUtil.run(() -> equityOptionVolatilitySurfaceService.getAllEquityOptionVolatilitySurfaces());
	}

	public boolean deleteEquityOptionVolatilitySurface(long surfaceId) throws TradistaBusinessException {
		if (surfaceId <= 0) {
			throw new TradistaBusinessException("The surface id must be positive.");
		}
		return SecurityUtil
				.runEx(() -> equityOptionVolatilitySurfaceService.deleteEquityOptionVolatilitySurface(surfaceId));
	}

	public Set<String> getAllGenerationAlgorithms() {
		return SecurityUtil.run(() -> equityOptionVolatilitySurfaceService.getAllGenerationAlgorithms());
	}

	public Set<String> getAllInterpolators() {
		return SecurityUtil.run(() -> equityOptionVolatilitySurfaceService.getAllInterpolators());
	}

	public Set<String> getAllInstances() {
		return SecurityUtil.run(() -> equityOptionVolatilitySurfaceService.getAllInstances());
	}

	public Collection<Number> getAllOptionExpiries() {
		return expiries.values();
	}

	public EquityOptionVolatilitySurface getEquityOptionVolatilitySurfaceByName(String name) {
		return SecurityUtil
				.run(() -> equityOptionVolatilitySurfaceService.getEquityOptionVolatilitySurfaceByName(name));
	}

	public boolean saveEquityOptionVolatilitySurfacePoints(long id,
			List<SurfacePoint<Long, BigDecimal, BigDecimal>> ratePoints, Long optionExpiry, BigDecimal delta) {
		return SecurityUtil.run(() -> equityOptionVolatilitySurfaceService.saveEquityOptionVolatilitySurfacePoints(id,
				ratePoints, optionExpiry, delta));
	}

	public long saveEquityOptionVolatilitySurface(EquityOptionVolatilitySurface surface)
			throws TradistaBusinessException {
		validator.validateSurface(surface);
		return SecurityUtil
				.runEx(() -> equityOptionVolatilitySurfaceService.saveEquityOptionVolatilitySurface(surface));
	}

	public BigDecimal getVolatility(String volatilitySurfaceName, int optionExpiry) {
		return SecurityUtil
				.run(() -> equityOptionVolatilitySurfaceService.getVolatility(volatilitySurfaceName, optionExpiry));
	}

	public List<SurfacePoint<Number, Number, Number>> getEquityOptionVolatilitySurfacePointsBySurfaceIdOptionExpiryAndStrike(
			long surfaceId, Long optionExpiry, BigDecimal strike) {
		return SecurityUtil.run(() -> equityOptionVolatilitySurfaceService
				.getEquityOptionVolatilitySurfacePointsBySurfaceIdOptionExpiryAndStrike(surfaceId, optionExpiry,
						strike));
	}

	public List<SurfacePoint<Integer, BigDecimal, BigDecimal>> generate(String algorithm, String interpolator,
			String instance, LocalDate quoteDate, QuoteSet quoteSet, List<String> quoteNames, List<BigDecimal> strikes)
			throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (quoteNames == null || quoteNames.isEmpty()) {
			errMsg.append(String.format("At least one quote must be selected.%n"));
		}
		if (strikes == null || strikes.isEmpty()) {
			errMsg.append(String.format("At least one Strike/Price ratio must be selected.%n"));
		}
		if (algorithm == null || algorithm.isEmpty()) {
			errMsg.append(String.format("The algorithm is mandatory.%n"));
		}
		if (interpolator == null || interpolator.isEmpty()) {
			errMsg.append(String.format("The interpolator is mandatory.%n"));
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

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil.run(() -> equityOptionVolatilitySurfaceService.generate(algorithm, interpolator, instance,
				quoteDate, quoteSet, quoteNames, strikes));
	}

	public List<SurfacePoint<Long, BigDecimal, BigDecimal>> getEquityOptionVolatilitySurfacePointsBySurfaceId(
			long volatilitySurfaceId) {
		return SecurityUtil.run(() -> equityOptionVolatilitySurfaceService
				.getEquityOptionVolatilitySurfacePointsBySurfaceId(volatilitySurfaceId));
	}

	public BigDecimal getVolatility(String volatilitySurfaceName, int maturity, double tenor) {
		return SecurityUtil
				.run(() -> equityOptionVolatilitySurfaceService.getVolatility(volatilitySurfaceName, maturity, tenor));
	}

	public Set<String> getAllOptionExpiriesAsString() {
		return expiries.keySet();
	}

	public String getOptionExpiryName(Number value) throws TradistaBusinessException {
		for (Map.Entry<String, Number> entry : expiries.entrySet()) {
			if (entry.getValue().equals(value)) {
				return entry.getKey();
			}
		}
		throw new TradistaBusinessException(String.format("No option expiry found for this value: (%s)", value));
	}

	public Integer getOptionExpiryValue(String name) throws TradistaBusinessException {
		Integer value = (Integer) expiries.get(name);
		if (value == null) {
			throw new TradistaBusinessException(
					String.format("No option expiry value found for this name: (%s)", name));
		}
		return value;
	}

	public EquityOptionVolatilitySurface getEquityOptionVolatilitySurfaceById(long id)
			throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The equity option volatility id must be positive.");
		}
		return SecurityUtil.run(() -> equityOptionVolatilitySurfaceService.getEquityOptionVolatilitySurfaceById(id));
	}
}