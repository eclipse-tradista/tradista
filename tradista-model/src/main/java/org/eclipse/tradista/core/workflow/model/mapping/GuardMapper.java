package org.eclipse.tradista.core.workflow.model.mapping;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.workflow.model.Guard;

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

public final class GuardMapper {

	private GuardMapper() {
	}

	public static <X extends finance.tradista.flow.model.WorkflowObject> Guard map(
			finance.tradista.flow.model.Guard<X> guard) {
		Guard guardResult = null;
		if (guard != null) {
			guardResult = new Guard();
			guardResult.setId(guard.getId());
			guardResult.setName(guard.getName());
			guardResult.setLongName(guard.getClass().getName());
		}
		return guardResult;
	}

	@SuppressWarnings("unchecked")
	public static <X extends finance.tradista.flow.model.WorkflowObject> finance.tradista.flow.model.Guard<X> map(
			Guard guard) {
		finance.tradista.flow.model.Guard<X> guardResult = null;
		if (guard != null) {
			guardResult = TradistaModelUtil.getInstance(finance.tradista.flow.model.Guard.class, guard.getLongName());
			guardResult.setId(guard.getId());
		}
		return guardResult;
	}

}
