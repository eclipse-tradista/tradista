package org.eclipse.tradista.core.workflow.model.mapping;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.workflow.model.Action;
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

public final class WorkflowMapper {

	private WorkflowMapper() {
	}

	public static Workflow map(org.eclipse.tradista.flow.model.Workflow<?> wkf) {
		Workflow workflow = null;
		if (wkf != null) {
			workflow = new Workflow();
			workflow.setId(wkf.getId());
			workflow.setName(wkf.getName());
			workflow.setDescription(wkf.getDescription());
			Set<Status> statusSet = new HashSet<>();
			Set<Action> actionsSet = new HashSet<>();
			for (org.eclipse.tradista.flow.model.Status<?> status : wkf.getStatus()) {
				Status currentStatus = StatusMapper.map(status);
				currentStatus.setWorkflowName(workflow.getName());
				statusSet.add(currentStatus);
			}
			for (org.eclipse.tradista.flow.model.Action<?> action : wkf.getActions()) {
				Action currentAction = ActionMapper.map(action);
				if (currentAction != null) {
					actionsSet.add(currentAction);
					currentAction.setWorkflowName(workflow.getName());
				}
			}
			workflow.setActions(actionsSet);
			workflow.setStatus(statusSet);
		}
		return workflow;
	}

}