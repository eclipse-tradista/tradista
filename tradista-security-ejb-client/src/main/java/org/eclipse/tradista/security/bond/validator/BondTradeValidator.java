package org.eclipse.tradista.security.bond.validator;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.validator.DefaultTradeValidator;
import org.eclipse.tradista.security.bond.model.Bond;
import org.eclipse.tradista.security.bond.model.BondTrade;

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

public class BondTradeValidator extends DefaultTradeValidator {

	private static final long serialVersionUID = -9058778618860422065L;

	@Override
	public void validateTrade(Trade<? extends Product> trade) throws TradistaBusinessException {
		BondTrade bondTrade = (BondTrade) trade;
		StringBuilder errMsg = validateTradeBasics(trade);
		if (bondTrade.getProduct() == null) {
			errMsg.append(String.format("The bond is mandatory.%n"));
		} else {
			Bond bond = (Bond) bondTrade.getProduct();
			if (trade.getTradeDate() != null) {
				if (trade.getTradeDate().isBefore(bond.getIssueDate())) {
					errMsg.append(String.format("The trade date (%s) cannot be before the bond issue date (%s).%n",
							trade.getTradeDate(), bond.getIssueDate()));
				}
			}
			if (trade.getSettlementDate() != null) {
				if (trade.getSettlementDate().isAfter(bond.getMaturityDate())) {
					errMsg.append(
							String.format("The settlement date (%s) cannot be after the bond maturity date (%s).%n",
									trade.getSettlementDate(), bond.getMaturityDate()));
				}
			}
		}

		if (bondTrade.getQuantity() == null) {
			errMsg.append(String.format("The quantity is mandatory.%n"));
		} else {
			if (bondTrade.getQuantity().doubleValue() <= 0) {
				errMsg.append(
						String.format("The quantity (%s) must be positive.%n", bondTrade.getQuantity().doubleValue()));
			}
		}

		// Other business controls
		if (trade.getAmount() != null && trade.getAmount().doubleValue() <= 0) {
			errMsg.append(String.format("The price (%s) must be positive.%n", trade.getAmount().doubleValue()));
		}

		if (trade.getSettlementDate() == null) {
			errMsg.append(String.format("The settlement date is mandatory.%n"));
		}

		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}

	}

}
