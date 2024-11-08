package org.eclipse.tradista.core.marketdata.generationalgorithm;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.tradista.core.common.util.TradistaUtil;
import org.eclipse.tradista.core.marketdata.interpolator.MultivariateInterpolator;
import org.eclipse.tradista.core.marketdata.model.Quote;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.SurfacePoint;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;
import org.eclipse.tradista.core.marketdata.surfacegenerationhandler.SurfaceGenerationHandler;

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

public class DefaultGenerator implements SurfaceGenerationAlgorithm {

	/**
	 * For FX Volatility Surface.
	 */
	public List<SurfacePoint<Integer, BigDecimal, BigDecimal>> generate(String surfaceType, String instance,
			List<String> quoteNames, LocalDate quoteDate, QuoteSet quoteSet, MultivariateInterpolator interpolator,
			List<BigDecimal> deltas) {
		List<SurfacePoint<Integer, BigDecimal, BigDecimal>> surfacePoints = new ArrayList<SurfacePoint<Integer, BigDecimal, BigDecimal>>();
		List<Quote> quotes = new ArrayList<Quote>();
		SurfaceGenerationHandler handler = null;

		// Get the SurfaceGenerationHandler
		handler = TradistaUtil.getInstance(SurfaceGenerationHandler.class,
				"org.eclipse.tradista." + surfaceType.toLowerCase() + "." + surfaceType + "SurfaceGenerationHandler");
		// Load the quote (and quote values) from Quote Id

		for (String quoteName : quoteNames) {
			List<Quote> quotesToAdd = new QuoteBusinessDelegate().getQuotesByName(quoteName);
			if (quotesToAdd != null && !quotesToAdd.isEmpty()) {
				quotes.addAll(quotesToAdd);
			}
		}

		// Extract Surface Points
		surfacePoints = handler.buildSurfacePoints2(quotes, quoteDate, instance, quoteSet);

		double[][] xy = new double[surfacePoints.size()][surfacePoints.size()];
		double[] z = new double[surfacePoints.size()];

		for (int i = 0; i < surfacePoints.size(); i++) {
			xy[i] = new double[] { surfacePoints.get(i).getxAxis().doubleValue(),
					surfacePoints.get(i).getyAxis().doubleValue() };
			z[i] = surfacePoints.get(i).getzAxis().doubleValue();
		}

		// Interpolate
		Map<Integer, Map<BigDecimal, BigDecimal>> results = interpolator.interpolate2(xy, z);

		surfacePoints.clear();

		for (Map.Entry<Integer, Map<BigDecimal, BigDecimal>> entry : results.entrySet()) {
			for (Map.Entry<BigDecimal, BigDecimal> entryValue : entry.getValue().entrySet()) {
				SurfacePoint<Integer, BigDecimal, BigDecimal> point = new SurfacePoint<Integer, BigDecimal, BigDecimal>(
						entry.getKey(), entryValue.getKey(), entryValue.getValue());
				surfacePoints.add(point);
			}
		}

		return surfacePoints;
	}

	public List<SurfacePoint<Number, Number, Number>> generate(String surfaceType, String instance, List<Long> quoteIds,
			LocalDate quoteDate, QuoteSet quoteSet, MultivariateInterpolator interpolator) {
		List<SurfacePoint<Number, Number, Number>> surfacePoints = new ArrayList<SurfacePoint<Number, Number, Number>>();
		List<Quote> quotes = new ArrayList<Quote>();
		SurfaceGenerationHandler handler = null;
		QuoteBusinessDelegate quoteBusinessDelegate = new QuoteBusinessDelegate();

		// Get the SurfaceGenerationHandler
		handler = TradistaUtil.getInstance(SurfaceGenerationHandler.class,
				"org.eclipse.tradista." + surfaceType.toLowerCase() + "." + surfaceType + "SurfaceGenerationHandler");

		// Load the quote (and quote values) from Quote Id
		for (long quoteId : quoteIds) {
			Quote quote = quoteBusinessDelegate.getQuoteById(quoteId);
			if (quote != null) {
				quotes.add(quote);
			}
		}

		// Extract Surface Points
		surfacePoints = handler.buildSurfacePoints(quotes, quoteDate, instance, quoteSet);

		double[][] xy = new double[surfacePoints.size()][surfacePoints.size()];
		double[] z = new double[surfacePoints.size()];

		for (int i = 0; i < surfacePoints.size(); i++) {
			xy[i] = new double[] { surfacePoints.get(i).getxAxis().doubleValue(),
					surfacePoints.get(i).getyAxis().doubleValue() };
			z[i] = surfacePoints.get(i).getzAxis().doubleValue();
		}

		// Interpolate
		Map<Long, Map<Long, BigDecimal>> results = interpolator.interpolate(xy, z);

		surfacePoints.clear();

		for (Map.Entry<Long, Map<Long, BigDecimal>> entry : results.entrySet()) {
			for (Map.Entry<Long, BigDecimal> entryValue : entry.getValue().entrySet()) {
				SurfacePoint<Number, Number, Number> point = new SurfacePoint<Number, Number, Number>(entry.getKey(),
						entryValue.getKey(), entryValue.getValue());
				surfacePoints.add(point);
			}
		}

		return surfacePoints;
	}
}