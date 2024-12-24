package org.eclipse.tradista.ai.agent.service;

import org.eclipse.tradista.ai.agent.model.AssetManagerAgent;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
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

public class AssetManagerAgentBusinessDelegate {

	private AssetManagerAgentService assetManagerAgentService;

	public AssetManagerAgentBusinessDelegate() {
		assetManagerAgentService = TradistaServiceLocator.getInstance().getAssetManagerAgentService();
	}

	public long saveAssetManagerAgent(AssetManagerAgent agent) throws TradistaBusinessException {
		if (agent == null) {
			throw new TradistaBusinessException("The agent cannot be null.");
		}
		StringBuilder errMsg = new StringBuilder();
		if (agent.getName() == null) {
			errMsg.append(String.format("The name is mandatory.%n"));
		}
		if (agent.getMandate() == null) {
			errMsg.append(String.format("The mandate is mandatory.%n"));
		}
		if (agent.getPricingParameter() == null) {
			errMsg.append(String.format("The pricing parameter set is mandatory.%n"));
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

		return SecurityUtil.runEx(() -> assetManagerAgentService.saveAssetManagerAgent(agent));
	}

	public void executeMandate(AssetManagerAgent agent) throws TradistaBusinessException {
		if (agent == null) {
			throw new TradistaBusinessException("The agent cannot be null.");
		}
		SecurityUtil.runEx(() -> assetManagerAgentService.executeMandate(agent));
	}

}