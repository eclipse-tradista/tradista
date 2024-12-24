package org.eclipse.tradista.fx.fxndf.validator;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.validator.DefaultTradeValidator;
import org.eclipse.tradista.fx.fxndf.model.FXNDFTrade;
import org.eclipse.tradista.fx.fxndf.service.FXNDFTradeBusinessDelegate;

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

public class FXNDFTradeValidator extends DefaultTradeValidator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2885486745043039277L;

	public FXNDFTradeValidator() {
	}

	@Override
	public void validateTrade(Trade<? extends Product> trade) throws TradistaBusinessException {
		FXNDFTradeBusinessDelegate fxNdfTradeBusinessDelegate = new FXNDFTradeBusinessDelegate();
		FXNDFTrade fxNdfTrade = (FXNDFTrade) trade;
		StringBuilder errMsg = validateTradeBasics(trade);
		if (fxNdfTrade.getNonDeliverableCurrency() == null) {
			errMsg.append(String.format("The non deliverable currency is mandatory.%n"));
		} else {
			if (fxNdfTrade.getNonDeliverableCurrency().equals(fxNdfTrade.getCurrency())) {
				errMsg.append(
						String.format("The non deliverable currency must be different of the settlement currency.%n"));
			}
		}

		if (fxNdfTradeBusinessDelegate.getFixingDate(fxNdfTrade) == null) {
			errMsg.append(String.format("The fixing date is mandatory.%n"));
		}

		if (fxNdfTrade.getNdfRate() == null) {
			errMsg.append(String.format("The ndf rate is mandatory.%n"));
		} else {
			if (fxNdfTrade.getNdfRate().doubleValue() <= 0) {
				errMsg.append(
						String.format("The ndf rate (%s) must be positive.%n", fxNdfTrade.getNdfRate().doubleValue()));
			}
		}

		// Other business controls
		if (trade.getAmount() != null && trade.getAmount().doubleValue() <= 0) {
			errMsg.append(
					String.format("The notional amount (%s) must be positive.%n", trade.getAmount().doubleValue()));
		}

		if (trade.getSettlementDate() == null) {
			errMsg.append(String.format("The settlement date is mandatory.%n"));
		} else {
			// the settlement date date must be an open day for FX calendar and for both
			// currencies
			if (!fxNdfTradeBusinessDelegate.isBusinessDay(fxNdfTrade, trade.getSettlementDate())) {
				errMsg.append(String.format(
						"The settlement date must be a business day in the '%s' calendar and both FX currencies (%s/%s)'s calendars.%n",
						fxNdfTradeBusinessDelegate.getFXExchange().getCalendar().getName(),
						fxNdfTrade.getNonDeliverableCurrency(), fxNdfTrade.getCurrency()));
			}
		}

		if (trade.getTradeDate() != null) {
			// the trade date must be an open day for FX calendar
			if (!fxNdfTradeBusinessDelegate.getFXExchange().getCalendar().isBusinessDay(trade.getTradeDate())) {
				errMsg.append(String.format("The trade date must be a business day in the '%s' calendar.%n",
						fxNdfTradeBusinessDelegate.getFXExchange().getCalendar().getName()));
			}
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

	}

}