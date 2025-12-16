package org.eclipse.tradista.core.mapping.service;

import java.lang.reflect.Method;
import java.util.Objects;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.mapping.model.InterfaceMappingSet;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

/********************************************************************************
 * Copyright (c) 2025 Olivier Asuncion
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

public class MappingAuthorizationFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	@Override
	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		Object[] parameters = ic.getParameters();
		Method method = ic.getMethod();
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes[0].equals(InterfaceMappingSet.class)) {
			InterfaceMappingSet ims = (InterfaceMappingSet) parameters[0];
			StringBuilder errMsg = new StringBuilder();
			if (!Objects.equals(ims.getProcessingOrg(), getCurrentUser().getProcessingOrg())) {
				errMsg.append(String.format("The interface %s was not found.%n", ims.getInterfaceName()));
			}
			if (!errMsg.isEmpty()) {
				throw new TradistaBusinessException(errMsg.toString());
			}
		}
		if (parameterTypes.length >= 5 && parameterTypes[4].equals(Long.class)) {
			long poId = (long) parameters[4];
			StringBuilder errMsg = new StringBuilder();
			if (getCurrentUser().getProcessingOrg() != null && (getCurrentUser().getProcessingOrg().getId() != poId)) {
				errMsg.append(String.format("The value was not found.%n"));
			}
			if (!errMsg.isEmpty()) {
				throw new TradistaBusinessException(errMsg.toString());
			}
		}
	}
}