package org.eclipse.tradista.fx.fxoption.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.marketdata.model.Generable;
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

public class FXVolatilitySurface extends VolatilitySurface<Integer, BigDecimal, BigDecimal> implements Generable {

	private static final long serialVersionUID = 2717361966579455059L;

	private List<BigDecimal> deltas;

	public FXVolatilitySurface(String name, LegalEntity processingOrg) {
		super(name, processingOrg);
	}

	public List<BigDecimal> getDeltas() {
		if (deltas == null) {
			return null;
		}
		return new ArrayList<>(deltas);
	}

	public void setDeltas(List<BigDecimal> deltas) {
		this.deltas = deltas;
	}

	public BigDecimal getVolatilityByOptionExpiry(long optionExpiry) throws TradistaBusinessException {
		if (points == null) {
			throw new TradistaBusinessException("The surface points list is null.");
		} else if (points.isEmpty()) {
			throw new TradistaBusinessException("The surface points list is empty.");
		}

		List<BigDecimal> volats = new ArrayList<>();

		for (SurfacePoint<Integer, BigDecimal, BigDecimal> p : points) {
			if (p.getxAxis() == optionExpiry) {
				volats.add(p.getzAxis());
			}
		}

		if (volats.isEmpty()) {
			throw new TradistaBusinessException(
					String.format("No values found for this option expiry: %d", optionExpiry));
		}

		BigDecimal avgVolat = BigDecimal.ZERO;

		for (BigDecimal v : volats) {
			avgVolat = avgVolat.add(v);
		}

		return avgVolat.divide(BigDecimal.valueOf(volats.size()), RoundingMode.HALF_EVEN);

	}

	@Override
	public FXVolatilitySurface clone() {
		FXVolatilitySurface fxVolatilitySurface = (FXVolatilitySurface) super.clone();
		if (deltas != null) {
			fxVolatilitySurface.deltas = new ArrayList<>(deltas);
		}
		return fxVolatilitySurface;
	}

}