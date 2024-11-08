package org.eclipse.tradista.core.user.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
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

public class UserFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		Object[] parameters = ic.getParameters();
		if (parameters.length > 0 && parameters[0] instanceof User) {
			User user = (User) parameters[0];
			StringBuilder errMsg = new StringBuilder();
			User currentUser = getCurrentUser();
			if (user.getId() != 0) {
				User u = userBusinessDelegate.getUserById(user.getId());
				if (u == null) {
					errMsg.append(String.format("The user %s was not found.%n", user.getLogin()));
				} else if (u.getProcessingOrg() == null) {
					errMsg.append(String.format("The user %s is a global one and you are not allowed to update it.%n",
							user.getLogin()));
				}
			}
			if (user.getProcessingOrg() != null && !user.getProcessingOrg().equals(currentUser.getProcessingOrg())) {
				errMsg.append(String.format("The processing org %s was not found.", user.getProcessingOrg()));
			}
			if (errMsg.length() > 0) {
				throw new TradistaBusinessException(errMsg.toString());
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected Object postFilter(Object value) {
		if (value != null) {
			User currentUser = getCurrentUser();
			if (value instanceof Set) {
				Set<User> users = (Set<User>) value;
				if (!users.isEmpty()) {
					value = users.stream()
							.filter(u -> ((currentUser.getProcessingOrg() == null) || ((u.getProcessingOrg() != null)
									&& (u.getProcessingOrg().equals(currentUser.getProcessingOrg())))))
							.collect(Collectors.toSet());
				}
			}
			if (value instanceof User) {
				User user = (User) value;
				if ((currentUser.getProcessingOrg() != null) && ((user.getProcessingOrg() == null)
						|| (!user.getProcessingOrg().equals(currentUser.getProcessingOrg())))) {
					value = null;
				}
			}
		}
		return value;
	}

}