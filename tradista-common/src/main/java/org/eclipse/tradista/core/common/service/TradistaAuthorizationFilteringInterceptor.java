package org.eclipse.tradista.core.common.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.user.model.User;
import org.eclipse.tradista.core.user.service.UserBusinessDelegate;

import jakarta.annotation.Resource;
import jakarta.ejb.EJBContext;
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

public abstract class TradistaAuthorizationFilteringInterceptor {

	protected UserBusinessDelegate userBusinessDelegate;

	@Resource
	private EJBContext ctx;

	public TradistaAuthorizationFilteringInterceptor() {
		userBusinessDelegate = new UserBusinessDelegate();
	}

	protected Object proceed(InvocationContext ic) throws Exception {
		User user = getCurrentUser();
		// When the client is the scheduler (for example, the
		// JobExecutionHistoryTriggerListener class), the user is null.
		// In this case, it is better to bypass the filters as the scheduler is already
		// identified (started from the Core server)
		if (user != null) {
			if (user.getProcessingOrg() != null) {
				preFilter(ic);
			}
		}
		Object value = ic.proceed();
		if (user != null) {
			if (user.getProcessingOrg() != null) {
				value = postFilter(value);
			}
		}
		return value;
	}

	public User getCurrentUser() {
		User user = null;
		if (ctx.getContextData().get(SecurityUtil.CURRENT_USER) == null) {
			user = userBusinessDelegate.getUserByLogin(ctx.getCallerPrincipal().getName());
			ctx.getContextData().put(SecurityUtil.CURRENT_USER, user);
		} else {
			user = (User) ctx.getContextData().get(SecurityUtil.CURRENT_USER);
		}
		return user;
	}

	protected Object postFilter(Object value) throws TradistaBusinessException {
		return value;
	}

	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
	}

}