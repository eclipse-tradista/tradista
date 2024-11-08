package org.eclipse.tradista.security.specificrepo.workflow.mapping;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.flow.model.Workflow;
import org.eclipse.tradista.security.repo.workflow.mapping.RepoTrade;

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

public class SpecificRepoTrade extends RepoTrade {

	public SpecificRepoTrade(Workflow wkf) {
		super(wkf);
	}

	public void setRepoTrade(org.eclipse.tradista.security.specificrepo.model.SpecificRepoTrade repoTrade) {
		this.repoTrade = repoTrade;
	}
	
	public org.eclipse.tradista.security.specificrepo.model.SpecificRepoTrade getOriginalRepoTrade() {
		return (org.eclipse.tradista.security.specificrepo.model.SpecificRepoTrade) TradistaModelUtil.clone(repoTrade);
	}

}