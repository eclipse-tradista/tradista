package org.eclipse.tradista.security.repo.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.user.model.User;
import org.eclipse.tradista.security.repo.model.AllocationConfiguration;

import jakarta.ejb.EJB;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

/********************************************************************************
 * Copyright (c) 2024 Olivier Asuncion
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

public class AllocationConfigurationFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	@EJB
	protected AllocationConfigurationService allocationConfigurationService;

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	@Override
	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		Object[] parameters = ic.getParameters();
		if (parameters.length > 0) {
			StringBuilder errMsg = new StringBuilder();
			if (parameters[0] instanceof AllocationConfiguration allocConfig) {
				if (!allocConfig.getProcessingOrg().equals(getCurrentUser().getProcessingOrg())) {
					errMsg.append(String.format("You are not allowed to save this Allocation Configuration.%n"));
				}
			}
			if (!errMsg.isEmpty()) {
				throw new TradistaBusinessException(errMsg.toString());
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Object postFilter(Object value) {
		if (value != null) {
			User user = getCurrentUser();
			if (value instanceof AllocationConfiguration allocConfig) {
				if (!allocConfig.getProcessingOrg().equals(user.getProcessingOrg())) {
					value = null;
				}
			}
			if (value instanceof Set) {
				Set<AllocationConfiguration> allocConfigs = (Set<AllocationConfiguration>) value;
				value = allocConfigs.stream().filter(a -> (a.getProcessingOrg().equals(user.getProcessingOrg())))
						.collect(Collectors.toSet());
			}
		}
		return value;
	}

}