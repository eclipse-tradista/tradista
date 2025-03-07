package org.eclipse.tradista.core.marketdata.generationalgorithm;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.eclipse.tradista.core.marketdata.interpolator.MultivariateInterpolator;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.SurfacePoint;

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

public interface SurfaceGenerationAlgorithm {

	List<SurfacePoint<Number, Number, Number>> generate(String surfaceType, String instance, List<Long> quoteIds,
			LocalDate quoteDate, QuoteSet quoteSet, MultivariateInterpolator interpolator);

	List<SurfacePoint<Integer, BigDecimal, BigDecimal>> generate(String surfaceType, String instance,
			List<String> quoteNames, LocalDate quoteDate, QuoteSet quoteSet, MultivariateInterpolator interpolator,
			List<BigDecimal> deltas);

}
