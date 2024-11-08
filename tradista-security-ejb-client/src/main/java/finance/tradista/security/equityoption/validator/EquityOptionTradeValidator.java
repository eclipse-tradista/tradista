package org.eclipse.tradista.security.equityoption.validator;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade.Style;
import org.eclipse.tradista.core.trade.validator.DefaultTradeValidator;
import org.eclipse.tradista.security.equityoption.model.EquityOptionTrade;
import org.eclipse.tradista.security.equityoption.service.EquityOptionTradeBusinessDelegate;

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

public class EquityOptionTradeValidator extends DefaultTradeValidator {

	private static final long serialVersionUID = 714149566720466546L;

	@Override
	public void validateTrade(Trade<? extends Product> trade) throws TradistaBusinessException {
		EquityOptionTrade equityOptionTrade = (EquityOptionTrade) trade;
		EquityOptionTradeBusinessDelegate equityOptionTradeBusinessDelegate = new EquityOptionTradeBusinessDelegate();
		StringBuilder errMsg = validateTradeBasics(trade);
		if (equityOptionTrade.getUnderlying() == null) {
			errMsg.append(String.format("The underlying is mandatory.%n"));
		} else {
			// Validate the underlying
			if (equityOptionTrade.getUnderlying().getAmount() == null) {
				errMsg.append(String.format("The underlying's price is mandatory.%n"));
			} else {
				if (equityOptionTrade.getUnderlying().getAmount().doubleValue() <= 0) {
					errMsg.append(String.format("The underlying's price (%s) must be positive.%n",
							equityOptionTrade.getUnderlying().getAmount().doubleValue()));
				}
			}
			if (equityOptionTrade.getUnderlying().getCurrency() == null) {
				errMsg.append(String.format("The underlying's currency is mandatory.%n"));
			}
			if (equityOptionTrade.getUnderlying().getCounterparty() == null) {
				errMsg.append(String.format("The underlying's counterparty is mandatory.%n"));
			}
			if (equityOptionTrade.getUnderlying().getBook() == null) {
				errMsg.append(String.format("The underlying's book is mandatory.%n"));
			}
			if (equityOptionTrade.getUnderlying().getProduct() == null) {
				errMsg.append(String.format("The underlying's equity is mandatory.%n"));
			}

			if (equityOptionTrade.getUnderlying().getQuantity() == null) {
				errMsg.append(String.format("The underlying's quantity is mandatory.%n"));
			} else {
				if (equityOptionTrade.getUnderlying().getQuantity().doubleValue() <= 0) {
					errMsg.append(String.format("The underlying's quantity (%s) must be positive.%n",
							equityOptionTrade.getUnderlying().getQuantity().doubleValue()));
				}
			}
		}

		if (equityOptionTrade.getStyle() == null) {
			errMsg.append(String.format("The style is mandatory.%n"));
		}

		if (equityOptionTrade.getSettlementType() == null) {
			errMsg.append(String.format("The settlement type is mandatory.%n"));
		}

		if (equityOptionTrade.getSettlementDateOffset() < 0) {
			errMsg.append(String.format("The settlement date offset must be positive.%n"));
		}

		if (equityOptionTrade.getStrike() == null) {
			errMsg.append(String.format("The strike is mandatory.%n"));
		} else {
			if (equityOptionTrade.getStrike().doubleValue() <= 0) {
				errMsg.append(String.format("The strike (%s) must be positive.%n",
						equityOptionTrade.getStrike().doubleValue()));
			}
		}

		if (equityOptionTrade.getMaturityDate() == null) {
			errMsg.append(String.format("The maturity date is mandatory.%n"));
		} else {
			if (trade.getTradeDate() != null) {
				if (!equityOptionTrade.getMaturityDate().isAfter(trade.getTradeDate())) {
					errMsg.append(String.format("The maturity date must be after the trade date.%n"));
				}
			}
		}

		if (equityOptionTrade.getSettlementDate() == null) {
			errMsg.append(String.format("The settlement date is mandatory.%n"));
		} else {
			if (equityOptionTrade.getMaturityDate() != null) {
				if (equityOptionTrade.getSettlementDate().isAfter(equityOptionTrade.getMaturityDate())) {
					errMsg.append(String.format("The settlement date cannot be after the option maturity date.%n"));
				}
			}
		}

		if (equityOptionTrade.getExerciseDate() != null) {
			if (trade.getTradeDate() != null) {
				if (equityOptionTrade.getExerciseDate().isBefore(equityOptionTrade.getTradeDate())) {
					errMsg.append(String.format("The exercise date cannot be before the trade date.%n"));
				}
			}
			if (equityOptionTrade.getMaturityDate() != null) {
				if (equityOptionTrade.getStyle().equals(Style.EUROPEAN)) {
					if (!equityOptionTrade.getExerciseDate().equals(equityOptionTrade.getMaturityDate())) {
						errMsg.append(String.format(
								"As it is an %s option, The exercise date must be equal to the maturity date.%n",
								Style.EUROPEAN));
					}
				} else {
					if (equityOptionTrade.getExerciseDate().isAfter(equityOptionTrade.getMaturityDate())) {
						errMsg.append(String.format("The exercise date cannot be after the trade date.%n"));
					}
				}
			}
		}

		// the exercise date must be an open day
		if (equityOptionTrade.getExerciseDate() != null) {
			if (!equityOptionTradeBusinessDelegate.isBusinessDay(equityOptionTrade,
					equityOptionTrade.getExerciseDate())) {
				errMsg.append(String.format("The exercise date must be a business day.%n"));
			}
		}

		// the trade date must be an open day
		if (trade.getTradeDate() != null && equityOptionTrade.getEquityOption() != null) {
			if (!equityOptionTradeBusinessDelegate.isBusinessDay(equityOptionTrade, equityOptionTrade.getTradeDate())) {
				errMsg.append(String.format("The trade date must be a business day.%n"));
			}
		}

		// in case of physical settlement, the settlement date must be an open
		// day for the underlying's exchange
		if (equityOptionTrade.getUnderlyingSettlementDate() != null
				&& equityOptionTrade.getSettlementType().equals(OptionTrade.SettlementType.PHYSICAL)
				&& equityOptionTrade.getUnderlying() != null && equityOptionTrade.getUnderlying().getProduct() != null
				&& equityOptionTrade.getUnderlying().getProduct().getExchange() != null
				&& equityOptionTrade.getUnderlying().getProduct().getExchange().getCalendar() != null) {
			if (!equityOptionTrade.getUnderlying().getProduct().getExchange().getCalendar()
					.isBusinessDay(equityOptionTrade.getUnderlyingSettlementDate())) {
				errMsg.append(String.format(
						"In case of Physical settlement, the underlying settlement date must be a business day for the exchange of the underlying: %s.%n",
						equityOptionTrade.getUnderlying().getProduct().getExchange()));
			}
		}

		// in case of cash settlement, the settlement date must be an open
		// day for the Equity Option market
		if (equityOptionTrade.getUnderlyingSettlementDate() != null
				&& equityOptionTrade.getSettlementType().equals(OptionTrade.SettlementType.CASH)) {
			if (!equityOptionTradeBusinessDelegate.isBusinessDay(equityOptionTrade,
					equityOptionTrade.getUnderlyingSettlementDate())) {
				errMsg.append(String.format(
						"In case of cash settlement, the underlying settlement date must be a business day in the Equity Option Market.%n"));
			}
		}

		// the underlying settlement date cannot be before the option exercise
		// date
		if (equityOptionTrade.getUnderlyingSettlementDate() != null && equityOptionTrade.getExerciseDate() != null) {
			if (equityOptionTrade.getUnderlyingSettlementDate().isBefore(equityOptionTrade.getExerciseDate())) {
				errMsg.append(
						String.format("the underlying settlement date cannot be before the option exercise date.%n"));
			}
		}

		// the underlying trade date cannot be before the option exercise
		// date
		if (equityOptionTrade.getUnderlyingTradeDate() != null && equityOptionTrade.getExerciseDate() != null) {
			if (equityOptionTrade.getUnderlyingTradeDate().isBefore(equityOptionTrade.getExerciseDate())) {
				errMsg.append(String.format("the underlying trade date cannot be before the option exercise date.%n"));
			}
		}

		// the underlying settlement date cannot be before the underlying trade
		// date
		if (equityOptionTrade.getUnderlyingSettlementDate() != null
				&& equityOptionTrade.getUnderlyingTradeDate() != null) {
			if (equityOptionTrade.getUnderlyingSettlementDate().isBefore(equityOptionTrade.getUnderlyingTradeDate())) {
				errMsg.append(
						String.format("the underlying settlement date cannot be before the underlying trade date.%n"));
			}
		}

		// the maturity date must be an open day
		if (equityOptionTrade.getMaturityDate() != null) {
			if (!equityOptionTradeBusinessDelegate.isBusinessDay(equityOptionTrade,
					equityOptionTrade.getMaturityDate())) {
				errMsg.append(String.format("The maturity date must be a business day.%n"));
			}
		}

		// If it is a listed equity option, the quantity is mandatory

		if (equityOptionTrade.getEquityOption() != null) {
			if (equityOptionTrade.getQuantity() == null) {
				errMsg.append(String.format("The quantity is mandatory when a listed quity option is traded.%n"));
			} else {
				if (equityOptionTrade.getQuantity().doubleValue() <= 0) {
					errMsg.append(String.format("The quantity must be positive.%n"));
				}
			}
		}

		// Other business controls
		if (trade.getAmount() != null && trade.getAmount().doubleValue() <= 0) {
			errMsg.append(String.format("The premium (%s) must be positive.%n", trade.getAmount().doubleValue()));
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

	}

}