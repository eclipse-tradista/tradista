package org.eclipse.tradista.core.marketdata.service;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.marketdata.model.FeedConfig;
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

public class FeedConfigFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	private FeedBusinessDelegate feedBusinessDelegate;

	public FeedConfigFilteringInterceptor() {
		super();
		feedBusinessDelegate = new FeedBusinessDelegate();
	}

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		Object[] parameters = ic.getParameters();
		if (parameters.length > 0) {
			if (parameters[0] instanceof FeedConfig) {
				FeedConfig feedConfig = (FeedConfig) parameters[0];
				StringBuilder errMsg = new StringBuilder();
				if (feedConfig.getId() != 0) {
					FeedConfig fc = feedBusinessDelegate.getFeedConfigById(feedConfig.getId());
					if (fc == null) {
						errMsg.append(String.format("The feed config %s was not found.%n", feedConfig.getName()));
					} else if (fc.getProcessingOrg() == null) {
						errMsg.append(String.format(
								"This feed config %d is a global one and you are not allowed to update it.",
								feedConfig.getId()));
					}
				}
				if (feedConfig.getProcessingOrg() != null
						&& !feedConfig.getProcessingOrg().equals(getCurrentUser().getProcessingOrg())) {
					errMsg.append(String.format("The processing org %s was not found.", feedConfig.getProcessingOrg()));
				}
				if (errMsg.length() > 0) {
					throw new TradistaBusinessException(errMsg.toString());
				}
			}
			if (parameters[0] instanceof Long) {
				Method method = ic.getMethod();
				if (method.getName().equals("deleteFeedConfig")) {
					Long feedConfigId = (Long) parameters[0];
					StringBuilder errMsg = new StringBuilder();
					if (feedConfigId != 0) {
						FeedConfig fc = feedBusinessDelegate.getFeedConfigById(feedConfigId);
						if (fc == null) {
							errMsg.append(String.format("The feed config %d was not found.%n", feedConfigId));
						} else if (fc.getProcessingOrg() == null) {
							errMsg.append(String.format(
									"This feed config %d is a global one and you are not allowed to delete it.",
									feedConfigId));
						}
					}
					if (errMsg.length() > 0) {
						throw new TradistaBusinessException(errMsg.toString());
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected Object postFilter(Object value) throws TradistaBusinessException {
		if (value != null) {
			if (value instanceof Set) {
				Set<FeedConfig> configs = (Set<FeedConfig>) value;
				if (!configs.isEmpty()) {
					User user = getCurrentUser();
					value = configs.stream()
							.filter(b -> (b.getProcessingOrg() == null)
									|| (b.getProcessingOrg().equals(user.getProcessingOrg())))
							.collect(Collectors.toSet());
				}
			}
		}
		return value;
	}

}