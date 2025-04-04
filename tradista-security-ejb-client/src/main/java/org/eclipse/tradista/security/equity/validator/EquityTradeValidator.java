package org.eclipse.tradista.security.equity.validator;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.validator.DefaultTradeValidator;
import org.eclipse.tradista.security.equity.model.EquityTrade;

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

public class EquityTradeValidator extends DefaultTradeValidator {

	private static final long serialVersionUID = 2019066568222842419L;

	@Override
	public void validateTrade(Trade<? extends Product> trade) throws TradistaBusinessException {
		EquityTrade equityTrade = (EquityTrade) trade;
		StringBuilder errMsg = validateTradeBasics(trade);
		if (equityTrade.getProduct() == null) {
			errMsg.append(String.format("The equity is mandatory.%n"));
		}

		if (equityTrade.getQuantity() == null) {
			errMsg.append(String.format("The quantity is mandatory.%n"));
		} else {
			if (equityTrade.getQuantity().doubleValue() <= 0) {
				errMsg.append(String.format("The quantity (%s) must be positive.%n",
						equityTrade.getQuantity().doubleValue()));
			}
		}

		// Other business controls
		if (trade.getAmount() != null && trade.getAmount().doubleValue() <= 0) {
			errMsg.append(String.format("The price (%s) must be positive.%n", trade.getAmount().doubleValue()));
		}

		if (trade.getSettlementDate() == null) {
			errMsg.append(String.format("The settlement date is mandatory.%n"));
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

	}

}
