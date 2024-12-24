package org.eclipse.tradista.fx.fxoption.service;

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
import org.eclipse.tradista.fx.fxoption.model.FXVolatilitySurface;
import org.eclipse.tradista.fx.fxoption.validator.FXOptionVolatilitySurfaceValidator;

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

public class FXVolatilitySurfaceBusinessDelegate {

	private FXVolatilitySurfaceService fxVolatilitySurfaceService;

	private FXOptionVolatilitySurfaceValidator validator;

	private static Map<String, Number> optionExpiries;

	{
		optionExpiries = new HashMap<String, Number>(10);
		optionExpiries.put("1M", 30);
		optionExpiries.put("3M", 91);
		optionExpiries.put("6M", 183);
		optionExpiries.put("1Y", 365);
		optionExpiries.put("2Y", 700);
		optionExpiries.put("3Y", 1065);
		optionExpiries.put("4Y", 1400);
		optionExpiries.put("5Y", 1765);
	}

	public FXVolatilitySurfaceBusinessDelegate() {
		fxVolatilitySurfaceService = TradistaServiceLocator.getInstance().getFXVolatilitySurfaceService();
		validator = new FXOptionVolatilitySurfaceValidator();
	}

	public Set<FXVolatilitySurface> getAllFXVolatilitySurfaces() {
		return SecurityUtil.run(() -> fxVolatilitySurfaceService.getAllFXVolatilitySurfaces());
	}

	public FXVolatilitySurface getFXVolatilitySurfaceByName(String name) {
		return SecurityUtil.run(() -> fxVolatilitySurfaceService.getFXVolatilitySurfaceByName(name));
	}

	public boolean saveFXVolatilitySurfacePoints(long id,
			List<SurfacePoint<Long, BigDecimal, BigDecimal>> surfacePoints, Long optionExpiry, BigDecimal strike) {
		return SecurityUtil.run(() -> fxVolatilitySurfaceService.saveFXVolatilitySurfacePoints(id, surfacePoints,
				optionExpiry, strike));
	}

	public boolean deleteFXVolatilitySurface(long surfaceId) throws TradistaBusinessException {
		if (surfaceId <= 0) {
			throw new TradistaBusinessException("The surface id must be positive.");
		}
		return SecurityUtil.runEx(() -> fxVolatilitySurfaceService.deleteFXVolatilitySurface(surfaceId));
	}

	public BigDecimal getVolatility(String surfaceName, int timeToMaturity, double tenor) {
		return SecurityUtil.run(() -> fxVolatilitySurfaceService.getVolatility(surfaceName, timeToMaturity, tenor));
	}

	public List<SurfacePoint<Number, Number, Number>> getFXVolatilitySurfacePointsBySurfaceIdOptionExpiryAndStrike(
			long surfaceId, Long optionExpiry, BigDecimal strike) {
		return SecurityUtil.run(() -> fxVolatilitySurfaceService
				.getFXVolatilitySurfacePointsBySurfaceIdOptionExpiryAndStrike(surfaceId, optionExpiry, strike));
	}

	public long saveFXVolatilitySurface(FXVolatilitySurface surface) throws TradistaBusinessException {
		validator.validateSurface(surface);
		return SecurityUtil.runEx(() -> fxVolatilitySurfaceService.saveFXVolatilitySurface(surface));

	}

	public List<SurfacePoint<Integer, BigDecimal, BigDecimal>> generate(String algorithm, String interpolator,
			String instance, LocalDate quoteDate, QuoteSet quoteSet, List<String> quoteNames, List<BigDecimal> deltas)
			throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (quoteNames == null || quoteNames.isEmpty()) {
			errMsg.append(String.format("At least one quote must be selected.%n"));
		}
		if (deltas == null || deltas.isEmpty()) {
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
		return SecurityUtil.run(() -> fxVolatilitySurfaceService.generate(algorithm, interpolator, instance, quoteDate,
				quoteSet, quoteNames, deltas));

	}

	public Set<String> getAllGenerationAlgorithms() {
		return SecurityUtil.run(() -> fxVolatilitySurfaceService.getAllGenerationAlgorithms());
	}

	public Set<String> getAllInterpolators() {
		return SecurityUtil.run(() -> fxVolatilitySurfaceService.getAllInterpolators());
	}

	public Set<String> getAllInstances() {
		return SecurityUtil.run(() -> fxVolatilitySurfaceService.getAllInstances());
	}

	public Collection<Number> getAllOptionExpiries() {
		return optionExpiries.values();
	}

	public Set<String> getAllOptionExpiriesAsString() {
		return optionExpiries.keySet();
	}

	public String getOptionExpiryName(Number value) throws TradistaBusinessException {
		for (Map.Entry<String, Number> entry : optionExpiries.entrySet()) {
			if (entry.getValue().equals(value)) {
				return entry.getKey();
			}
		}
		throw new TradistaBusinessException(String.format("No option expiry found for this value: (%s)", value));
	}

	public Integer getOptionExpiryValue(String name) throws TradistaBusinessException {
		Integer value = (Integer) optionExpiries.get(name);
		if (value == null) {
			throw new TradistaBusinessException(
					String.format("No option expiry value found for this name: (%s)", name));
		}
		return value;
	}

	public List<SurfacePoint<Integer, BigDecimal, BigDecimal>> getFXVolatilitySurfacePointsBySurfaceId(
			long volatilitySurfaceId) {
		return SecurityUtil
				.run(() -> fxVolatilitySurfaceService.getFXVolatilitySurfacePointsBySurfaceId(volatilitySurfaceId));
	}

	public BigDecimal getVolatility(String volatilitySurfaceName, int optionExpiry) {
		return SecurityUtil.run(() -> fxVolatilitySurfaceService.getVolatility(volatilitySurfaceName, optionExpiry));
	}

	public FXVolatilitySurface getFXVolatilitySurfaceById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The fx volatility id must be positive.");
		}
		return SecurityUtil.run(() -> fxVolatilitySurfaceService.getFXVolatilitySurfaceById(id));
	}

}