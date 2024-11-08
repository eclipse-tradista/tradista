package org.eclipse.tradista.fx.fxoption.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import jakarta.ejb.Remote;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.SurfacePoint;
import org.eclipse.tradista.fx.fxoption.model.FXVolatilitySurface;

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
public interface FXVolatilitySurfaceService {

	Set<FXVolatilitySurface> getAllFXVolatilitySurfaces();

	FXVolatilitySurface getFXVolatilitySurfaceByName(String name);

	boolean saveFXVolatilitySurfacePoints(long id, List<SurfacePoint<Long, BigDecimal, BigDecimal>> ratePoints,
			Long optionExpiry, BigDecimal strike);

	long saveFXVolatilitySurface(FXVolatilitySurface surface) throws TradistaBusinessException;

	boolean deleteFXVolatilitySurface(long surfaceId) throws TradistaBusinessException;

	BigDecimal getVolatility(String volativitycurveName, int maturity, double tenor);

	List<SurfacePoint<Number, Number, Number>> getFXVolatilitySurfacePointsBySurfaceIdOptionExpiryAndStrike(
			long surfaceId, Long optionExpiry, BigDecimal strike);

	List<SurfacePoint<Integer, BigDecimal, BigDecimal>> generate(String value, String value2, String value3,
			LocalDate quoteDate, QuoteSet quoteSet, List<String> quoteNames, List<BigDecimal> deltas);

	Set<String> getAllInstances();

	Set<String> getAllInterpolators();

	Set<String> getAllGenerationAlgorithms();

	List<SurfacePoint<Integer, BigDecimal, BigDecimal>> getFXVolatilitySurfacePointsBySurfaceId(
			long volatilitySurfaceId);

	BigDecimal getVolatility(String volatilitySurfaceName, int maturity);

	FXVolatilitySurface getFXVolatilitySurfaceById(long id);

}