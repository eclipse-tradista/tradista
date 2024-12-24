package org.eclipse.tradista.security.specificrepo.service;

import java.math.BigDecimal;
import java.util.Map;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.security.common.model.Security;
import org.eclipse.tradista.security.specificrepo.model.SpecificRepoTrade;
import org.eclipse.tradista.security.specificrepo.validator.SpecificRepoTradeValidator;

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

public class SpecificRepoTradeBusinessDelegate {

	private SpecificRepoTradeService specificRepoTradeService;

	private SpecificRepoTradeValidator validator;

	private static final String TRADE_ID_MUST_BE_POSITIVE = "The trade id must be positive.";

	public SpecificRepoTradeBusinessDelegate() {
		specificRepoTradeService = TradistaServiceLocator.getInstance().getSpecificRepoTradeService();
		validator = new SpecificRepoTradeValidator();
	}

	public long saveSpecificRepoTrade(SpecificRepoTrade trade, String action) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> specificRepoTradeService.saveSpecificRepoTrade(trade, action));
	}

	public SpecificRepoTrade getSpecificRepoTradeById(long tradeId) throws TradistaBusinessException {
		if (tradeId <= 0) {
			throw new TradistaBusinessException(TRADE_ID_MUST_BE_POSITIVE);
		}
		return SecurityUtil.run(() -> specificRepoTradeService.getSpecificRepoTradeById(tradeId));
	}

	public Map<Security, Map<Book, BigDecimal>> getAllocatedCollateral(SpecificRepoTrade trade)
			throws TradistaBusinessException {
		if (trade == null) {
			throw new TradistaBusinessException("The trade is mandatory.");
		}
		return SecurityUtil.runEx(() -> specificRepoTradeService.getAllocatedCollateral(trade));
	}

}