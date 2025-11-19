package org.eclipse.tradista.security.specificrepo.importer.model;

import org.eclipse.tradista.security.repo.importer.model.RepoIncomingMessageManager;
import org.eclipse.tradista.security.specificrepo.model.SpecificRepoTrade;

/********************************************************************************
 * Copyright (c) 2025 Olivier Asuncion
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

public interface SpecificRepoIncomingMessageManager<X> extends RepoIncomingMessageManager<X, SpecificRepoTrade> {

	@Override
	public default void checkMessage(X externalMessage, StringBuilder errMsg) {
		RepoIncomingMessageManager.super.checkMessage(externalMessage, errMsg);
		checkIsin(externalMessage, errMsg);
		checkExchangeCode(externalMessage, errMsg);
	}

	void checkIsin(X externalMessage, StringBuilder errMsg);

	void checkExchangeCode(X externalMessage, StringBuilder errMsg);

	String getIsin(X externalMessage);

	String getExchangeCode(X externalMessage);

}