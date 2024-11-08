package org.eclipse.tradista.core.marketdata.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Set;

import jakarta.ejb.Remote;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.marketdata.model.Curve;
import org.eclipse.tradista.core.marketdata.model.RatePoint;

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

@Remote
public interface CurveService {

	public Curve<LocalDate, BigDecimal> getCurveById(long curveId);

	public List<RatePoint> getCurvePointsByCurveAndDates(Curve<LocalDate, BigDecimal> curve, LocalDate min,
			LocalDate max);

	public Set<Curve<? extends LocalDate, ? extends BigDecimal>> getAllCurves();

	public List<RatePoint> getCurvePointsByCurveAndDate(Curve<? extends LocalDate, ? extends BigDecimal> curve,
			Year year, Month month) throws TradistaBusinessException;

	public boolean saveCurvePoints(Curve<? extends LocalDate, ? extends BigDecimal> curve, List<RatePoint> ratePoints,
			Year year, Month month) throws TradistaBusinessException;

	public Set<String> getAllCurveTypes();

	public long saveCurve(Curve<LocalDate, BigDecimal> curve) throws TradistaBusinessException;

	public boolean deleteCurve(Curve<? extends LocalDate, ? extends BigDecimal> curve) throws TradistaBusinessException;
}
