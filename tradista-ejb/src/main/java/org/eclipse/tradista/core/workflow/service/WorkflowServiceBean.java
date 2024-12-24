package org.eclipse.tradista.core.workflow.service;

import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.workflow.model.Status;
import org.eclipse.tradista.core.workflow.model.Workflow;
import org.eclipse.tradista.core.workflow.model.mapping.StatusMapper;
import org.eclipse.tradista.core.workflow.model.mapping.WorkflowMapper;
import org.jboss.ejb3.annotation.SecurityDomain;

import finance.tradista.flow.exception.TradistaFlowBusinessException;
import finance.tradista.flow.service.WorkflowManager;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;

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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class WorkflowServiceBean implements WorkflowService {

	@Override
	public Workflow getWorkflowByName(String name) throws TradistaBusinessException {
		finance.tradista.flow.model.Workflow workflow;
		Workflow workflowResult = null;
		try {
			workflow = WorkflowManager.getWorkflowByName(name);
		} catch (TradistaFlowBusinessException tfbe) {
			throw new TradistaBusinessException(tfbe);
		}
		if (workflow != null) {
			workflowResult = WorkflowMapper.map(workflow);
		}
		return workflowResult;
	}

	@Override
	public Set<String> getAvailableActionsFromStatus(String workflowName, Status status)
			throws TradistaBusinessException {
		finance.tradista.flow.model.Workflow workflow;
		Set<String> actionNames = null;
		try {
			workflow = WorkflowManager.getWorkflowByName(workflowName);
		} catch (TradistaFlowBusinessException tfbe) {
			throw new TradistaBusinessException(tfbe);
		}
		if (workflow != null) {
			actionNames = workflow.getAvailableActionsFromStatus(StatusMapper.map(status, workflow));
		} else {
			throw new TradistaBusinessException(String.format("The workflow %s cannot be found", workflowName));
		}
		return actionNames;
	}

	@Override
	public Status getInitialStatus(String workflowName) throws TradistaBusinessException {
		finance.tradista.flow.model.Workflow workflow;
		try {
			workflow = WorkflowManager.getWorkflowByName(workflowName);
		} catch (TradistaFlowBusinessException tfbe) {
			throw new TradistaBusinessException(tfbe);
		}
		if (workflow != null) {
			return StatusMapper.map(workflow.getInitialStatus());
		} else {
			throw new TradistaBusinessException(String.format("The workflow %s cannot be found", workflowName));
		}
	}

}