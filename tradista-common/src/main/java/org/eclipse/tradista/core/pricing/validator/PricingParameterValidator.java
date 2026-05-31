package org.eclipse.tradista.core.pricing.validator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.util.TradistaUtil;
import org.eclipse.tradista.core.marketdata.model.Curve;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.pricer.PricingParameterModule;
import org.eclipse.tradista.core.pricing.service.PricingParameterModuleValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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

public class PricingParameterValidator implements Serializable {

	private static final long serialVersionUID = -2420915609315572648L;

	private static final Logger logger = LoggerFactory.getLogger(PricingParameterValidator.class);

	private Map<String, PricingParameterModuleValidator> validators;

	public PricingParameterValidator() {
		validators = new HashMap<>();
		PricingParameterModuleValidator validator = null;
		try {
			validator = TradistaUtil.getInstance(PricingParameterModuleValidator.class,
					"org.eclipse.tradista.security.equityoption.validator.PricingParameterDividendYieldCurveModuleValidator");
			validators.put("org.eclipse.tradista.security.equityoption.model.PricingParameterDividendYieldCurveModule",
					validator);
		} catch (TradistaTechnicalException _) {
			logger.info("PricingParameterDividendYieldCurveModuleValidator not found, skipping.");
		}
		try {
			validator = TradistaUtil.getInstance(PricingParameterModuleValidator.class,
					"org.eclipse.tradista.fx.common.validator.PricingParameterUnrealizedPnlCalculationModuleValidator");
			validators.put("org.eclipse.tradista.fx.common.model.PricingParameterUnrealizedPnlCalculationModule",
					validator);
		} catch (TradistaTechnicalException _) {
			logger.info("PricingParameterUnrealizedPnlCalculationModuleValidator not found, skipping.");
		}
		try {
			validator = TradistaUtil.getInstance(PricingParameterModuleValidator.class,
					"org.eclipse.tradista.fx.fxoption.validator.PricingParameterVolatilitySurfaceModuleValidator");
			validators.put("org.eclipse.tradista.fx.fxoption.model.PricingParameterVolatilitySurfaceModule", validator);
		} catch (TradistaTechnicalException _) {
			logger.info("FX Option PricingParameterVolatilitySurfaceModuleValidator not found, skipping.");
		}
		try {
			validator = TradistaUtil.getInstance(PricingParameterModuleValidator.class,
					"org.eclipse.tradista.ir.irswapoption.validator.PricingParameterVolatilitySurfaceModuleValidator");
			validators.put("org.eclipse.tradista.ir.irswapoption.model.PricingParameterVolatilitySurfaceModule",
					validator);
		} catch (TradistaTechnicalException _) {
			logger.info("IR Swap Option PricingParameterVolatilitySurfaceModuleValidator not found, skipping.");
		}
		try {
			validator = TradistaUtil.getInstance(PricingParameterModuleValidator.class,
					"org.eclipse.tradista.security.equityoption.validator.PricingParameterVolatilitySurfaceModuleValidator");
			validators.put("org.eclipse.tradista.security.equityoption.model.PricingParameterVolatilitySurfaceModule",
					validator);
		} catch (TradistaTechnicalException _) {
			logger.info("Equity Option PricingParameterVolatilitySurfaceModuleValidator not found, skipping.");
		}
	}

	public void validatePricingParameter(PricingParameter param) throws TradistaBusinessException {
		if (param == null) {
			throw new TradistaBusinessException("The Pricing Parameter Set cannot be null.");
		}
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isEmpty(param.getName())) {
			errMsg.append(String.format("Please select a Pricing Parameters Set Name.%n"));
		} else {
			if (param.getName().length() > 20) {
				errMsg.append(String.format("The Pricing Parameters Set Name cannot exceed 20 characters.%n"));
			}
		}

		if (param.getQuoteSet() == null) {
			errMsg.append(String.format("Please select a QuoteSet.%n"));
		} else {
			if (param.getProcessingOrg() != null && param.getQuoteSet().getProcessingOrg() != null
					&& !param.getQuoteSet().getProcessingOrg().equals(param.getProcessingOrg())) {
				errMsg.append(
						String.format("the Pricing Parameters Set's PO and the QuoteSet's PO should be the same.%n"));
			}
			if (param.getProcessingOrg() == null && param.getQuoteSet().getProcessingOrg() != null) {
				errMsg.append(String
						.format("If the Pricing Parameters Set is a global one, the QuoteSet must also be global.%n"));
			}
		}

		validateCurves(param.getDiscountCurves(), param, "Discount", errMsg);
		validateCurves(param.getIndexCurves(), param, "Index", errMsg);
		validateCurves(param.getFxCurves(), param, "FX", errMsg);

		if (param.getModules() != null && !param.getModules().isEmpty()) {
			for (PricingParameterModule module : param.getModules()) {
				PricingParameterModuleValidator validator = validators.get(module.getClass().getName());
				if (validator != null) {
					try {
						validator.validateModule(module, param.getProcessingOrg());
					} catch (TradistaBusinessException tbe) {
						errMsg.append(tbe.getMessage());
					}
				}
			}
		}

		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

	private void validateCurves(Map<?, ? extends Curve<?, ?>> curves, PricingParameter param, String type,
			StringBuilder errMsg) {
		if (curves != null && !curves.isEmpty()) {
			for (Curve<?, ?> curve : curves.values()) {
				if (param.getProcessingOrg() != null && curve.getProcessingOrg() != null
						&& !curve.getProcessingOrg().equals(param.getProcessingOrg())) {
					errMsg.append(String.format(
							"the Pricing Parameters Set's PO and the %s curve %s's PO should be the same.%n", type,
							curve));
				}
				if (param.getProcessingOrg() == null && curve.getProcessingOrg() != null) {
					errMsg.append(String.format(
							"If the Pricing Parameters Set is a global one, the %s curve %s must also be global.%n",
							type, curve));
				}
			}
		}
	}

	public PricingParameterModuleValidator getValidator(PricingParameterModule module) {
		return validators.get(module.getClass().getName());
	}

}