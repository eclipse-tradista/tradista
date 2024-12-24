package org.eclipse.tradista.core.marketdata.interpolator;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.stat.StatUtils;

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

public abstract class AbstractMultivariateInterpolator
		implements org.eclipse.tradista.core.marketdata.interpolator.MultivariateInterpolator {

	private org.apache.commons.math3.analysis.interpolation.MultivariateInterpolator interpolator;

	public AbstractMultivariateInterpolator(
			org.apache.commons.math3.analysis.interpolation.MultivariateInterpolator interpolator) {
		this.interpolator = interpolator;
	}

	@Override
	public Map<Long, Map<Long, BigDecimal>> interpolate(double[][] xy, double[] z) {
		// Interpolate
		MultivariateFunction function = (MultivariateFunction) interpolator.interpolate(xy, z);

		double[] x = new double[xy.length];
		double[] y = new double[xy.length];

		for (int i = 0; i < xy.length; i++) {
			x[i] = xy[i][0];
			y[i] = xy[i][1];
		}
		// Determines the min x, max x, min y, max Y
		long minX = (long) StatUtils.min(x);
		long maxX = (long) StatUtils.max(x);
		long minY = (long) StatUtils.min(y);
		long maxY = (long) StatUtils.max(y);

		// Create the map from the BivariateFunction object
		return createResult(function, minX, maxX, minY, maxY);
	}

	@Override
	public Map<Integer, Map<BigDecimal, BigDecimal>> interpolate2(double[][] xy, double[] z) {
		// Interpolate
		MultivariateFunction function = (MultivariateFunction) interpolator.interpolate(xy, z);

		double[] x = new double[xy.length];
		double[] y = new double[xy.length];

		for (int i = 0; i < xy.length; i++) {
			x[i] = xy[i][0];
			y[i] = xy[i][1];
		}
		// Determines the min x, max x, min y, max Y
		int minX = (int) StatUtils.min(x);
		int maxX = (int) StatUtils.max(x);
		double minY = StatUtils.min(y);
		double maxY = StatUtils.max(y);

		// Create the map from the BivariateFunction object
		return createResult(function, minX, maxX, minY, maxY);
	}

	private Map<Long, Map<Long, BigDecimal>> createResult(MultivariateFunction function, long minX, long maxX,
			long minY, long maxY) {
		Map<Long, Map<Long, BigDecimal>> result = new HashMap<Long, Map<Long, BigDecimal>>();
		for (long i = minX; i <= maxX; i++) {
			for (long j = minY; j <= maxY; j++) {
				Map<Long, BigDecimal> currentMap = result.get(i);
				if (currentMap == null) {
					currentMap = new HashMap<Long, BigDecimal>();
				}
				currentMap.put(j, BigDecimal.valueOf(function.value(new double[] { i, j })));
				result.put(i, currentMap);
			}
		}
		return result;
	}

	private Map<Integer, Map<BigDecimal, BigDecimal>> createResult(MultivariateFunction function, int minX, int maxX,
			double minY, double maxY) {
		Map<Integer, Map<BigDecimal, BigDecimal>> result = new HashMap<Integer, Map<BigDecimal, BigDecimal>>();
		for (int i = minX; i <= maxX; i++) {
			for (double j = minY; j <= maxY; j++) {
				Map<BigDecimal, BigDecimal> currentMap = result.get(i);
				if (currentMap == null) {
					currentMap = new HashMap<BigDecimal, BigDecimal>();
				}
				currentMap.put(BigDecimal.valueOf(j), BigDecimal.valueOf(function.value(new double[] { i, j })));
				result.put(i, currentMap);
			}
		}
		return result;
	}
}
