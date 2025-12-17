package org.eclipse.tradista.core.trade.validator;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.Trade;

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

public class DefaultTradeValidator implements TradeValidator {

	private static final long serialVersionUID = -5806272321571799302L;

	@Override
	public void validateTrade(Trade<? extends Product> trade) throws TradistaBusinessException {
		StringBuilder errMsg = validateTradeBasics(trade);
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

	protected StringBuilder validateTradeBasics(Trade<? extends Product> trade) throws TradistaBusinessException {
		// Existence controls
		if (trade == null) {
			throw new TradistaBusinessException("The trade cannot be null.");
		}
		StringBuilder errMsg = new StringBuilder();
		if (trade.getTradeDate() == null) {
			errMsg.append(String.format("The trade date is mandatory.%n"));
		}
		if (trade.getAmount() == null) {
			errMsg.append(String.format("The amount is mandatory.%n"));
		}
		if (trade.getCurrency() == null) {
			errMsg.append(String.format("The currency is mandatory.%n"));
		}

		if (trade.getTradeDate() != null && trade.getSettlementDate() != null) {
			if (trade.getSettlementDate().isBefore(trade.getTradeDate())) {
				errMsg.append(String.format("The settlement date (%s) cannot be before trade date (%s).%n",
						trade.getSettlementDate(), trade.getTradeDate()));
			}
		}

		if (trade.getCounterparty() == null) {
			errMsg.append(String.format("The counterparty is mandatory.%n"));
		}
		if (trade.getBook() == null) {
			errMsg.append(String.format("The book is mandatory.%n"));
		}
		return errMsg;
	}

}
