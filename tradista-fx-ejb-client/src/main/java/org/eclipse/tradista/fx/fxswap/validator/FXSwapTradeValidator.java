package org.eclipse.tradista.fx.fxswap.validator;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.validator.DefaultTradeValidator;
import org.eclipse.tradista.fx.fxswap.model.FXSwapTrade;
import org.eclipse.tradista.fx.fxswap.service.FXSwapTradeBusinessDelegate;

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

public class FXSwapTradeValidator extends DefaultTradeValidator {

	private static final long serialVersionUID = -5576047822147936643L;

	public FXSwapTradeValidator() {
	}

	@Override
	public void validateTrade(Trade<? extends Product> trade) throws TradistaBusinessException {
		FXSwapTradeBusinessDelegate fxSwapTradeBusinessDelegate = new FXSwapTradeBusinessDelegate();
		FXSwapTrade fxSwapTrade = (FXSwapTrade) trade;
		StringBuilder errMsg = validateTradeBasics(trade);
		if (fxSwapTrade.getCurrencyOne() == null) {
			errMsg.append(String.format("The currency one is mandatory.%n"));
		} else {
			if (fxSwapTrade.getCurrencyOne().equals(fxSwapTrade.getCurrency())) {
				errMsg.append(String.format("The currency one must be different of the currency two.%n"));
			}
		}

		if (fxSwapTrade.getSettlementDateForward() == null) {
			errMsg.append(String.format("The forward settlement date is mandatory.%n"));
		} else {
			if (fxSwapTrade.getSettlementDate() != null) {
				if (!fxSwapTrade.getSettlementDateForward().isAfter(fxSwapTrade.getSettlementDate())) {
					errMsg.append(
							String.format("The forward settlement date must be after the spot settlement date.%n"));
				}
			}
		}

		if (fxSwapTrade.getAmountOneSpot() == null) {
			errMsg.append(String.format("The spot amount one is mandatory.%n"));
		} else {
			if (fxSwapTrade.getAmountOneSpot().doubleValue() <= 0) {
				errMsg.append(String.format("The spot amount one (%s) must be positive.%n",
						fxSwapTrade.getAmountOneSpot().doubleValue()));
			}
		}

		if (fxSwapTrade.getAmountOneForward() == null) {
			errMsg.append(String.format("The forward amount one is mandatory.%n"));
		} else {
			if (fxSwapTrade.getAmountOneForward().doubleValue() <= 0) {
				errMsg.append(String.format("The forward amount one (%s) must be positive.%n",
						fxSwapTrade.getAmountOneForward().doubleValue()));
			}
		}

		if (fxSwapTrade.getAmountTwoForward() == null) {
			errMsg.append(String.format("The forward amount two is mandatory.%n"));
		} else {
			if (fxSwapTrade.getAmountTwoForward().doubleValue() <= 0) {
				errMsg.append(String.format("The forward amount two (%s) must be positive.%n",
						fxSwapTrade.getAmountTwoForward().doubleValue()));
			}
		}

		// Other business controls
		if (trade.getAmount() != null && trade.getAmount().doubleValue() <= 0) {
			errMsg.append(
					String.format("The spot amount two (%s) must be positive.%n", trade.getAmount().doubleValue()));
		}

		if (trade.getSettlementDate() == null) {
			errMsg.append(String.format("The settlement date is mandatory.%n"));
		} else {
			// the settlement date must be an open day for FX calendar and for
			// both
			// currencies
			if (!fxSwapTradeBusinessDelegate.isBusinessDay(fxSwapTrade, trade.getSettlementDate())) {
				errMsg.append(String.format(
						"The settlement date must be a business day in the '%s' calendar and both currencies (%s/%s)'s calendars.%n",
						fxSwapTradeBusinessDelegate.getFXExchange().getCalendar().getName(),
						fxSwapTrade.getCurrencyOne(), fxSwapTrade.getCurrency()));
			}
		}

		if (trade.getTradeDate() != null) {
			// the trade date must be an open day for FX calendar
			if (!fxSwapTradeBusinessDelegate.getFXExchange().getCalendar().isBusinessDay(trade.getTradeDate())) {
				errMsg.append(String.format("The trade date must be a business day in the '%s' calendar.%n",
						fxSwapTradeBusinessDelegate.getFXExchange().getCalendar().getName()));
			}
		}

		if (fxSwapTrade.getSettlementDateForward() != null) {
			// the forward settlement date must be an open day for FX calendar
			// and
			// for both currencies
			if (!fxSwapTradeBusinessDelegate.isBusinessDay(fxSwapTrade, fxSwapTrade.getSettlementDateForward())) {
				errMsg.append(String.format(
						"The settlement date must be a business day in the '%s' calendar and both currencies (%s/%s)'s calendars.%n",
						fxSwapTradeBusinessDelegate.getFXExchange().getCalendar().getName(),
						fxSwapTrade.getCurrencyOne(), fxSwapTrade.getCurrency()));
			}
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

	}

}
