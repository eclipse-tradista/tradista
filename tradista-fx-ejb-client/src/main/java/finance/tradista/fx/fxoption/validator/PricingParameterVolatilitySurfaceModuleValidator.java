package finance.tradista.fx.fxoption.validator;

import finance.tradista.core.common.exception.TradistaBusinessException;
import finance.tradista.core.legalentity.model.LegalEntity;
import finance.tradista.core.pricing.pricer.PricingParameterModule;
import finance.tradista.core.pricing.service.PricingParameterModuleValidator;
import finance.tradista.fx.fxoption.model.FXVolatilitySurface;
import finance.tradista.fx.fxoption.model.PricingParameterVolatilitySurfaceModule;
import finance.tradista.fx.fxoption.service.FXVolatilitySurfaceBusinessDelegate;

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

public class PricingParameterVolatilitySurfaceModuleValidator implements PricingParameterModuleValidator {

	private FXVolatilitySurfaceBusinessDelegate fxVolatilitySurfaceBusinessDelegate;

	public PricingParameterVolatilitySurfaceModuleValidator() {
		fxVolatilitySurfaceBusinessDelegate = new FXVolatilitySurfaceBusinessDelegate();
	}

	@Override
	public void validateModule(PricingParameterModule module, LegalEntity po) throws TradistaBusinessException {
		PricingParameterVolatilitySurfaceModule mod = (PricingParameterVolatilitySurfaceModule) module;
		StringBuilder errMsg = new StringBuilder();
		if (mod.getVolatilitySurfaces() != null && !mod.getVolatilitySurfaces().isEmpty()) {
			for (FXVolatilitySurface surface : mod.getVolatilitySurfaces().values()) {
				if (po != null && surface.getProcessingOrg() != null && !surface.getProcessingOrg().equals(po)) {
					errMsg.append(String.format(
							"the Pricing Parameters Set's PO and the FX Volatility Surface %s's PO should be the same.%n",
							surface));
				}
				if (po == null && surface.getProcessingOrg() != null) {
					errMsg.append(String.format(
							"If the Pricing Parameters Set is a global one, the FX Volatility Surface %s must also be global.%n",
							surface));
				}
				if (po != null && surface.getProcessingOrg() == null) {
					errMsg.append(String.format(
							"If the FX Volatility Surface %s is a global one, the Pricing Parameters Set must also be global.%n",
							surface));
				}
			}
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

	@Override
	public void checkAccess(PricingParameterModule module, StringBuilder errMsg) {
		PricingParameterVolatilitySurfaceModule mod = (PricingParameterVolatilitySurfaceModule) module;
		if (mod.getVolatilitySurfaces() != null && !mod.getVolatilitySurfaces().isEmpty()) {
			for (FXVolatilitySurface surface : mod.getVolatilitySurfaces().values()) {
				FXVolatilitySurface vol = null;
				try {
					vol = fxVolatilitySurfaceBusinessDelegate.getFXVolatilitySurfaceById(surface.getId());
				} catch (TradistaBusinessException tbe) {
					// Not expected here.
				}
				if (vol == null) {
					errMsg.append(String.format("the fx volatility surface %s was not found.%n", surface.getName()));
				}
			}
		}
	}

}