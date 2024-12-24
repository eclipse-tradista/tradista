package org.eclipse.tradista.security.equityoption.validator;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.service.InterestRateCurveBusinessDelegate;
import org.eclipse.tradista.core.pricing.pricer.PricingParameterModule;
import org.eclipse.tradista.core.pricing.service.PricingParameterModuleValidator;
import org.eclipse.tradista.security.equityoption.model.PricingParameterDividendYieldCurveModule;

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

public class PricingParameterDividendYieldCurveModuleValidator implements PricingParameterModuleValidator {

	private InterestRateCurveBusinessDelegate interestRateCurveBusinessDelegate;

	public PricingParameterDividendYieldCurveModuleValidator() {
		interestRateCurveBusinessDelegate = new InterestRateCurveBusinessDelegate();
	}

	@Override
	public void validateModule(PricingParameterModule module, LegalEntity po) throws TradistaBusinessException {
		PricingParameterDividendYieldCurveModule mod = (PricingParameterDividendYieldCurveModule) module;
		StringBuilder errMsg = new StringBuilder();
		if (mod.getDividendYieldCurves() != null && !mod.getDividendYieldCurves().isEmpty()) {
			for (InterestRateCurve curve : mod.getDividendYieldCurves().values()) {
				if (po != null && curve.getProcessingOrg() != null && !curve.getProcessingOrg().equals(po)) {
					errMsg.append(String.format(
							"the Pricing Parameters Set's PO and the Dividend Yield curve %s's PO should be the same.%n",
							curve));
				}
				if (po == null && curve.getProcessingOrg() != null) {
					errMsg.append(String.format(
							"If the Pricing Parameters Set is a global one, the Dividend Yield curve %s must also be global.%n",
							curve));
				}
				if (po != null && curve.getProcessingOrg() == null) {
					errMsg.append(String.format(
							"If the Dividend Yield curve %s is a global one, the Pricing Parameters Set must also be global.%n",
							curve));
				}
			}
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

	@Override
	public void checkAccess(PricingParameterModule module, StringBuilder errMsg) {
		PricingParameterDividendYieldCurveModule mod = (PricingParameterDividendYieldCurveModule) module;
		if (mod.getDividendYieldCurves() != null && !mod.getDividendYieldCurves().isEmpty()) {
			for (InterestRateCurve curve : mod.getDividendYieldCurves().values()) {
				InterestRateCurve c = null;
				try {
					c = interestRateCurveBusinessDelegate.getInterestRateCurveById(curve.getId());
				} catch (TradistaBusinessException tbe) {
					// Not expected here.
				}
				if (c == null) {
					errMsg.append(String.format("the Dividend Yield curve %s was not found.%n", curve));
				}
			}
		}
	}

}