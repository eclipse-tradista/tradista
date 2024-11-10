package org.eclipse.tradista.core.configuration.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.configuration.model.UIConfiguration;
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

public class ConfigurationPreFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	private static final String UI_CONFIGURATION_IS_NOT_YOURS = "This UI configuration is not yours, you are not allowed to update it.";

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	@Override
	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		Object[] parameters = ic.getParameters();
		if (parameters.length > 0) {
			if (parameters[0] instanceof UIConfiguration uiConfiguration) {
				StringBuilder errMsg = new StringBuilder();
				User user = getCurrentUser();
				if (!user.equals(uiConfiguration.getUser())) {
					errMsg.append(UI_CONFIGURATION_IS_NOT_YOURS);
				}
				if (errMsg.length() > 0) {
					throw new TradistaBusinessException(errMsg.toString());
				}
			}
			if (parameters[0] instanceof User user) {
				StringBuilder errMsg = new StringBuilder();
				User currentUser = getCurrentUser();
				if (user.getId() != 0) {
					User u = userBusinessDelegate.getUserById(user.getId());
					if (u == null) {
						errMsg.append(String.format("The User %s was not found.%n", user.getSurname()));
					} else if (!u.equals(currentUser)) {
						errMsg.append(UI_CONFIGURATION_IS_NOT_YOURS);
					}
				}
				if (!currentUser.equals(user)) {
					errMsg.append(UI_CONFIGURATION_IS_NOT_YOURS);
				}
				if (errMsg.length() > 0) {
					throw new TradistaBusinessException(errMsg.toString());
				}
			}
		}
	}

}