package org.eclipse.tradista.core.workflow.model;

import java.util.Map;
import java.util.Set;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;

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

public class ConditionalAction extends Action {

	private static final long serialVersionUID = -7207658264977414731L;

	private Condition condition;

	private Map<Integer, Status> conditionalRouting;

	private Status choicePseudoStatus;

	private Set<SimpleAction> conditionalActions;

	public Condition getCondition() {
		return TradistaModelUtil.clone(condition);
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	@SuppressWarnings("unchecked")
	public Map<Integer, Status> getConditionalRouting() {
		return (Map<Integer, Status>) TradistaModelUtil.deepCopy(conditionalRouting);
	}

	public void setConditionalRouting(Map<Integer, Status> conditionalRouting) {
		this.conditionalRouting = conditionalRouting;
	}

	public Status getChoicePseudoStatus() {
		return TradistaModelUtil.clone(choicePseudoStatus);
	}

	public void setChoicePseudoStatus(Status choicePseudoStatus) {
		this.choicePseudoStatus = choicePseudoStatus;
	}

	@SuppressWarnings("unchecked")
	public Set<SimpleAction> getConditionalActions() {
		return (Set<SimpleAction>) TradistaModelUtil.deepCopy(conditionalActions);
	}

	public void setConditionalActions(Set<SimpleAction> conditionalActions) {
		this.conditionalActions = conditionalActions;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ConditionalAction clone() {
		ConditionalAction action = (ConditionalAction) super.clone();
		action.condition = TradistaModelUtil.clone(condition);
		action.conditionalRouting = (Map<Integer, Status>) TradistaModelUtil.deepCopy(conditionalRouting);
		action.conditionalActions = (Set<SimpleAction>) TradistaModelUtil.deepCopy(conditionalActions);
		return action;
	}

}