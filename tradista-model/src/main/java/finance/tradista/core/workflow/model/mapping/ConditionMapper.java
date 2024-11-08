package org.eclipse.tradista.core.workflow.model.mapping;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.workflow.model.Condition;

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

public final class ConditionMapper {

	private ConditionMapper() {
	}

	public static <X extends org.eclipse.tradista.flow.model.WorkflowObject> Condition map(
			org.eclipse.tradista.flow.model.Condition<X> condition) {
		Condition conditionResult = null;
		if (condition != null) {
			conditionResult = new Condition();
			conditionResult.setId(condition.getId());
			conditionResult.setName(condition.getName());
			conditionResult.setLongName(condition.getClass().getName());
		}
		return conditionResult;
	}

	@SuppressWarnings("unchecked")
	public static <X extends org.eclipse.tradista.flow.model.WorkflowObject> org.eclipse.tradista.flow.model.Condition<X> map(
			Condition condition) {
		org.eclipse.tradista.flow.model.Condition<X> conditionResult = null;
		if (condition != null) {
			conditionResult = TradistaModelUtil.getInstance(org.eclipse.tradista.flow.model.Condition.class,
					condition.getLongName());
			conditionResult.setId(condition.getId());
		}
		return conditionResult;
	}

}