package org.eclipse.tradista.mm.loandeposit.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.messaging.service.LocalCoreMessagingService;
import org.eclipse.tradista.core.trade.service.CheckTradeAccess;
import org.eclipse.tradista.core.trade.service.ProductScope;
import org.eclipse.tradista.core.trade.service.ProductScopeMode;
import org.eclipse.tradista.core.trade.service.TradeService;
import org.eclipse.tradista.mm.loandeposit.messaging.LoanDepositTradeEvent;
import org.eclipse.tradista.mm.loandeposit.model.LoanDepositTrade;
import org.eclipse.tradista.mm.loandeposit.persistence.LoanDepositTradeSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class LoanDepositTradeServiceBean implements LoanDepositTradeService {

	@EJB
	private LocalCoreMessagingService messagingConfigurationService;

	@EJB
	private TradeService tradeService;

	@ProductScope(value = LoanDepositTrade.LOAN_DEPOSIT, mode = ProductScopeMode.ON_CREATION)
	@Override
	public long saveLoanDepositTrade(@CheckTradeAccess LoanDepositTrade trade) throws TradistaBusinessException {
		tradeService.checkTradeBasics(trade, true);

		LoanDepositTradeEvent event = new LoanDepositTradeEvent();
		if (trade.getId() != 0) {
			LoanDepositTrade oldTrade = LoanDepositTradeSQL.getTradeById(trade.getId());
			event.setOldTrade(oldTrade);
		}

		event.setTrade(trade);
		long result = LoanDepositTradeSQL.saveLoanDepositTrade(trade);

		messagingConfigurationService.publishEvent(event);
		return result;
	}

	@Override
	public LoanDepositTrade getLoanDepositTradeById(long id) {
		return LoanDepositTradeSQL.getTradeById(id);
	}

}