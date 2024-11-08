package org.eclipse.tradista.ir.irswapoption.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.pricing.pricer.PricingParameterModule;

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

	private Map<Index, SwaptionVolatilitySurface> volatilitySurfaces;

	public PricingParameterVolatilitySurfaceModule() {
		volatilitySurfaces = new HashMap<Index, SwaptionVolatilitySurface>();
	}

	@Override
	public String getProductFamily() {
		return "ir";
	}

	@Override
	public String getProductType() {
		return IRSwapOptionTrade.IR_SWAP_OPTION;
	}

	@SuppressWarnings("unchecked")
	public Map<Index, SwaptionVolatilitySurface> getVolatilitySurfaces() {
		if (volatilitySurfaces == null) {
			return null;
		}
		return (Map<Index, SwaptionVolatilitySurface>) TradistaModelUtil.deepCopy(volatilitySurfaces);
	}

	public void setVolatilitySurfaces(Map<Index, SwaptionVolatilitySurface> volatilitySurfaces) {
		this.volatilitySurfaces = volatilitySurfaces;
	}

	public SwaptionVolatilitySurface getSwaptionVolatilitySurface(Index index) {
		if (index == null || volatilitySurfaces == null) {
			return null;
		}
		return TradistaModelUtil.clone(volatilitySurfaces.get(index));
	}

	@SuppressWarnings("unchecked")
	@Override
	public PricingParameterVolatilitySurfaceModule clone() {
		PricingParameterVolatilitySurfaceModule pricingParameterVolatilitySurfaceModule = (PricingParameterVolatilitySurfaceModule) super.clone();
		if (volatilitySurfaces != null) {
			pricingParameterVolatilitySurfaceModule.volatilitySurfaces = (Map<Index, SwaptionVolatilitySurface>) TradistaModelUtil
					.deepCopy(volatilitySurfaces);
		}
		return pricingParameterVolatilitySurfaceModule;
	}

}