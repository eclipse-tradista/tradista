package org.eclipse.tradista.core.marketdata.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.marketdata.model.Curve;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.RatePoint;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
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
public class CurveServiceBean implements CurveService {

	@EJB
	private InterestRateCurveService irCurveService;

	@EJB
	private FXCurveService fxCurveService;

	@Interceptors(CurveFilteringInterceptor.class)
	@Override
	public Curve<LocalDate, BigDecimal> getCurveById(long curveId) {
		// Method to be enriched when new curve types will exist.
		Curve<LocalDate, BigDecimal> curve = irCurveService.getInterestRateCurveById(curveId);
		if (curve == null) {
			curve = fxCurveService.getFXCurveById(curveId);
		}
		return curve;
	}

	@Override
	public List<RatePoint> getCurvePointsByCurveAndDates(Curve<LocalDate, BigDecimal> curve, LocalDate min,
			LocalDate max) {
		// Method to be enriched when new curve types will exist.
		if (curve instanceof InterestRateCurve) {
			return irCurveService.getInterestRateCurvePointsByCurveIdAndDates(curve.getId(), min, max);
		}
		if (curve instanceof FXCurve) {
			return fxCurveService.getFXCurvePointsByCurveIdAndDates(curve.getId(), min, max);
		}
		return null;
	}

	@Override
	public Set<Curve<? extends LocalDate, ? extends BigDecimal>> getAllCurves() {
		Set<InterestRateCurve> irCurves = irCurveService.getAllInterestRateCurves();
		Set<FXCurve> fxCurves = fxCurveService.getAllFXCurves();
		Set<Curve<? extends LocalDate, ? extends BigDecimal>> result = null;
		if (irCurves != null) {
			if (fxCurves != null) {
				result = new HashSet<Curve<? extends LocalDate, ? extends BigDecimal>>();
				result.addAll(irCurves);
				result.addAll(fxCurves);
			} else {
				result = new HashSet<Curve<? extends LocalDate, ? extends BigDecimal>>();
				result.addAll(irCurves);
			}
		} else {
			if (fxCurves != null) {
				result = new HashSet<Curve<? extends LocalDate, ? extends BigDecimal>>();
				result.addAll(fxCurves);
			}
		}
		return result;
	}

	@Interceptors(CurveFilteringInterceptor.class)
	@Override
	public List<RatePoint> getCurvePointsByCurveAndDate(Curve<? extends LocalDate, ? extends BigDecimal> curve,
			Year year, Month month) throws TradistaBusinessException {
		// Method to be enriched when new curve types will exist.

		if (curve instanceof InterestRateCurve) {
			return irCurveService.getInterestRateCurvePointsByCurveIdAndDate(curve.getId(), year, month);
		}
		if (curve instanceof FXCurve) {
			return fxCurveService.getFXCurvePointsByCurveIdAndDate(curve.getId(), year, month);
		}
		return null;
	}

	@Interceptors(CurveFilteringInterceptor.class)
	@Override
	public boolean saveCurvePoints(Curve<? extends LocalDate, ? extends BigDecimal> curve, List<RatePoint> ratePoints,
			Year year, Month month) throws TradistaBusinessException {
		// Method to be enriched when new curve types will exist.

		if (curve instanceof InterestRateCurve) {
			return irCurveService.saveInterestRateCurvePoints(curve.getId(), ratePoints, year, month);
		}
		if (curve instanceof FXCurve) {
			return fxCurveService.saveFXCurvePoints(curve.getId(), ratePoints, year, month);
		}
		return false;
	}

	@Override
	public Set<String> getAllCurveTypes() {
		Set<String> irTypes = irCurveService.getAllInterestRateCurveTypes();
		irTypes.add(FXCurve.FX_CURVE);
		return irTypes;
	}

	@Interceptors(CurveFilteringInterceptor.class)
	@Override
	public long saveCurve(Curve<LocalDate, BigDecimal> curve) throws TradistaBusinessException {
		// Method to be enriched when new curve types will exist.

		if (curve instanceof InterestRateCurve) {
			return irCurveService.saveInterestRateCurve((InterestRateCurve) curve);
		}
		if (curve instanceof FXCurve) {
			return fxCurveService.saveFXCurve((FXCurve) curve);
		}
		return -1;
	}

	@Interceptors(CurveFilteringInterceptor.class)
	@Override
	public boolean deleteCurve(Curve<? extends LocalDate, ? extends BigDecimal> curve)
			throws TradistaBusinessException {
		// Method to be enriched when new curve types will exist.

		if (curve instanceof InterestRateCurve) {
			return irCurveService.deleteInterestRateCurve(curve.getId());
		}
		if (curve instanceof FXCurve) {
			return fxCurveService.deleteFXCurve(curve.getId());
		}
		return false;
	}

}