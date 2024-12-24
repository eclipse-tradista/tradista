package org.eclipse.tradista.fx.fxoption.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.currency.model.CurrencyPair;
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

	private static final long serialVersionUID = 6630400213476177724L;

	private Map<CurrencyPair, FXVolatilitySurface> volatilitySurfaces;

	public PricingParameterVolatilitySurfaceModule() {
		volatilitySurfaces = new HashMap<CurrencyPair, FXVolatilitySurface>();
	}

	@Override
	public String getProductFamily() {
		return "fx";
	}

	@Override
	public String getProductType() {
		return FXOptionTrade.FX_OPTION;
	}

	@SuppressWarnings("unchecked")
	public Map<CurrencyPair, FXVolatilitySurface> getVolatilitySurfaces() {
		return (Map<CurrencyPair, FXVolatilitySurface>) TradistaModelUtil.deepCopy(volatilitySurfaces);
	}

	public void setVolatilitySurfaces(Map<CurrencyPair, FXVolatilitySurface> volatilitySurfaces) {
		this.volatilitySurfaces = volatilitySurfaces;
	}

	public FXVolatilitySurface getFXVolatilitySurface(CurrencyPair currencyPair) {
		if (currencyPair == null || volatilitySurfaces == null) {
			return null;
		}
		return TradistaModelUtil.clone(volatilitySurfaces.get(currencyPair));
	}

	@SuppressWarnings("unchecked")
	@Override
	public PricingParameterVolatilitySurfaceModule clone() {
		PricingParameterVolatilitySurfaceModule pricingParameterVolatilitySurfaceModule = (PricingParameterVolatilitySurfaceModule) super.clone();
		pricingParameterVolatilitySurfaceModule.volatilitySurfaces = (Map<CurrencyPair, FXVolatilitySurface>) TradistaModelUtil
				.deepCopy(volatilitySurfaces);
		return pricingParameterVolatilitySurfaceModule;
	}

}