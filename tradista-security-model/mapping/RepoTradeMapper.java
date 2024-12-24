package org.eclipse.tradista.security.repo.workflow.mapping;

import org.eclipse.tradista.flow.model.Workflow;
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;

/********************************************************************************
 * Copyright (c) 2024 Olivier Asuncion
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

public class RepoTradeMapper {

	private RepoTradeMapper() {
	}

	public static org.eclipse.tradista.security.repo.workflow.mapping.RepoTrade map(GCRepoTrade gcRepoTrade,
			Workflow wkf) {

		org.eclipse.tradista.security.repo.workflow.mapping.RepoTrade gcRepoTradeResult = new org.eclipse.tradista.security.repo.workflow.mapping.RepoTrade(
				wkf);
		gcRepoTradeResult.setRepoTrade(gcRepoTrade);

		return gcRepoTradeResult;

	}

}