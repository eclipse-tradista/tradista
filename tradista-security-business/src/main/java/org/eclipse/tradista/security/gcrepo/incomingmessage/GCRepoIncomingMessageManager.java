package org.eclipse.tradista.security.gcrepo.incomingmessage;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.security.gcrepo.model.GCBasket;
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;
import org.eclipse.tradista.security.gcrepo.service.GCRepoTradeBusinessDelegate;
import org.eclipse.tradista.security.repo.incomingmessage.RepoIncomingMessageManager;

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

public interface GCRepoIncomingMessageManager<X> extends RepoIncomingMessageManager<X, GCRepoTrade> {

	static GCRepoTradeBusinessDelegate gcRepoTradeBusinessDelegate = new GCRepoTradeBusinessDelegate();

	@Override
	public default void validateMessage(X externalMessage, StringBuilder errMsg) {
		RepoIncomingMessageManager.super.validateMessage(externalMessage, errMsg);
		checkBasket(externalMessage, errMsg);
	}

	@Override
	public default GCRepoTrade createObject(X externalMessage) {
		GCRepoTrade trade = new GCRepoTrade();
		trade.setGcBasket(getBasket(externalMessage));
		fillObject(externalMessage, trade);
		return trade;
	}

	@Override
	public default long saveObject(GCRepoTrade trade) throws TradistaBusinessException {
		return gcRepoTradeBusinessDelegate.saveGCRepoTrade(trade, null);
	}

	void checkBasket(X externalMessage, StringBuilder errMsg);

	GCBasket getBasket(X externalMessage);

}