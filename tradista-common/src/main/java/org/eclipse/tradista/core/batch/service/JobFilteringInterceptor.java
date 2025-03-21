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

	private static final String THE_PROCESSING_ORG_WAS_NOT_FOUND = "The processing org %s was not found.";
	private static final String THIS_JOB_INSTANCE_IS_A_GLOBAL_ONE_AND_YOU_ARE_NOT_ALLOWED_TO_UPDATE_IT = "This job instance %s is a global one and you are not allowed to update it.%n";
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
		Method method = ic.getMethod();
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameters.length > 0) {
			if (parameterTypes[0].equals(TradistaJobInstance.class)) {
				TradistaJobInstance jobInstance = (TradistaJobInstance) parameters[0];
				StringBuilder errMsg = new StringBuilder();
				User user = getCurrentUser();
				if (jobInstance.getProcessingOrg() == null) {
					errMsg.append(String.format(THIS_JOB_INSTANCE_IS_A_GLOBAL_ONE_AND_YOU_ARE_NOT_ALLOWED_TO_UPDATE_IT,
							jobInstance.getName()));
				}
				if (jobInstance.getProcessingOrg() != null
						&& !jobInstance.getProcessingOrg().equals(user.getProcessingOrg())) {
					errMsg.append(String.format(THE_PROCESSING_ORG_WAS_NOT_FOUND, jobInstance.getProcessingOrg()));
				}
				if (!errMsg.isEmpty()) {
					throw new TradistaBusinessException(errMsg.toString());
				}
			}
			if (parameters.length == 1 && parameterTypes[0].equals(String.class)) {
				StringBuilder errMsg = new StringBuilder();
				if (method.getName().equals("stopJobExecution")) {
					String jobExecutionId = (String) parameters[0];
					TradistaJobExecution jobExecution = batchBusinessDelegate.getJobExecutionById(jobExecutionId);
					if (jobExecution == null) {
						errMsg.append(
								String.format(THIS_JOB_INSTANCE_IS_A_GLOBAL_ONE_AND_YOU_ARE_NOT_ALLOWED_TO_UPDATE_IT));
					}
				} else {
					if (method.getName().equals("getAllJobInstances")) {
						String po = (String) parameters[0];
						String userPo = getCurrentUser().getProcessingOrg() != null
								? getCurrentUser().getProcessingOrg().getShortName()
								: null;
						if (po == null) {
							errMsg.append(String
									.format(THIS_JOB_INSTANCE_IS_A_GLOBAL_ONE_AND_YOU_ARE_NOT_ALLOWED_TO_UPDATE_IT));
						}
						if (po != null && !po.equals(userPo)) {
							errMsg.append(String.format(THE_PROCESSING_ORG_WAS_NOT_FOUND, po));
						}
						if (!errMsg.isEmpty()) {
							throw new TradistaBusinessException(errMsg.toString());
						}
					}
				}
			}
			if (parameters.length > 1 && parameterTypes[1].equals(String.class)) {
				String po = (String) parameters[1];
				StringBuilder errMsg = new StringBuilder();
				String userPo = getCurrentUser().getProcessingOrg() != null
						? getCurrentUser().getProcessingOrg().getShortName()
						: null;
				if (po == null) {
					errMsg.append(
							String.format(THIS_JOB_INSTANCE_IS_A_GLOBAL_ONE_AND_YOU_ARE_NOT_ALLOWED_TO_UPDATE_IT));
				}
				if (po != null && !po.equals(userPo)) {
					errMsg.append(String.format(THE_PROCESSING_ORG_WAS_NOT_FOUND, po));
				}
				if (!errMsg.isEmpty()) {
					throw new TradistaBusinessException(errMsg.toString());
				}
			}
		}
	}

	@Override
	protected Object postFilter(Object value) {
		if (value != null) {
			User user = getCurrentUser();
			if (value instanceof TradistaJobExecution jobExecution) {
				if (!jobExecution.getJobInstance().getProcessingOrg().equals(user.getProcessingOrg())) {
					value = null;
				}
			}
		}
		return value;
	}

}