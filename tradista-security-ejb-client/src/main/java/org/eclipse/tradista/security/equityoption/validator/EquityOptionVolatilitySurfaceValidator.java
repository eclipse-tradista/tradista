package org.eclipse.tradista.security.equityoption.validator;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.marketdata.model.SurfacePoint;
import org.eclipse.tradista.security.equityoption.model.EquityOptionVolatilitySurface;

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

public class EquityOptionVolatilitySurfaceValidator {

	public void validateSurface(EquityOptionVolatilitySurface surface) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (surface == null) {
			throw new TradistaBusinessException("The surface cannot be null.%n");
		}
		if (StringUtils.isBlank(surface.getName())) {
			errMsg.append(String.format("The surface name is mandatory.%n"));
		}
		if (surface.isGenerated()) {
			if (surface.getAlgorithm() == null) {
				errMsg.append(String.format("The algorithm is mandatory when the surface is generated.%n"));
			}
			if (surface.getInterpolator() == null) {
				errMsg.append(String.format("The interpolator is mandatory when the surface is generated.%n"));
			}
			if (surface.getInstance() == null) {
				errMsg.append(String.format("The instance is mandatory when the surface is generated.%n"));
			}
			if (surface.getQuoteDate() == null) {
				errMsg.append(String.format("The quote date is mandatory when the surface is generated.%n"));
			}
			if (surface.getQuoteSet() == null) {
				errMsg.append(String.format("The quote set is mandatory when the surface is generated.%n"));
			}
			if (surface.getQuotes() == null || surface.getQuotes().isEmpty()) {
				errMsg.append(String.format("At least one quote must be selected when the surface is generated.%n"));
			}
		}

		if (surface.getStrikes() == null || surface.getStrikes().isEmpty()) {
			errMsg.append(String.format("At least one Strike/Price ratio must be selected.%n"));
		} else {
			if (surface.getPoints() != null && !surface.getPoints().isEmpty()) {
				for (SurfacePoint<Integer, BigDecimal, BigDecimal> point : surface.getPoints()) {
					boolean found = false;
					if (surface.getStrikes() != null && !surface.getStrikes().isEmpty()) {
						for (BigDecimal d : surface.getStrikes()) {
							if (d.compareTo(point.getyAxis()) == 0) {
								found = true;
								break;
							}
						}
					}
					if (!found) {
						errMsg.append(String.format(
								"Error for this surface point: %s. The Strike/Price ratio (%s) is not defined in this surface.%n",
								point, point.getyAxis()));
					}
				}
			}
		}

		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}
