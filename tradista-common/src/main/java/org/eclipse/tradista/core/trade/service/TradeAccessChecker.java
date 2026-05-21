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

package org.eclipse.tradista.core.trade.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.AccessChecker;
import org.eclipse.tradista.core.trade.model.Trade;

/**
 * {@link AccessChecker} implementation for
 * {@link org.eclipse.tradista.core.trade.model.Trade} parameters. Verifies that
 * the trade (if id is non-zero) exists in the system.
 */
public class TradeAccessChecker implements AccessChecker {

	private final TradeBusinessDelegate tradeBusinessDelegate;

	public TradeAccessChecker() {
		tradeBusinessDelegate = new TradeBusinessDelegate();
	}

	@Override
	public void check(Object parameter, StringBuilder errMsg) throws TradistaBusinessException {
		if (parameter instanceof Trade<?> trade) {
			if (trade.getId() != 0) {
				if (tradeBusinessDelegate.getTradeById(trade.getId(), true) == null) {
					errMsg.append(String.format("The trade %d was not found.%n", trade.getId()));
				}
			}
		} else if (parameter instanceof Long tradeId) {
			if (tradeId != 0) {
				Trade<?> trade = tradeBusinessDelegate.getTradeById(tradeId, true);
				if (trade == null) {
					errMsg.append(String.format("The trade %d was not found.%n", tradeId));
				}
			}
		}
	}

}