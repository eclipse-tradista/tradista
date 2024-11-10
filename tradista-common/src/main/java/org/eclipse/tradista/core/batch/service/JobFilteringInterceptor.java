package org.eclipse.tradista.core.batch.service;

import java.lang.reflect.Method;

import org.eclipse.tradista.core.batch.model.TradistaJobExecution;
import org.eclipse.tradista.core.batch.model.TradistaJobInstance;
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

public class JobFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	private BatchBusinessDelegate batchBusinessDelegate;

	public JobFilteringInterceptor() {
		super();
		batchBusinessDelegate = new BatchBusinessDelegate();
	}

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		Object[] parameters = ic.getParameters();
		if (parameters.length > 0) {
			if (parameters[0] instanceof TradistaJobInstance) {
				TradistaJobInstance jobInstance = (TradistaJobInstance) parameters[0];
				StringBuilder errMsg = new StringBuilder();
				User user = getCurrentUser();
				if (jobInstance.getProcessingOrg() == null) {
					errMsg.append(String.format(
							"This job instance %s is a global one and you are not allowed to update it.%n",
							jobInstance.getName()));
				}
				if (jobInstance.getProcessingOrg() != null
						&& !jobInstance.getProcessingOrg().equals(user.getProcessingOrg())) {
					errMsg.append(
							String.format("The processing org %s was not found.", jobInstance.getProcessingOrg()));
				}
				if (errMsg.length() > 0) {
					throw new TradistaBusinessException(errMsg.toString());
				}
			}
			if (parameters.length == 1 && parameters[0] instanceof String) {
				Method method = ic.getMethod();
				StringBuilder errMsg = new StringBuilder();
				if (method.getName().equals("stopJobExecution")) {
					String jobExecutionId = (String) parameters[0];
					TradistaJobExecution jobExecution = batchBusinessDelegate.getJobExecutionById(jobExecutionId);
					if (jobExecution == null) {
						errMsg.append(String.format(
								"This job instance %s is a global one and you are not allowed to update it.%n"));
					}
				} else {
					String po = (String) parameters[0];
					String userPo = getCurrentUser().getProcessingOrg() != null
							? getCurrentUser().getProcessingOrg().getShortName()
							: null;
					if (po == null) {
						errMsg.append(String.format(
								"This job instance %s is a global one and you are not allowed to update it.%n"));
					}
					if (po != null && !po.equals(userPo)) {
						errMsg.append(String.format("The processing org %s was not found.", po));
					}
					if (errMsg.length() > 0) {
						throw new TradistaBusinessException(errMsg.toString());
					}
				}
			}
			if (parameters.length > 1 && parameters[1] instanceof String) {
				String po = (String) parameters[1];
				StringBuilder errMsg = new StringBuilder();
				String userPo = getCurrentUser().getProcessingOrg() != null
						? getCurrentUser().getProcessingOrg().getShortName()
						: null;
				if (po == null) {
					errMsg.append(String
							.format("This job instance %s is a global one and you are not allowed to update it.%n"));
				}
				if (po != null && !po.equals(userPo)) {
					errMsg.append(String.format("The processing org %s was not found.", po));
				}
				if (errMsg.length() > 0) {
					throw new TradistaBusinessException(errMsg.toString());
				}
			}
		}
	}

	protected Object postFilter(Object value) {
		if (value != null) {
			User user = getCurrentUser();
			if (value instanceof TradistaJobExecution) {
				TradistaJobExecution jobExecution = (TradistaJobExecution) value;
				if (!jobExecution.getJobInstance().getProcessingOrg().equals(user.getProcessingOrg())) {
					value = null;
				}
			}
		}
		return value;
	}

}