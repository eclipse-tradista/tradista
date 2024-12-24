package org.eclipse.tradista.security.gcrepo.service;

import java.math.BigDecimal;
import java.util.Map;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.security.common.model.Security;
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;
import org.eclipse.tradista.security.gcrepo.validator.GCRepoTradeValidator;

/********************************************************************************
 * Copyright (c) 2023 Olivier Asuncion
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

public class GCRepoTradeBusinessDelegate {

	private GCRepoTradeService gcRepoTradeService;

	private GCRepoTradeValidator validator;

	private static final String TRADE_ID_MUST_BE_POSITIVE = "The trade id must be positive.";

	public GCRepoTradeBusinessDelegate() {
		gcRepoTradeService = TradistaServiceLocator.getInstance().getGCRepoTradeService();
		validator = new GCRepoTradeValidator();
	}

	public long saveGCRepoTrade(GCRepoTrade trade, String action) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> gcRepoTradeService.saveGCRepoTrade(trade, action));
	}

	public GCRepoTrade getGCRepoTradeById(long tradeId) throws TradistaBusinessException {
		if (tradeId <= 0) {
			throw new TradistaBusinessException(TRADE_ID_MUST_BE_POSITIVE);
		}
		return SecurityUtil.run(() -> gcRepoTradeService.getGCRepoTradeById(tradeId));
	}

	public Map<Security, Map<Book, BigDecimal>> getAllocatedCollateral(GCRepoTrade trade)
			throws TradistaBusinessException {
		if (trade == null) {
			throw new TradistaBusinessException("The trade is mandatory.");
		}
		return SecurityUtil.runEx(() -> gcRepoTradeService.getAllocatedCollateral(trade));
	}

}