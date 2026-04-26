package org.eclipse.tradista.core.marketdata.service;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
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

public class QuoteSetFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	private QuoteBusinessDelegate quoteBusinessDelegate;

	public QuoteSetFilteringInterceptor() {
		super();
		quoteBusinessDelegate = new QuoteBusinessDelegate();
	}

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		Object[] parameters = ic.getParameters();
		Method method = ic.getMethod();
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameters.length > 0) {
			if (parameterTypes[0].equals(QuoteSet.class)) {
				QuoteSet quoteSet = (QuoteSet) parameters[0];
				StringBuilder errMsg = new StringBuilder();
				if (quoteSet.getId() != 0) {
					QuoteSet qs = quoteBusinessDelegate.getQuoteSetById(quoteSet.getId());
					if (qs == null) {
						errMsg.append(String.format("The quote set %s was not found.%n", quoteSet.getName()));
					} else if (qs.getProcessingOrg() == null) {
						errMsg.append(
								String.format("This QuoteSet %d is a global one and you are not allowed to update it.",
										quoteSet.getId()));
					}
				}
				if (quoteSet.getProcessingOrg() != null
						&& !quoteSet.getProcessingOrg().equals(getCurrentUser().getProcessingOrg())) {
					errMsg.append(String.format("The processing org %s was not found.", quoteSet.getProcessingOrg()));
				}
				if (!errMsg.isEmpty()) {
					throw new TradistaBusinessException(errMsg.toString());
				}
			}
			if (parameterTypes[0].equals(Long.class)) {
				if (!method.getName().equals("getQuoteSetById") && !method.getName().equals("getQuoteSetsByPoId")) {
					Long quoteSetId = (Long) parameters[0];
					StringBuilder errMsg = new StringBuilder();
					if (quoteSetId != 0) {
						QuoteSet qs = quoteBusinessDelegate.getQuoteSetById(quoteSetId);
						if (qs == null) {
							errMsg.append(String.format("The quote set %d was not found.%n", quoteSetId));
						} else if (method.getName().equals("deleteQuoteSet")) {
							if (qs.getProcessingOrg() == null) {
								errMsg.append(String.format(
										"This QuoteSet %d is a global one and you are not allowed to delete it.",
										qs.getId()));
							}
						}
					}
					if (!errMsg.isEmpty()) {
						throw new TradistaBusinessException(errMsg.toString());
					}
				}
				if (method.getName().equals("getQuoteSetsByPoId")) {
					Long poId = (Long) parameters[0];
					if (poId != 0 && (getCurrentUser().getProcessingOrg() == null
							|| poId != getCurrentUser().getProcessingOrg().getId())) {
						if (getCurrentUser().getProcessingOrg() != null) {
							throw new TradistaBusinessException(
									"You are not allowed to access QuoteSets from this PO.");
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected Object postFilter(Object value) {
		if (value != null) {
			if (value instanceof Set) {
				Set<QuoteSet> pps = (Set<QuoteSet>) value;
				if (!pps.isEmpty()) {
					User user = getCurrentUser();
					value = pps.stream()
							.filter(b -> (b.getProcessingOrg() == null)
									|| (b.getProcessingOrg().equals(user.getProcessingOrg())))
							.collect(Collectors.toSet());
				}
			}
			if (value instanceof QuoteSet qs) {
				if (qs.getProcessingOrg() != null
						&& !qs.getProcessingOrg().equals(getCurrentUser().getProcessingOrg())) {
					value = null;
				}
			}
		}
		return value;
	}

}