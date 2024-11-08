package org.eclipse.tradista.ir.irswapoption.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Set;

import jakarta.ejb.Remote;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.RatePoint;
import org.eclipse.tradista.core.marketdata.model.SurfacePoint;
import org.eclipse.tradista.ir.irswapoption.model.SwaptionVolatilitySurface;

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

@Remote
public interface SwaptionVolatilitySurfaceService {

	Set<SwaptionVolatilitySurface> getAllSwaptionVolatilitySurfaces();

	SwaptionVolatilitySurface getSwaptionVolatilitySurfaceByName(String name);

	List<RatePoint> getInterestRateCurvePointsByCurveIdAndDate(long id, Integer year, Integer month);

	boolean saveSwaptionVolatilitySurfacePoints(long id, List<SurfacePoint<Float, Float, Float>> ratePoints,
			Float optionLifeTime, Float swapOptionLifetime);

	long saveSwaptionVolatilitySurface(SwaptionVolatilitySurface surface) throws TradistaBusinessException;

	boolean deleteSwaptionVolatilitySurface(long surfaceId) throws TradistaBusinessException;

	List<RatePoint> getInterestRateCurvePointsByCurveNameAndDates(String curveName, Date min, Date max);

	List<RatePoint> getInterestRateCurvePointsByCurveAndDates(InterestRateCurve curve, Date min, Date max);

	BigDecimal getVolatility(String volativitycurveName, int maturity, int tenor);

	List<SurfacePoint<Number, Number, Number>> getSwaptionVolatilitySurfacePointsBySurfaceIdOptionAndSwapLifetimes(
			long currentSwaptionVolatilitySurfaceId, Float optionLifetime, Float swapLifetime);

	List<SurfacePoint<Integer, Integer, BigDecimal>> generate(String value, String value2, String value3,
			LocalDate quoteDate, QuoteSet quoteSet, List<Long> quoteIds);

	Set<String> getAllInstances();

	Set<String> getAllInterpolators();

	Set<String> getAllGenerationAlgorithms();

	List<SurfacePoint<Integer, Integer, BigDecimal>> getSwaptionVolatilitySurfacePointsBySurfaceId(
			long volatilitySurfaceId);

	SwaptionVolatilitySurface getSwaptionVolatilitySurfaceById(long id);

}