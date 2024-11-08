package org.eclipse.tradista.security.repo.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.security.gcrepo.service.GCRepoTradeBusinessDelegate;
import org.eclipse.tradista.security.repo.model.RepoTrade;
import org.eclipse.tradista.security.specificrepo.service.SpecificRepoTradeBusinessDelegate;

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

public class RepoTradeBusinessDelegate {

	protected GCRepoTradeBusinessDelegate gcRepoTradeBusinessDelegate;

	protected SpecificRepoTradeBusinessDelegate specificRepoTradeBusinessDelegate;

	public RepoTradeBusinessDelegate() {
		gcRepoTradeBusinessDelegate = new GCRepoTradeBusinessDelegate();
		specificRepoTradeBusinessDelegate = new SpecificRepoTradeBusinessDelegate();
	}

	public RepoTrade getRepoTradeById(long tradeId) throws TradistaBusinessException {
		RepoTrade repoTrade = null;
		if (tradeId <= 0) {
			throw new TradistaBusinessException("The trade id must be positive");
		}
		repoTrade = specificRepoTradeBusinessDelegate.getSpecificRepoTradeById(tradeId);
		if (repoTrade == null) {
			repoTrade = gcRepoTradeBusinessDelegate.getGCRepoTradeById(tradeId);
		}
		return repoTrade;
	}
}