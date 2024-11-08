package org.eclipse.tradista.ir.irswapoption.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.RatePoint;
import org.eclipse.tradista.core.marketdata.model.SurfacePoint;
import org.eclipse.tradista.ir.irswapoption.model.SwaptionVolatilitySurface;
import org.eclipse.tradista.ir.irswapoption.service.SwaptionVolatilitySurfaceService;
import org.eclipse.tradista.ir.irswapoption.validator.SwaptionVolatilitySurfaceValidator;

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

public class SwaptionVolatilitySurfaceBusinessDelegate {

	private static Map<String, Number> swapMaturities;

	{
		swapMaturities = new HashMap<String, Number>(10);
		swapMaturities.put("1Y", 365);
		swapMaturities.put("2Y", 700);
		swapMaturities.put("3Y", 1065);
		swapMaturities.put("4Y", 1400);
		swapMaturities.put("5Y", 1765);
		swapMaturities.put("7Y", 2465);
		swapMaturities.put("10Y", 3650);
	}

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

	private SwaptionVolatilitySurfaceService swaptionVolatilitySurfaceService;

	private SwaptionVolatilitySurfaceValidator validator;

	public SwaptionVolatilitySurfaceBusinessDelegate() {
		swaptionVolatilitySurfaceService = TradistaServiceLocator.getInstance().getSwaptionVolatilitySurfaceService();
		validator = new SwaptionVolatilitySurfaceValidator();
	}

	public Set<SwaptionVolatilitySurface> getAllSwaptionVolatilitySurfaces() {
		return SecurityUtil.run(() -> swaptionVolatilitySurfaceService.getAllSwaptionVolatilitySurfaces());
	}

	public SwaptionVolatilitySurface getSwaptionVolatilitySurfaceByName(String name) {
		return SecurityUtil.run(() -> swaptionVolatilitySurfaceService.getSwaptionVolatilitySurfaceByName(name));
	}

	public List<RatePoint> getInterestRateCurvePointsByCurveIdAndDate(long id, Integer year, Integer month) {
		return SecurityUtil.run(
				() -> swaptionVolatilitySurfaceService.getInterestRateCurvePointsByCurveIdAndDate(id, year, month));
	}

	public boolean saveSwaptionVolatilitySurfacePoints(long id, List<SurfacePoint<Float, Float, Float>> surfacePoints,
			Float optionLifeTime, Float swapLifetime) {
		return SecurityUtil.run(() -> swaptionVolatilitySurfaceService.saveSwaptionVolatilitySurfacePoints(id,
				surfacePoints, optionLifeTime, swapLifetime));
	}

	public boolean deleteSwaptionVolatilitySurface(long surfaceId) throws TradistaBusinessException {
		if (surfaceId <= 0) {
			throw new TradistaBusinessException("The surface id must be positive.");
		}
		return SecurityUtil.runEx(() -> swaptionVolatilitySurfaceService.deleteSwaptionVolatilitySurface(surfaceId));
	}

	public List<RatePoint> getInterestRateCurvePointsByCurveNameAndDates(String curveName, Date min, Date max) {
		return SecurityUtil.run(() -> swaptionVolatilitySurfaceService
				.getInterestRateCurvePointsByCurveNameAndDates(curveName, min, max));
	}

	public List<RatePoint> getInterestRateCurvePointsByCurveAndDates(InterestRateCurve curve, Date min, Date max) {
		return SecurityUtil
				.run(() -> swaptionVolatilitySurfaceService.getInterestRateCurvePointsByCurveAndDates(curve, min, max));
	}

	public BigDecimal getVolatility(String surfaceName, int timeToMaturity, int tenor) {
		return SecurityUtil
				.run(() -> swaptionVolatilitySurfaceService.getVolatility(surfaceName, timeToMaturity, tenor));
	}

	public List<SurfacePoint<Number, Number, Number>> getSwaptionVolatilitySurfacePointsBySurfaceIdOptionAndSwapLifetimes(
			long currentSwaptionVolatilitySurfaceId, Float optionLifetime, Float swapLifetime) {
		return SecurityUtil.run(() -> swaptionVolatilitySurfaceService
				.getSwaptionVolatilitySurfacePointsBySurfaceIdOptionAndSwapLifetimes(currentSwaptionVolatilitySurfaceId,
						optionLifetime, swapLifetime));
	}

	public long saveSwaptionVolatilitySurface(SwaptionVolatilitySurface surface) throws TradistaBusinessException {
		validator.validateSurface(surface);
		return SecurityUtil.runEx(() -> swaptionVolatilitySurfaceService.saveSwaptionVolatilitySurface(surface));
	}

	public List<SurfacePoint<Integer, Integer, BigDecimal>> generate(String algorithm, String interpolator,
			String instance, LocalDate quoteDate, QuoteSet quoteSet, List<Long> quoteIds)
			throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (quoteIds == null || quoteIds.isEmpty()) {
			errMsg.append(String.format("At least one quote must be selected.%n"));
		}
		if (quoteIds == null || quoteIds.isEmpty()) {
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
		return SecurityUtil.run(() -> swaptionVolatilitySurfaceService.generate(algorithm, interpolator, instance,
				quoteDate, quoteSet, quoteIds));
	}

	public Set<String> getAllGenerationAlgorithms() {
		return SecurityUtil.run(() -> swaptionVolatilitySurfaceService.getAllGenerationAlgorithms());
	}

	public Set<String> getAllInterpolators() {
		return SecurityUtil.run(() -> swaptionVolatilitySurfaceService.getAllInterpolators());
	}

	public Set<String> getAllInstances() {
		return SecurityUtil.run(() -> swaptionVolatilitySurfaceService.getAllInstances());
	}

	public List<SurfacePoint<Integer, Integer, BigDecimal>> getSwaptionVolatilitySurfacePointsBySurfaceId(
			long volatilitySurfaceId) {
		return SecurityUtil.run(() -> swaptionVolatilitySurfaceService
				.getSwaptionVolatilitySurfacePointsBySurfaceId(volatilitySurfaceId));
	}

	public Collection<Number> getAllSwapMaturities() {
		return swapMaturities.values();
	}

	public Set<String> getAllSwapMaturitiesAsString() {
		return swapMaturities.keySet();
	}

	public String getSwapMaturityName(Number value) throws TradistaBusinessException {
		for (Map.Entry<String, Number> entry : swapMaturities.entrySet()) {
			if (entry.getValue().equals(value)) {
				return entry.getKey();
			}
		}
		throw new TradistaBusinessException(String.format("No swap maturity found for this value: (%s)", value));
	}

	public Integer getSwapMaturityValue(String name) throws TradistaBusinessException {
		Integer value = (Integer) swapMaturities.get(name);
		if (value == null) {
			throw new TradistaBusinessException(
					String.format("No swap maturity value found for this name: (%s)", name));
		}
		return value;
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

	public SwaptionVolatilitySurface getSwaptionVolatilitySurfaceById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The swaption volatility id must be positive.");
		}
		return SecurityUtil.run(() -> swaptionVolatilitySurfaceService.getSwaptionVolatilitySurfaceById(id));
	}

}