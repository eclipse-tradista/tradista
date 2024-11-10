package org.eclipse.tradista.security.equityoption.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.pricing.pricer.PricingParameterModule;
import org.eclipse.tradista.security.equity.model.Equity;

/********************************************************************************
 * Copyright (c) 2019 Olivier Asuncion
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

public class PricingParameterVolatilitySurfaceModule extends PricingParameterModule {

	private static final long serialVersionUID = 4583080254067074653L;

	private Map<Equity, EquityOptionVolatilitySurface> volatilitySurfaces;

	public PricingParameterVolatilitySurfaceModule() {
		volatilitySurfaces = new HashMap<Equity, EquityOptionVolatilitySurface>();
	}

	@Override
	public String getProductFamily() {
		return "security";
	}

	@Override
	public String getProductType() {
		return EquityOption.EQUITY_OPTION;
	}

	@SuppressWarnings("unchecked")
	public Map<Equity, EquityOptionVolatilitySurface> getVolatilitySurfaces() {
		return (Map<Equity, EquityOptionVolatilitySurface>) TradistaModelUtil.deepCopy(volatilitySurfaces);
	}

	public void setVolatilitySurfaces(Map<Equity, EquityOptionVolatilitySurface> volatilitySurfaces) {
		this.volatilitySurfaces = volatilitySurfaces;
	}

	public EquityOptionVolatilitySurface getEquityOptionVolatilitySurface(Equity equity) {
		if (equity == null || volatilitySurfaces == null) {
			return null;
		}
		return TradistaModelUtil.clone(volatilitySurfaces.get(equity));
	}

	@SuppressWarnings("unchecked")
	@Override
	public PricingParameterVolatilitySurfaceModule clone() {
		PricingParameterVolatilitySurfaceModule pricingParameterVolatilitySurfaceModule = (PricingParameterVolatilitySurfaceModule) super.clone();
		pricingParameterVolatilitySurfaceModule.volatilitySurfaces = (Map<Equity, EquityOptionVolatilitySurface>) TradistaModelUtil
				.deepCopy(volatilitySurfaces);
		return pricingParameterVolatilitySurfaceModule;
	}

}