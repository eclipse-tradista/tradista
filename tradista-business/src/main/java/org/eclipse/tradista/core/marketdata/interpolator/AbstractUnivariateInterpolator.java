package org.eclipse.tradista.core.marketdata.interpolator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import org.eclipse.tradista.core.common.util.DateUtil;

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

public abstract class AbstractUnivariateInterpolator
		implements org.eclipse.tradista.core.marketdata.interpolator.UnivariateInterpolator {

	private org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator interpolator;

	public AbstractUnivariateInterpolator(UnivariateInterpolator interpolator) {
		this.interpolator = interpolator;
	}

	@Override
	public Map<Long, BigDecimal> interpolate2(SortedMap<Long, BigDecimal> points) {
		// Create two double arrays from the map
		double x[] = convertLongSet(points.keySet());
		double y[] = convertBigDecimalCollection(points.values());
		// Interpolate
		PolynomialSplineFunction function = (PolynomialSplineFunction) interpolator.interpolate(x, y);
		// Create the map from the UnivariateFunction object
		return createResult2(function, points.firstKey());
	}

	private double[] convertLongSet(Set<Long> keySet) {
		double y[] = new double[keySet.size()];
		int i = 0;
		for (Long val : keySet) {
			y[i] = val.doubleValue();
			i++;
		}
		return y;
	}

	@Override
	public Map<LocalDate, BigDecimal> interpolate(SortedMap<LocalDate, BigDecimal> points) {
		// Create two double arrays from the map
		double x[] = convertLocalDateSet(points.keySet());
		double y[] = convertBigDecimalCollection(points.values());
		// Interpolate
		PolynomialSplineFunction function = (PolynomialSplineFunction) interpolator.interpolate(x, y);
		// Create the map from the UnivariateFunction object
		return createResult(function, points.firstKey());
	}

	private Map<LocalDate, BigDecimal> createResult(PolynomialSplineFunction function, LocalDate firstDate) {
		Map<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();
		int i = 0;
		while (function.isValidPoint(i)) {
			System.out.println("i: " + i);
			System.out.println("function(i): " + function.value(i));
			result.put(createDate(firstDate, i), BigDecimal.valueOf(function.value(i)));
			i++;
		}
		return result;
	}

	private Map<Long, BigDecimal> createResult2(PolynomialSplineFunction function, Long firstLong) {
		Map<Long, BigDecimal> result = new TreeMap<Long, BigDecimal>();
		long i = firstLong;
		while (function.isValidPoint(i)) {
			System.out.println("i: " + i);
			System.out.println("function(i): " + function.value(i));
			result.put(firstLong + i, BigDecimal.valueOf(function.value(i)));
			i++;
		}
		return result;
	}

	private LocalDate createDate(LocalDate firstDate, int i) {
		LocalDate date = firstDate.plus(i, ChronoUnit.DAYS);
		System.out.println("Date created: " + date);
		return date;
	}

	private double[] convertBigDecimalCollection(Collection<BigDecimal> values) {
		double y[] = new double[values.size()];
		int i = 0;
		for (BigDecimal val : values) {
			y[i] = val.doubleValue();
			i++;
		}
		return y;
	}

	private double[] convertLocalDateSet(Set<LocalDate> dates) {
		double x[] = new double[dates.size()];
		LocalDate firstDate = null;
		int i = 0;
		for (java.util.Iterator<LocalDate> it = dates.iterator(); it.hasNext();) {
			LocalDate date = it.next();
			if (firstDate == null) {
				x[0] = 0;
				firstDate = date;
			} else {
				x[i] = DateUtil.difference(firstDate, date);
			}
			i++;
		}
		return x;
	}

}
