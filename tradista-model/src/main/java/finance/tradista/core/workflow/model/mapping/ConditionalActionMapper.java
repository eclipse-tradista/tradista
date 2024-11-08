package org.eclipse.tradista.core.workflow.model.mapping;

import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.workflow.model.ConditionalAction;
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

public final class ConditionalActionMapper {

	private ConditionalActionMapper() {
	}

	public static ConditionalAction map(org.eclipse.tradista.flow.model.ConditionalAction<?> action) {
		ConditionalAction actionResult = null;
		if (action != null) {
			actionResult = new ConditionalAction();
			actionResult.setId(action.getId());
			actionResult.setName(action.getName());
			actionResult.setChoicePseudoStatus(StatusMapper.map(action.getChoicePseudoStatus()));
			if (action.getConditionalActions() != null) {
				actionResult.setConditionalActions(action.getConditionalActions().stream().map(SimpleActionMapper::map)
						.collect(Collectors.toSet()));
			}
			if (action.getConditionalRouting() != null) {
				actionResult.setConditionalRouting(action.getConditionalRouting().entrySet().stream()
						.collect(Collectors.toMap(Map.Entry::getKey, e -> StatusMapper.map(e.getValue()))));
			}
			actionResult.setDepartureStatus(StatusMapper.map(action.getDepartureStatus()));
			if (action.getGuards() != null) {
				actionResult.setGuards(action.getGuards().stream().map(GuardMapper::map).collect(Collectors.toSet()));
			}
			actionResult.setCondition(ConditionMapper.map(action.getCondition()));
		}
		return actionResult;
	}

	public static <X extends WorkflowObject> org.eclipse.tradista.flow.model.ConditionalAction<X> map(
			ConditionalAction action, Workflow<X> workflow) {
		org.eclipse.tradista.flow.model.ConditionalAction<X> actionResult = null;
		if (action != null) {
			actionResult = new org.eclipse.tradista.flow.model.ConditionalAction<>();
			actionResult.setId(action.getId());
			actionResult.setName(action.getName());
			actionResult.setChoicePseudoStatus(StatusMapper.map(action.getChoicePseudoStatus(), workflow));
			if (action.getConditionalActions() != null) {
				actionResult.setConditionalActions(action.getConditionalActions().stream()
						.map(a -> SimpleActionMapper.map(a, workflow)).collect(Collectors.toSet()));
			}
			if (action.getConditionalRouting() != null) {
				actionResult.setConditionalRouting(action.getConditionalRouting().entrySet().stream()
						.collect(Collectors.toMap(Map.Entry::getKey, e -> StatusMapper.map(e.getValue(), workflow))));
			}
			if (action.getGuards() != null) {
				actionResult.setGuards(action.getGuards().stream().map(GuardMapper::map).collect(Collectors.toSet()));
			}
			actionResult.setDepartureStatus(StatusMapper.map(action.getDepartureStatus(), workflow));
			actionResult.setWorkflow(workflow);
			actionResult.setCondition(ConditionMapper.map(action.getCondition()));
		}
		return actionResult;
	}

}