package org.eclipse.tradista.security.specificrepo.incomingmessage;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.security.common.model.Security;
import org.eclipse.tradista.security.common.service.SecurityBusinessDelegate;
import org.eclipse.tradista.security.repo.incomingmessage.RepoIncomingMessageManager;
import org.eclipse.tradista.security.specificrepo.model.SpecificRepoTrade;
import org.eclipse.tradista.security.specificrepo.service.SpecificRepoTradeBusinessDelegate;

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

	static SecurityBusinessDelegate securityBusinessDelegate = new SecurityBusinessDelegate();

	static SpecificRepoTradeBusinessDelegate specificRepoTradeBusinessDelegate = new SpecificRepoTradeBusinessDelegate();

	@Override
	public default void validateMessage(X externalMessage, StringBuilder errMsg) {
		RepoIncomingMessageManager.super.validateMessage(externalMessage, errMsg);
		checkIsin(externalMessage, errMsg);
		checkExchangeCode(externalMessage, errMsg);
	}

	@Override
	public default SpecificRepoTrade createObject(X externalMessage) {
		SpecificRepoTrade trade = new SpecificRepoTrade();
		trade.setProduct(getSecurity(externalMessage));
		fillObject(externalMessage, trade);
		return trade;
	}

	@Override
	public default long saveObject(SpecificRepoTrade trade) throws TradistaBusinessException {
		return specificRepoTradeBusinessDelegate.saveSpecificRepoTrade(trade, null);
	}

	public default Security getSecurity(X externalMessage) {
		try {
			return securityBusinessDelegate.getSecurityByIsinAndExchangeCode(getIsin(externalMessage),
					getExchangeCode(externalMessage));
		} catch (TradistaBusinessException tbe) {
			throw new TradistaTechnicalException(
					String.format("There was an issue loading the security: %s", tbe.getMessage()));
		}
	}

	void checkIsin(X externalMessage, StringBuilder errMsg);

	void checkExchangeCode(X externalMessage, StringBuilder errMsg);

	String getIsin(X externalMessage);

	String getExchangeCode(X externalMessage);

}