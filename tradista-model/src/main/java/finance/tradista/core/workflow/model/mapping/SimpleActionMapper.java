package org.eclipse.tradista.core.workflow.model.mapping;

import java.util.stream.Collectors;

import org.eclipse.tradista.core.workflow.model.SimpleAction;
import org.eclipse.tradista.flow.model.Workflow;
import org.eclipse.tradista.flow.model.WorkflowObject;

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

public final class SimpleActionMapper {

	private SimpleActionMapper() {
	}

	public static SimpleAction map(org.eclipse.tradista.flow.model.SimpleAction<?> action) {
		SimpleAction actionResult = null;
		if (action != null) {
			actionResult = new SimpleAction();
			actionResult.setId(action.getId());
			actionResult.setName(action.getName());
			actionResult.setArrivalStatus(StatusMapper.map(action.getArrivalStatus()));
			actionResult.setDepartureStatus(StatusMapper.map(action.getDepartureStatus()));
			if (action.getGuards() != null) {
				actionResult.setGuards(action.getGuards().stream().map(GuardMapper::map).collect(Collectors.toSet()));
			}
			if (action.getProcesses() != null) {
				actionResult.setProcesses(
						action.getProcesses().stream().map(ProcessMapper::map).collect(Collectors.toSet()));
			}
		}
		return actionResult;
	}

	public static <X extends WorkflowObject> org.eclipse.tradista.flow.model.SimpleAction<X> map(SimpleAction action,
			Workflow<X> workflow) {
		org.eclipse.tradista.flow.model.SimpleAction<X> actionResult = null;
		if (action != null) {
			actionResult = new org.eclipse.tradista.flow.model.SimpleAction<>();
			actionResult.setId(action.getId());
			actionResult.setName(action.getName());
			actionResult.setArrivalStatus(StatusMapper.map(action.getArrivalStatus(), workflow));
			actionResult.setDepartureStatus(StatusMapper.map(action.getDepartureStatus(), workflow));
			if (action.getGuards() != null) {
				actionResult.setGuards(
						action.getGuards().stream().map(GuardMapper::map).collect(Collectors.toSet()));
			}
			if (action.getProcesses() != null) {
				actionResult.setProcesses(
						action.getProcesses().stream().map(ProcessMapper::map).collect(Collectors.toSet()));
			}
			actionResult.setWorkflow(workflow);
		}
		return actionResult;
	}

}