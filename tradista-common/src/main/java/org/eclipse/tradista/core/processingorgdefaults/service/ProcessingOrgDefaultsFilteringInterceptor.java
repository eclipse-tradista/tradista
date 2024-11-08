package org.eclipse.tradista.core.processingorgdefaults.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.processingorgdefaults.model.ProcessingOrgDefaults;
import org.eclipse.tradista.core.processingorgdefaults.model.ProcessingOrgDefaultsModule;
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

public class ProcessingOrgDefaultsFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	protected ProcessingOrgDefaultsBusinessDelegate poDefaultsBusinessDelegate;

	public ProcessingOrgDefaultsFilteringInterceptor() {
		poDefaultsBusinessDelegate = new ProcessingOrgDefaultsBusinessDelegate();
	}

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	@Override
	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		Object[] parameters = ic.getParameters();
		if (parameters.length > 0) {
			StringBuilder errMsg = new StringBuilder();
			if (parameters[0] instanceof Long poId) {
				if (poId != getCurrentUser().getProcessingOrg().getId()) {
					errMsg.append(String.format(
							"You are not allowed to access the Processing Org Defaults of this Processing Org.%n"));

				}
			}
			if (parameters[0] instanceof ProcessingOrgDefaults pod) {
				if (pod.getModules() != null && pod.getModules().isEmpty()) {
					for (ProcessingOrgDefaultsModule module : pod.getModules()) {
						ProcessingOrgDefaultsModuleValidator validator = poDefaultsBusinessDelegate
								.getValidator(module);
						validator.checkAccess(module, errMsg);
					}
				}
			}
			if (errMsg.length() > 0) {
				throw new TradistaBusinessException(errMsg.toString());
			}
		}
	}

}