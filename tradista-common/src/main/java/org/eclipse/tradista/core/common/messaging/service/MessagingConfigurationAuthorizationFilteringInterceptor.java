package org.eclipse.tradista.core.common.messaging.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

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

public class MessagingConfigurationAuthorizationFilteringInterceptor
		extends TradistaAuthorizationFilteringInterceptor {

	private static final String ONLY_ADMINS_ALLOWED = "Only administrator users are allowed to update messaging configuration.";

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	@Override
	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		// When preFilter is called by TradistaAuthorizationFilteringInterceptor,
		// user != null and user.getProcessingOrg() != null, meaning the user is NOT an admin.
		throw new TradistaBusinessException(ONLY_ADMINS_ALLOWED);
	}

}
