package org.eclipse.tradista.core.marketdata.service;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
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

public class QuoteValueFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	private QuoteBusinessDelegate quoteBusinessDelegate;

	public QuoteValueFilteringInterceptor() {
		super();
		quoteBusinessDelegate = new QuoteBusinessDelegate();
	}

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		Object[] parameters = ic.getParameters();
		long quoteSetId = (long) parameters[0];
		StringBuilder errMsg = new StringBuilder();
		if (quoteSetId != 0) {
			QuoteSet qs = quoteBusinessDelegate.getQuoteSetById(quoteSetId);
			if (qs == null) {
				errMsg.append(String.format("The quote set %d was not found.%n", quoteSetId));
			} else if (qs.getProcessingOrg() == null) {
				errMsg.append(String.format(
						"This QuoteSet %d is a global one and you are not allowed to add quote values on it.",
						quoteSetId));
			}
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

	@SuppressWarnings("unchecked")
	protected Object postFilter(Object value) {
		if (value != null) {
			if (value instanceof List) {
				List<QuoteValue> qvs = (List<QuoteValue>) value;
				if (!qvs.isEmpty()) {
					User user = getCurrentUser();
					value = qvs.stream()
							.filter(qv -> (qv.getQuoteSet().getProcessingOrg() == null)
									|| (qv.getQuoteSet().getProcessingOrg().equals(user.getProcessingOrg())))
							.collect(Collectors.toList());
				}
			}
		}
		return value;
	}

}