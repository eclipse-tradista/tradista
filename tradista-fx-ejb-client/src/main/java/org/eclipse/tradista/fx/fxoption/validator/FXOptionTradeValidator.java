package org.eclipse.tradista.fx.fxoption.validator;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade.Style;
import org.eclipse.tradista.core.trade.validator.DefaultTradeValidator;
import org.eclipse.tradista.fx.fx.service.FXTradeBusinessDelegate;
import org.eclipse.tradista.fx.fxoption.model.FXOptionTrade;
import org.eclipse.tradista.fx.fxoption.service.FXOptionTradeBusinessDelegate;

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

public class FXOptionTradeValidator extends DefaultTradeValidator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 317158780737799356L;

	public FXOptionTradeValidator() {
	}

	@Override
	public void validateTrade(Trade<? extends Product> trade) throws TradistaBusinessException {
		FXOptionTradeBusinessDelegate fxOptionTradeBusinessDelegate = new FXOptionTradeBusinessDelegate();
		FXOptionTrade fxOptionTrade = (FXOptionTrade) trade;
		StringBuilder errMsg = validateTradeBasics(trade);
		if (fxOptionTrade.getUnderlying() == null) {
			errMsg.append(String.format("The underlying is mandatory.%n"));
		} else {
			// Validate the underlying

			if (fxOptionTrade.getUnderlying().getAmountOne() == null) {
				errMsg.append(String.format("The underlying's amount one is mandatory.%n"));
			} else {
				if (fxOptionTrade.getUnderlying().getAmountOne().doubleValue() <= 0) {
					errMsg.append(String.format("The underlying's amount one (%s) must be positive.%n",
							fxOptionTrade.getUnderlying().getAmountOne().doubleValue()));
				}
			}

			if (fxOptionTrade.getUnderlying().getAmount() == null) {
				errMsg.append(String.format("The underlying's amount two is mandatory.%n"));
			} else {
				if (fxOptionTrade.getUnderlying().getAmount().doubleValue() <= 0) {
					errMsg.append(String.format("The underlying's amount two (%s) must be positive.%n",
							fxOptionTrade.getUnderlying().getAmount().doubleValue()));
				}
			}
			if (fxOptionTrade.getUnderlying().getCurrency() == null) {
				errMsg.append(String.format("The underlying's currency two is mandatory.%n"));
			}
			if (fxOptionTrade.getUnderlying().getCounterparty() == null) {
				errMsg.append(String.format("The underlying's counterparty is mandatory.%n"));
			}
			if (fxOptionTrade.getUnderlying().getBook() == null) {
				errMsg.append(String.format("The underlying's book is mandatory.%n"));
			}

			if (fxOptionTrade.getUnderlying().getCurrencyOne() == null) {
				errMsg.append(String.format("The underlying's currency one is mandatory.%n"));
			} else {
				if (fxOptionTrade.getUnderlying().getCurrency() != null) {
					if (fxOptionTrade.getUnderlying().getCurrencyOne()
							.equals((fxOptionTrade.getUnderlying().getCurrency()))) {
						errMsg.append(String.format(
								"The underlying's currency one (%s) must be different of underlying's currency two (%s).%n",
								fxOptionTrade.getUnderlying().getCurrencyOne(),
								fxOptionTrade.getUnderlying().getCurrency()));
					}
				}
			}
		}

		if (fxOptionTrade.getStyle() == null) {
			errMsg.append(String.format("The style is mandatory.%n"));
		}

		if (fxOptionTrade.getSettlementType() == null) {
			errMsg.append(String.format("The settlement type is mandatory.%n"));
		}

		if (fxOptionTrade.getSettlementDateOffset() < 0) {
			errMsg.append(String.format("The settlement date offset must be positive.%n"));
		}

		if (fxOptionTrade.getMaturityDate() == null) {
			errMsg.append(String.format("The maturity date is mandatory.%n"));
		} else {
			if (trade.getTradeDate() != null) {
				if (!fxOptionTrade.getMaturityDate().isAfter(trade.getTradeDate())) {
					errMsg.append(String.format("The maturity date must be after the trade date.%n"));
				}
			}
		}

		if (fxOptionTrade.getSettlementDate() == null) {
			errMsg.append(String.format("The settlement date is mandatory.%n"));
		} else {
			if (fxOptionTrade.getMaturityDate() != null) {
				if (fxOptionTrade.getSettlementDate().isAfter(fxOptionTrade.getMaturityDate())) {
					errMsg.append(String.format("The settlement date cannot be after the option maturity date.%n"));
				}
			}
		}

		if (fxOptionTrade.getExerciseDate() != null) {
			if (trade.getTradeDate() != null) {
				if (fxOptionTrade.getExerciseDate().isBefore(fxOptionTrade.getTradeDate())) {
					errMsg.append(String.format("The exercise date cannot be before the trade date.%n"));
				}
			}
			if (fxOptionTrade.getMaturityDate() != null) {
				if (fxOptionTrade.getStyle().equals(Style.EUROPEAN)) {
					if (!fxOptionTrade.getExerciseDate().equals(fxOptionTrade.getMaturityDate())) {
						errMsg.append(String.format(
								"As it is an %s option, The exercise date must be equal to the maturity date.%n",
								Style.EUROPEAN));
					}
				} else {
					if (fxOptionTrade.getExerciseDate().isAfter(fxOptionTrade.getMaturityDate())) {
						errMsg.append(String.format("The exercise date cannot be after the maturity date.%n"));
					}
				}
			}
		}

		// the exercise date must be an open day for FX calendar
		if (fxOptionTrade.getExerciseDate() != null) {
			if (!fxOptionTradeBusinessDelegate.getFXExchange().getCalendar()
					.isBusinessDay(fxOptionTrade.getExerciseDate())) {
				errMsg.append(String.format("The exercise date must be a business day in the '%s' calendar.%n",
						fxOptionTradeBusinessDelegate.getFXExchange().getCalendar().getName()));
			}
		}

		// the trade date must be an open day for FX calendar
		if (trade.getTradeDate() != null) {
			if (!fxOptionTradeBusinessDelegate.getFXExchange().getCalendar().isBusinessDay(trade.getTradeDate())) {
				errMsg.append(String.format("The trade date must be a business day in the '%s' calendar.%n",
						fxOptionTradeBusinessDelegate.getFXExchange().getCalendar().getName()));
			}
		}

		// the settlement date must be an open day for FX calendar
		if (trade.getSettlementDate() != null) {
			if (!fxOptionTradeBusinessDelegate.getFXExchange().getCalendar().isBusinessDay(trade.getSettlementDate())) {
				errMsg.append(String.format("The settlement date must be a business day in the '%s' calendar.%n",
						fxOptionTradeBusinessDelegate.getFXExchange().getCalendar().getName()));
			}
		}

		// in case of physical settlement, the settlement date must be an open
		// day for both currencies of the underlying
		if (fxOptionTrade.getUnderlyingSettlementDate() != null
				&& fxOptionTrade.getSettlementType().equals(OptionTrade.SettlementType.PHYSICAL)) {
			if (!new FXTradeBusinessDelegate().isBusinessDay(fxOptionTrade.getUnderlying(),
					fxOptionTrade.getUnderlyingSettlementDate())) {
				errMsg.append(String.format(
						"In case of Physical settlement, the settlement date must be an open day for the FX market and for both currencies (%s and %s) of the underlying.%n",
						fxOptionTrade.getUnderlying().getCurrencyOne(), fxOptionTrade.getUnderlying().getCurrency()));
			}
		}

		// in case of cash settlement, the settlement date must be an open
		// day for the Equity Option market
		if (fxOptionTrade.getUnderlyingSettlementDate() != null
				&& fxOptionTrade.getSettlementType().equals(OptionTrade.SettlementType.CASH)
				&& fxOptionTrade.getUnderlying() != null && fxOptionTradeBusinessDelegate.getFXExchange() != null
				&& fxOptionTradeBusinessDelegate.getFXExchange().getCalendar() != null) {
			if (!fxOptionTradeBusinessDelegate.getFXExchange().getCalendar()
					.isBusinessDay(fxOptionTrade.getUnderlyingSettlementDate())) {
				errMsg.append(String.format(
						"In case of cash settlement, the underlying settlement date must be a business day in the FX Market.%n"));
			}
		}

		// the underlying settlement date cannot be before the option exercise
		// date
		if (fxOptionTrade.getUnderlyingSettlementDate() != null && fxOptionTrade.getExerciseDate() != null) {
			if (fxOptionTrade.getUnderlyingSettlementDate().isBefore(fxOptionTrade.getExerciseDate())) {
				errMsg.append(
						String.format("the underlying settlement date cannot be before the option exercise date.%n"));
			}
		}

		// the underlying trade date cannot be before the option exercise
		// date
		if (fxOptionTrade.getUnderlyingTradeDate() != null && fxOptionTrade.getExerciseDate() != null) {
			if (fxOptionTrade.getUnderlyingTradeDate().isBefore(fxOptionTrade.getExerciseDate())) {
				errMsg.append(String.format("the underlying trade date cannot be before the option exercise date.%n"));
			}
		}

		// the underlying settlement date cannot be before the underlying trade
		// date
		if (fxOptionTrade.getUnderlyingSettlementDate() != null && fxOptionTrade.getUnderlyingTradeDate() != null) {
			if (fxOptionTrade.getUnderlyingSettlementDate().isBefore(fxOptionTrade.getUnderlyingTradeDate())) {
				errMsg.append(
						String.format("the underlying settlement date cannot be before the underlying trade date.%n"));
			}
		}

		if (fxOptionTrade.getStrike() == null) {
			errMsg.append(String.format("The strike is mandatory.%n"));
		} else {
			if (fxOptionTrade.getStrike().doubleValue() <= 0) {
				errMsg.append(
						String.format("The strike (%s) must be positive.%n", fxOptionTrade.getStrike().doubleValue()));
			}
		}

		// Other business controls
		if (trade.getAmount() != null && trade.getAmount().doubleValue() <= 0) {
			errMsg.append(String.format("The premium (%s) must be positive.%n", trade.getAmount().doubleValue()));
		}

		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}

	}

}