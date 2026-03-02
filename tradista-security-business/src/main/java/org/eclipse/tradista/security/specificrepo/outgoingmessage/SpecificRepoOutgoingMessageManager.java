package org.eclipse.tradista.security.specificrepo.outgoingmessage;

import org.eclipse.tradista.security.repo.outgoingmessage.RepoOutgoingMessageManager;
import org.eclipse.tradista.security.specificrepo.model.SpecificRepoTrade;

/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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

public interface SpecificRepoOutgoingMessageManager<X> extends RepoOutgoingMessageManager<X, SpecificRepoTrade> {

	@Override
	public default X createContent(X externalMessage, SpecificRepoTrade trade) {
		externalMessage = createSecurity(externalMessage, trade);
		externalMessage = RepoOutgoingMessageManager.super.createContent(externalMessage, trade);
		return externalMessage;
	}

	X createSecurity(X externalMessage, SpecificRepoTrade trade);

}