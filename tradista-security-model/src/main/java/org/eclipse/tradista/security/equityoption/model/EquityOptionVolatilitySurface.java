package org.eclipse.tradista.security.equityoption.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.marketdata.model.SurfacePoint;
import org.eclipse.tradista.core.marketdata.model.VolatilitySurface;

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

public class EquityOptionVolatilitySurface extends VolatilitySurface<Integer, BigDecimal, BigDecimal> {

	private static final long serialVersionUID = -8402233398798004883L;

	private List<BigDecimal> strikes;

	public EquityOptionVolatilitySurface(String name, LegalEntity processingOrg) {
		super(name, processingOrg);
	}

	public BigDecimal getVolatilityByOptionExpiry(long optionExpiry) throws TradistaBusinessException {
		if (points == null) {
			throw new TradistaBusinessException("The surface points list is null.");
		} else if (points.isEmpty()) {
			throw new TradistaBusinessException("The surface points list is empty.");
		}

		List<BigDecimal> volats = new ArrayList<BigDecimal>();

		for (SurfacePoint<Integer, BigDecimal, BigDecimal> p : points) {
			if (p.getxAxis() == optionExpiry) {
				volats.add(p.getzAxis());
			}
		}

		if (volats.isEmpty()) {
			throw new TradistaBusinessException(
					String.format("No values found for this option expiry: %", optionExpiry));
		}

		BigDecimal avgVolat = BigDecimal.ZERO;

		for (BigDecimal v : volats) {
			avgVolat = avgVolat.add(v);
		}

		return avgVolat.divide(BigDecimal.valueOf(volats.size()), RoundingMode.HALF_EVEN);

	}

	public List<BigDecimal> getStrikes() {
		if (strikes == null) {
			return null;
		}
		return new ArrayList<>(strikes);
	}

	public void setStrikes(List<BigDecimal> strikes) {
		this.strikes = strikes;
	}

	@Override
	public EquityOptionVolatilitySurface clone() {
		EquityOptionVolatilitySurface equityOptionVolatilitySurface = (EquityOptionVolatilitySurface) super.clone();
		if (strikes != null) {
			equityOptionVolatilitySurface.strikes = new ArrayList<>(strikes);
		}
		return equityOptionVolatilitySurface;
	}

}