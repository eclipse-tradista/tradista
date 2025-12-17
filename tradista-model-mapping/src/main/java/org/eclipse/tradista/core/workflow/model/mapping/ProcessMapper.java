package org.eclipse.tradista.core.workflow.model.mapping;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.workflow.model.Process;

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

public final class ProcessMapper {

	private ProcessMapper() {
	}

	public static <X extends finance.tradista.flow.model.WorkflowObject> Process map(
			finance.tradista.flow.model.Process<X> process) {
		Process processResult = null;
		if (process != null) {
			processResult = new Process();
			processResult.setId(process.getId());
			processResult.setName(process.getName());
			processResult.setLongName(process.getClass().getName());
		}
		return processResult;
	}

	@SuppressWarnings("unchecked")
	public static <X extends finance.tradista.flow.model.WorkflowObject> finance.tradista.flow.model.Process<X> map(
			Process process) {
		finance.tradista.flow.model.Process<X> processResult = null;
		if (process != null) {
			processResult = TradistaModelUtil.getInstance(finance.tradista.flow.model.Process.class,
					process.getLongName());
			processResult.setId(process.getId());
		}
		return processResult;
	}

}