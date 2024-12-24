package org.eclipse.tradista.ir.future.validator;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.validator.DefaultTradeValidator;
import org.eclipse.tradista.ir.future.model.FutureTrade;

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

public class FutureTradeValidator extends DefaultTradeValidator {

	private static final long serialVersionUID = 6152667501086172308L;

	private FutureValidator futureValidator;

	public FutureTradeValidator() {
		super();
		futureValidator = new FutureValidator();
	}

	@Override
	public void validateTrade(Trade<? extends Product> trade) throws TradistaBusinessException {
		FutureTrade futureTrade = (FutureTrade) trade;
		StringBuilder errMsg = new StringBuilder();
		if (futureTrade.getProduct() == null) {
			errMsg.append(String.format("The future is mandatory.%n"));
		} else {
			errMsg.append(validateTradeBasics(trade));
		}

		// Other business controls
		if (trade.getAmount() != null && trade.getAmount().doubleValue() <= 0) {
			errMsg.append(String.format("The price (%s) must be positive.%n", trade.getAmount().doubleValue()));
		}
		if (futureTrade.getQuantity() == null) {
			errMsg.append(String.format("The quantity is mandatory.%n"));
		}
		if (futureTrade.getQuantity() != null && futureTrade.getQuantity().doubleValue() <= 0) {
			errMsg.append(
					String.format("The quantity (%s) must be positive.%n", futureTrade.getQuantity().doubleValue()));
		}
		if (futureTrade.getMaturityDate() == null) {
			errMsg.append(String.format("The maturity date is mandatory.%n"));
		}

		if (trade.getSettlementDate() == null) {
			errMsg.append(String.format("The settlement date is mandatory.%n"));
		}

		if (futureTrade.getMaturityDate() != null && trade.getSettlementDate() != null) {
			if (trade.getSettlementDate().isAfter(futureTrade.getMaturityDate())) {
				errMsg.append(String.format("The settlement date (%s) cannot be after the maturity date (%s).%n",
						trade.getSettlementDate(), futureTrade.getMaturityDate()));
			}
		}

		futureValidator.validateProduct(trade.getProduct());

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

	}

}
