package org.eclipse.tradista.core.workflow.service;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.workflow.model.Status;
import org.eclipse.tradista.core.workflow.model.Workflow;

/********************************************************************************
 * Copyright (c) 2023 Olivier Asuncion
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

public class WorkflowBusinessDelegate {

	private WorkflowService workflowService;

	public WorkflowBusinessDelegate() {
		workflowService = TradistaServiceLocator.getInstance().getWorkflowService();
	}

	public Workflow getWorkflowByName(String name) throws TradistaBusinessException {
		if (StringUtils.isEmpty(name)) {
			throw new TradistaBusinessException("The name is mandatory");
		}
		return SecurityUtil.runEx(() -> workflowService.getWorkflowByName(name));
	}

	public Set<String> getAvailableActionsFromStatus(String workflowName, Status status)
			throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isEmpty(workflowName)) {
			errMsg.append("The workflow name is mandatory.");
		}
		if (status == null) {
			errMsg.append("The status is mandatory.");
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil.runEx(() -> workflowService.getAvailableActionsFromStatus(workflowName, status));
	}

	public Status getInitialStatus(String workflowName) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isEmpty(workflowName)) {
			errMsg.append("The workflow name is mandatory.");
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil.runEx(() -> workflowService.getInitialStatus(workflowName));
	}

}