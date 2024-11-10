package org.eclipse.tradista.security.specificrepo.model;

import org.eclipse.tradista.security.common.model.Security;
import org.eclipse.tradista.security.repo.model.RepoTrade;

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
 * License for the specific language governing permissions and limitations
 * under the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
/**
 * Class representing a trade on Special Repo.
 * 
 *
 */
public class SpecificRepoTrade extends RepoTrade {

	private static final long serialVersionUID = 8452035320272812574L;

	public static final String SPECIFIC_REPO = "SpecificRepo";

	private Security security;

	@Override
	public String getWorkflow() {
		return SPECIFIC_REPO;
	}

	@Override
	public String getProductType() {
		return SPECIFIC_REPO;
	}

	public Security getSecurity() {
		return security;
	}

	public void setSecurity(Security security) {
		this.security = security;
	}

}