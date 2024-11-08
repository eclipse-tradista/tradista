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
import org.eclipse.tradista.core.common.util.TradistaUtil;
import org.eclipse.tradista.core.marketdata.bootstraphandler.BootstrapHandler;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.RatePoint;
import org.eclipse.tradista.core.marketdata.model.ZeroCouponCurve;
import org.eclipse.tradista.core.marketdata.service.InterestRateCurveService;
import org.eclipse.tradista.core.marketdata.validator.DefaultInterestRateCurveValidator;
import org.eclipse.tradista.core.marketdata.validator.DefaultZeroCouponCurveValidator;
import org.eclipse.tradista.core.marketdata.validator.InterestRateCurveValidator;

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

public class InterestRateCurveBusinessDelegate {

	private InterestRateCurveService interestRateCurveService;

	private InterestRateCurveValidator irCurveValidator;

	private InterestRateCurveValidator zcCurveValidator;

	public InterestRateCurveBusinessDelegate() {
		interestRateCurveService = TradistaServiceLocator.getInstance().getInterestRateCurveService();
		irCurveValidator = new DefaultInterestRateCurveValidator();
		zcCurveValidator = new DefaultZeroCouponCurveValidator();
	}

	public Set<InterestRateCurve> getAllInterestRateCurves() {
		return SecurityUtil.run(() -> interestRateCurveService.getAllInterestRateCurves());
	}

	public Set<ZeroCouponCurve> getAllZeroCouponCurves() {
		return SecurityUtil.run(() -> interestRateCurveService.getAllZeroCouponCurves());
	}

	public List<RatePoint> getInterestRateCurvePointsByCurveIdAndDate(long curveId, Year year, Month month)
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
		return SecurityUtil
				.runEx(() -> interestRateCurveService.getInterestRateCurvePointsByCurveIdAndDate(curveId, year, month));
	}

	public boolean saveInterestRateCurvePoints(long curveId, List<RatePoint> ratePoints, Year year, Month month)
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
		return SecurityUtil
				.runEx(() -> interestRateCurveService.saveInterestRateCurvePoints(curveId, ratePoints, year, month));
	}

	public boolean deleteInterestRateCurve(long curveId) throws TradistaBusinessException {
		if (curveId <= 0) {
			throw new TradistaBusinessException("The curve id must be positive.");
		}
		return SecurityUtil.runEx(() -> interestRateCurveService.deleteInterestRateCurve(curveId));
	}

	public List<RatePoint> getInterestRateCurvePointsByCurveIdAndDates(long curveId, LocalDate min, LocalDate max)
			throws TradistaBusinessException {
		if (curveId <= 0) {
			throw new TradistaBusinessException("The curve id must be positive.");
		}
		return SecurityUtil
				.run(() -> interestRateCurveService.getInterestRateCurvePointsByCurveIdAndDates(curveId, min, max));
	}

	public List<RatePoint> getInterestRateCurvePointsByCurveAndDates(InterestRateCurve curve, LocalDate min,
			LocalDate max) throws TradistaBusinessException {
		if (curve == null) {
			throw new TradistaBusinessException("The curve is mandatory.");
		}
		return SecurityUtil.run(
				() -> interestRateCurveService.getInterestRateCurvePointsByCurveIdAndDates(curve.getId(), min, max));
	}

	public InterestRateCurve getInterestRateCurveByName(String curveName) {
		return SecurityUtil.run(() -> interestRateCurveService.getInterestRateCurveByName(curveName));
	}

	public Set<String> getAllInterestRateCurveTypes() {
		return SecurityUtil.run(() -> interestRateCurveService.getAllInterestRateCurveTypes());
	}

	public long saveInterestRateCurve(InterestRateCurve curve) throws TradistaBusinessException {
		if (curve instanceof ZeroCouponCurve) {
			zcCurveValidator.validateCurve(curve);
		} else {
			irCurveValidator.validateCurve(curve);
		}
		return SecurityUtil.runEx(() -> interestRateCurveService.saveInterestRateCurve(curve));
	}

	public List<RatePoint> getInterestRateCurvePointsByCurveId(long curveId) {
		return SecurityUtil.run(() -> interestRateCurveService.getInterestRateCurvePointsByCurveId(curveId));
	}

	public List<RatePoint> generate(String algorithm, String interpolator, String instance, LocalDate quoteDate,
			QuoteSet quoteSet, List<Long> quoteIds) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (quoteIds == null || quoteIds.isEmpty()) {
			errMsg.append(String.format("At least one quote must be selected.%n"));
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
		return SecurityUtil.run(() -> interestRateCurveService.generate(algorithm, interpolator, instance, quoteDate,
				quoteSet, quoteIds));
	}

	public Set<String> getAllGenerationAlgorithms() {
		return SecurityUtil.run(() -> interestRateCurveService.getAllGenerationAlgorithms());
	}

	public Set<String> getAllInterpolators() {
		return SecurityUtil.run(() -> interestRateCurveService.getAllInterpolators());
	}

	public Set<String> getAllInstances() {
		Set<String> instances = new HashSet<String>();
		instances.add("CLOSE");
		instances.add("OPEN");
		instances.add("BID");
		instances.add("ASK");
		instances.add("MID");
		return instances;
	}

	public InterestRateCurve getInterestRateCurveById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException(String.format("The id (%s) must be positive.", id));
		}
		return SecurityUtil.run(() -> interestRateCurveService.getInterestRateCurveById(id));
	}

	/**
	 * Gets the list of handled product types for zero coupon curve generation.
	 * 
	 * @return
	 */
	public Set<String> getBootstrapableProductTypes() {
		List<BootstrapHandler> items = TradistaUtil.getAllInstancesByType(BootstrapHandler.class, "org.eclipse.tradista");
		Set<String> productTypes = new HashSet<String>();
		if (!items.isEmpty()) {
			for (BootstrapHandler item : items) {
				productTypes.add(item.getInstrumentName());
			}
		}

		return productTypes;
	}

}