package org.eclipse.tradista.core.trade.incomingmessage;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.trade.model.Trade;

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

public interface TradeExporter<X> {

	default void exportTrade(X externalMessage, Trade<?> trade) throws TradistaBusinessException {
		exportTradeDate(trade, externalMessage);
		exportSettlementDate(trade, externalMessage);
		exportNotional(trade, externalMessage);
		exportCurrency(trade, externalMessage);
		exportCounterparty(trade, externalMessage);
		exportBook(trade, externalMessage);
		exportBuySell(trade, externalMessage);
	}

	X exportTradeDate(Trade<?> trade, X externalMessage);

	X exportSettlementDate(Trade<?> trade, X externalMessage);

	X exportNotional(Trade<?> trade, X externalMessage);

	X exportCurrency(Trade<?> trade, X externalMessage);

	X exportCounterparty(Trade<?> trade, X externalMessage) throws TradistaBusinessException;

	X exportBook(Trade<?> trade, X externalMessage) throws TradistaBusinessException;

	X exportBuySell(Trade<?> trade, X externalMessage) throws TradistaBusinessException;

}