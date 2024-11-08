package org.eclipse.tradista.core.pricing.service;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.service.FXCurveBusinessDelegate;
import org.eclipse.tradista.core.marketdata.service.InterestRateCurveBusinessDelegate;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.pricer.PricingParameterModule;
import org.eclipse.tradista.core.user.model.User;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

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

public class PricingParameterFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	private QuoteBusinessDelegate quoteBusinessDelegate;

	private PricerBusinessDelegate pricerBusinessDelegate;

	private InterestRateCurveBusinessDelegate interestRateCurveBusinessDelegate;

	private FXCurveBusinessDelegate fxCurveBusinessDelegate;

	public PricingParameterFilteringInterceptor() {
		super();
		quoteBusinessDelegate = new QuoteBusinessDelegate();
		pricerBusinessDelegate = new PricerBusinessDelegate();
		interestRateCurveBusinessDelegate = new InterestRateCurveBusinessDelegate();
		fxCurveBusinessDelegate = new FXCurveBusinessDelegate();
	}

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	@Override
	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		Object[] parameters = ic.getParameters();
		if (parameters.length > 0) {
			Method method = ic.getMethod();
			StringBuilder errMsg = new StringBuilder();
			if (parameters[0] instanceof PricingParameter pricingParameter) {
				if (pricingParameter.getId() != 0) {
					PricingParameter pp = pricerBusinessDelegate.getPricingParameterById(pricingParameter.getId());
					if (pp == null) {
						errMsg.append(String.format("The pricing parameters set %s was not found.%n",
								pricingParameter.getName()));
					} else {
						if (pp.getProcessingOrg() == null) {
							errMsg.append(String.format(
									"This Pricing Parameters Set %d is a global one and you are not allowed to delete it.",
									pp.getId()));
						}
					}
				}
				if (!pricingParameter.getProcessingOrg().equals(getCurrentUser().getProcessingOrg())) {
					errMsg.append(
							String.format("The processing org %s was not found.", pricingParameter.getProcessingOrg()));
				}
				QuoteSet qs = quoteBusinessDelegate.getQuoteSetById(pricingParameter.getQuoteSet().getId());
				if (qs == null) {
					errMsg.append(
							String.format("The quote set %s was not found.", pricingParameter.getQuoteSet().getName()));
				}
				if (pricingParameter.getDiscountCurves() != null && !pricingParameter.getDiscountCurves().isEmpty()) {
					for (InterestRateCurve curve : pricingParameter.getDiscountCurves().values()) {
						InterestRateCurve c = interestRateCurveBusinessDelegate.getInterestRateCurveById(curve.getId());
						if (c == null) {
							errMsg.append(String.format("the Discount curve %s was not found.%n", curve));
						}
					}
				}
				if (pricingParameter.getIndexCurves() != null && !pricingParameter.getIndexCurves().isEmpty()) {
					for (InterestRateCurve curve : pricingParameter.getIndexCurves().values()) {
						InterestRateCurve c = interestRateCurveBusinessDelegate.getInterestRateCurveById(curve.getId());
						if (c == null) {
							errMsg.append(String.format("the Index curve %s was not found.%n", curve));
						}
					}
				}
				if (pricingParameter.getFxCurves() != null && !pricingParameter.getFxCurves().isEmpty()) {
					for (FXCurve curve : pricingParameter.getFxCurves().values()) {
						FXCurve c = fxCurveBusinessDelegate.getFXCurveById(curve.getId());
						if (c == null) {
							errMsg.append(String.format("the FX curve %s was not found.%n", curve));
						}
					}
				}
				if (pricingParameter.getModules() != null && pricingParameter.getModules().isEmpty()) {
					for (PricingParameterModule module : pricingParameter.getModules()) {
						PricingParameterModuleValidator validator = pricerBusinessDelegate.getValidator(module);
						validator.checkAccess(module, errMsg);
					}
				}
			}
			if (parameters[0] instanceof Long pricingParameterId) {
				if (method.getName().equals("deletePricingParameter")) {
					if (pricingParameterId != 0) {
						PricingParameter pp = pricerBusinessDelegate.getPricingParameterById(pricingParameterId);
						if (pp == null) {
							errMsg.append(String.format("The pricing parameters set %d was not found.%n",
									pricingParameterId));
						} else if (pp.getProcessingOrg() == null) {
							errMsg.append(String.format(
									"This Pricing Parameters Set %d is a global one and you are not allowed to delete it.",
									pp.getId()));
						}
					}
				}
			}
			if (parameters[0] instanceof String && parameters[1] instanceof Long poId) {
				if (getCurrentUser().getProcessingOrg().getId() != 0) {
					if (poId != getCurrentUser().getProcessingOrg().getId()) {
						errMsg.append(String
								.format("You are not allowed to access Pricing Parameters of this Processing Org.%n"));
					}
				}
			}
			if (errMsg.length() > 0) {
				throw new TradistaBusinessException(errMsg.toString());
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Object postFilter(Object value) {
		if (value != null) {
			if (value instanceof Set) {
				Set<PricingParameter> pps = (Set<PricingParameter>) value;
				if (!pps.isEmpty()) {
					User user = getCurrentUser();
					value = pps.stream()
							.filter(pp -> pp.getProcessingOrg() == null
									|| pp.getProcessingOrg().equals(user.getProcessingOrg()))
							.collect(Collectors.toSet());
				}
			}
			if (value instanceof PricingParameter pp) {
				if (pp.getProcessingOrg() != null
						&& !pp.getProcessingOrg().equals(getCurrentUser().getProcessingOrg())) {
					value = null;
				}
			}
		}
		return value;
	}

}