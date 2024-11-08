package org.eclipse.tradista.ir.irswapoption.validator;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.pricing.pricer.PricingParameterModule;
import org.eclipse.tradista.core.pricing.service.PricingParameterModuleValidator;
import org.eclipse.tradista.ir.irswapoption.model.PricingParameterVolatilitySurfaceModule;
import org.eclipse.tradista.ir.irswapoption.model.SwaptionVolatilitySurface;
import org.eclipse.tradista.ir.irswapoption.service.SwaptionVolatilitySurfaceBusinessDelegate;

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

	private SwaptionVolatilitySurfaceBusinessDelegate swaptionVolatilitySurfaceBusinessDelegate;

	public PricingParameterVolatilitySurfaceModuleValidator() {
		swaptionVolatilitySurfaceBusinessDelegate = new SwaptionVolatilitySurfaceBusinessDelegate();
	}

	@Override
	public void validateModule(PricingParameterModule module, LegalEntity po) throws TradistaBusinessException {
		PricingParameterVolatilitySurfaceModule mod = (PricingParameterVolatilitySurfaceModule) module;
		StringBuilder errMsg = new StringBuilder();
		if (mod.getVolatilitySurfaces() != null && !mod.getVolatilitySurfaces().isEmpty()) {
			for (SwaptionVolatilitySurface surface : mod.getVolatilitySurfaces().values()) {
				if (po != null && surface.getProcessingOrg() != null && !surface.getProcessingOrg().equals(po)) {
					errMsg.append(String.format(
							"the Pricing Parameters Set's PO and the Swaption Volatility Surface %s's PO should be the same.%n",
							surface));
				}
				if (po == null && surface.getProcessingOrg() != null) {
					errMsg.append(String.format(
							"If the Pricing Parameters Set is a global one, the Swaption Volatility Surface %s must also be global.%n",
							surface));
				}
				if (po != null && surface.getProcessingOrg() == null) {
					errMsg.append(String.format(
							"If the Swaption Volatility Surface %s is a global one, the Pricing Parameters Set must also be global.%n",
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
			for (SwaptionVolatilitySurface surface : mod.getVolatilitySurfaces().values()) {
				SwaptionVolatilitySurface vol = null;
				try {
					vol = swaptionVolatilitySurfaceBusinessDelegate.getSwaptionVolatilitySurfaceById(surface.getId());
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