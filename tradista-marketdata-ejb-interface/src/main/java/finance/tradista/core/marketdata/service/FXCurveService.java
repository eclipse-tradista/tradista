package org.eclipse.tradista.core.marketdata.service;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Set;

import jakarta.ejb.Remote;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
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
public interface FXCurveService {

	Set<FXCurve> getAllFXCurves();

	Set<String> getAllGenerationAlgorithms();

	Set<String> getAllInterpolators();

	FXCurve getFXCurveById(long id);

	List<RatePoint> getFXCurvePointsByCurveIdAndDate(long id, Year year, Month month) throws TradistaBusinessException;

	boolean saveFXCurvePoints(long id, List<RatePoint> ratePoints, Year year, Month month)
			throws TradistaBusinessException;

	long saveFXCurve(FXCurve curve) throws TradistaBusinessException;

	boolean deleteFXCurve(long curveId) throws TradistaBusinessException;

	List<RatePoint> getFXCurvePointsByCurveIdAndDates(long curveId, LocalDate min, LocalDate max);

	List<RatePoint> getFXCurvePointsByCurveId(long curveId);

	List<RatePoint> generate(String algorithm, String interpolator, String instance, LocalDate quoteDate,
			QuoteSet quoteSet, Currency primaryCurrency, Currency quoteCurrency,
			InterestRateCurve primaryCurrencyIRCurve, InterestRateCurve quoteCurrencyIRCurve)
			throws TradistaBusinessException;

}