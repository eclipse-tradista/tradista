package org.eclipse.tradista.ir.irswapoption.validator;

import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade.Style;
import org.eclipse.tradista.core.trade.validator.DefaultTradeValidator;
import org.eclipse.tradista.ir.irswap.service.IRSwapTradeBusinessDelegate;
import org.eclipse.tradista.ir.irswapoption.model.IRSwapOptionTrade;
import org.eclipse.tradista.ir.irswapoption.service.IRSwapOptionTradeBusinessDelegate;

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

public class IRSwapOptionTradeValidator extends DefaultTradeValidator {

	private static final long serialVersionUID = -7959449888423783677L;

	@Override
	public void validateTrade(Trade<? extends Product> trade) throws TradistaBusinessException {
		IRSwapOptionTrade irSwapOptionTrade = (IRSwapOptionTrade) trade;
		IRSwapOptionTradeBusinessDelegate irSwapOptionTradeBusinessDelegate = new IRSwapOptionTradeBusinessDelegate();
		StringBuilder errMsg = new StringBuilder();
		if (irSwapOptionTrade.getUnderlying() == null) {
			errMsg.append(String.format("The underlying is mandatory.%n"));
		} else {
			errMsg.append(validateTradeBasics(trade));
		}

		if (irSwapOptionTrade.getStyle() == null) {
			errMsg.append(String.format("The style is mandatory.%n"));
		}

		if (irSwapOptionTrade.getSettlementType() == null) {
			errMsg.append(String.format("The settlement type is mandatory.%n"));
		}

		if (irSwapOptionTrade.getSettlementDateOffset() < 0) {
			errMsg.append(String.format("The settlement date offset must be positive.%n"));
		}

		if (irSwapOptionTrade.getMaturityDate() == null) {
			errMsg.append(String.format("The maturity date is mandatory.%n"));
		} else {
			if (trade.getTradeDate() != null) {
				if (!irSwapOptionTrade.getMaturityDate().isAfter(trade.getTradeDate())) {
					errMsg.append(String.format("The maturity date must be after the trade date.%n"));
				}
			}
		}

		if (irSwapOptionTrade.getSettlementDate() == null) {
			errMsg.append(String.format("The settlement date is mandatory.%n"));
		} else {
			if (irSwapOptionTrade.getMaturityDate() != null) {
				if (irSwapOptionTrade.getSettlementDate().isAfter(irSwapOptionTrade.getMaturityDate())) {
					errMsg.append(String.format("The settlement date cannot be after the option maturity date.%n"));
				}
			}
		}

		if (irSwapOptionTrade.getExerciseDate() != null) {
			if (trade.getTradeDate() != null) {
				if (irSwapOptionTrade.getExerciseDate().isBefore(irSwapOptionTrade.getTradeDate())) {
					errMsg.append(String.format("The exercise date cannot be before the trade date.%n"));
				}
			}
			if (irSwapOptionTrade.getMaturityDate() != null) {
				if (irSwapOptionTrade.getStyle().equals(Style.EUROPEAN)) {
					if (!irSwapOptionTrade.getExerciseDate().equals(irSwapOptionTrade.getMaturityDate())) {
						errMsg.append(String.format(
								"As it is an %s option, The exercise date must be equal to the maturity date.%n",
								Style.EUROPEAN));
					}
				} else {
					if (irSwapOptionTrade.getExerciseDate().isAfter(irSwapOptionTrade.getMaturityDate())) {
						errMsg.append(String.format("The exercise date cannot be after the trade date.%n"));
					}
				}
			}
		}

		// the exercise date must be an open day
		if (irSwapOptionTrade.getExerciseDate() != null) {
			if (!irSwapOptionTradeBusinessDelegate.isBusinessDay(irSwapOptionTrade,
					irSwapOptionTrade.getExerciseDate())) {
				errMsg.append(String.format("The exercise date must be a business day.%n"));
			}
		}

		// the maturity date must be an open day
		if (irSwapOptionTrade.getMaturityDate() != null) {
			if (!irSwapOptionTradeBusinessDelegate.isBusinessDay(irSwapOptionTrade,
					irSwapOptionTrade.getMaturityDate())) {
				errMsg.append(String.format("The maturity date must be a business day.%n"));
			}
		}

		// the settlement date must be an open day
		if (trade.getSettlementDate() != null) {
			if (!irSwapOptionTradeBusinessDelegate.isBusinessDay(irSwapOptionTrade, trade.getSettlementDate())) {
				errMsg.append(String.format("The settlement date must be a business day.%n"));
			}
		}

		if (irSwapOptionTrade.getUnderlying() != null) {
			if ((!irSwapOptionTrade.getUnderlying().getMaturityTenor().equals(Tenor.NO_TENOR))
					&& (irSwapOptionTrade.getUnderlying().getMaturityDate() != null)) {
				LocalDate startingDate = null;
				if (irSwapOptionTrade.getExerciseDate() != null) {
					startingDate = irSwapOptionTrade.getExerciseDate();
				}
				if (irSwapOptionTrade.getStyle().equals(VanillaOptionTrade.Style.EUROPEAN)) {
					startingDate = irSwapOptionTrade.getMaturityDate();
				}
				if (startingDate != null) {
					LocalDate expectedMaturityDate = DateUtil.addTenor(startingDate.minusDays(1),
							irSwapOptionTrade.getUnderlying().getMaturityTenor());
					if (!expectedMaturityDate.isEqual(irSwapOptionTrade.getUnderlying().getMaturityDate())) {
						errMsg.append(String.format(
								"Inconsistency detected. With this exercise date %tD and this maturity %s, the maturity date should be %s. %n",
								startingDate, irSwapOptionTrade.getUnderlying().getMaturityTenor(),
								expectedMaturityDate));
					}
				}
			}
		}

		if (irSwapOptionTrade.getUnderlying() != null) {
			if (irSwapOptionTrade.getUnderlying().getAmount() != null
					&& irSwapOptionTrade.getUnderlying().getAmount().doubleValue() <= 0) {
				errMsg.append(String.format("The underlying notional amount (%s) must be positive.%n",
						trade.getAmount().doubleValue()));
			}
		}

		// in case of physical settlement, the settlement date must be an open
		// day for the currency of the underlying.
		if (irSwapOptionTrade.getUnderlyingSettlementDate() != null
				&& irSwapOptionTrade.getSettlementType().equals(OptionTrade.SettlementType.PHYSICAL)) {
			if (!new IRSwapOptionTradeBusinessDelegate().isBusinessDay(irSwapOptionTrade,
					irSwapOptionTrade.getUnderlyingSettlementDate())) {
				errMsg.append(String.format(
						"In case of Physical settlement, the settlement date must be an open day for the currency (%s) of the underlying.%n",
						irSwapOptionTrade.getUnderlying().getCurrency()));
			}
		}

		// underlying reception interest payment is mandatory
		if (irSwapOptionTrade.getUnderlying() != null) {
			if (irSwapOptionTrade.getUnderlying().getReceptionInterestPayment() == null) {
				errMsg.append(String.format("Reception interest payment of the underlying must be specified.%n"));
			}
		}

		// underlying payment interest payment is mandatory
		if (irSwapOptionTrade.getUnderlying() != null) {
			if (irSwapOptionTrade.getUnderlying().getPaymentInterestPayment() == null) {
				errMsg.append(String.format("Payment interest payment of the underlying must be specified.%n"));
			}
		}

		// underlying maturity date is mandatory
		if (irSwapOptionTrade.getUnderlying() != null) {
			if (irSwapOptionTrade.getUnderlying().getMaturityDate() == null
					&& (irSwapOptionTrade.getUnderlying().getMaturityTenor() == null
							|| irSwapOptionTrade.getUnderlying().getMaturityTenor().equals(Tenor.NO_TENOR))) {
				errMsg.append(String.format("Maturity date or maturity tenor of the underlying must be specified.%n"));
			}
		}

		if (irSwapOptionTrade.getUnderlying() != null) {
			if (irSwapOptionTrade.getUnderlying().getReceptionInterestFixing() == null) {
				errMsg.append(String.format("The underlying reception interest fixing is mandatory.%n"));
			}
		}

		if (irSwapOptionTrade.getUnderlying() != null) {
			if (irSwapOptionTrade.getUnderlying().isInterestsToPayFixed()) {
				if (irSwapOptionTrade.getUnderlying().getPaymentInterestFixing() == null) {
					errMsg.append(String.format("The underlying payment interest fixing is mandatory.%n"));
				}
			}
		}

		if (irSwapOptionTrade.getUnderlying() != null) {
			if (irSwapOptionTrade.getUnderlying().getPaymentInterestPayment() != null
					&& irSwapOptionTrade.getUnderlying().getPaymentInterestFixing() != null) {
				if (irSwapOptionTrade.getUnderlying().getPaymentInterestPayment()
						.equals(InterestPayment.BEGINNING_OF_PERIOD)
						&& irSwapOptionTrade.getUnderlying().getPaymentInterestFixing()
								.equals(InterestPayment.END_OF_PERIOD)) {
					errMsg.append(String.format(
							"It is not possible to have payment interest payment before payment interest fixing.%n"));
				}
			}
		}

		if (irSwapOptionTrade.getUnderlying() != null) {
			if (irSwapOptionTrade.getUnderlying().getReceptionInterestPayment() != null
					&& irSwapOptionTrade.getUnderlying().getReceptionInterestFixing() != null) {
				if (irSwapOptionTrade.getUnderlying().getReceptionInterestPayment()
						.equals(InterestPayment.BEGINNING_OF_PERIOD)
						&& irSwapOptionTrade.getUnderlying().getReceptionInterestFixing()
								.equals(InterestPayment.END_OF_PERIOD)) {
					errMsg.append(String.format(
							"It is not possible to have reception interest payment before reception interest fixing.%n"));
				}
			}
		}

		// in case of cash settlement, the settlement date must be an open
		// day for the Equity Option market
		if (irSwapOptionTrade.getUnderlyingSettlementDate() != null
				&& irSwapOptionTrade.getSettlementType().equals(OptionTrade.SettlementType.CASH)
				&& irSwapOptionTrade.getUnderlying() != null) {
			if (!new IRSwapTradeBusinessDelegate().isBusinessDay(irSwapOptionTrade.getUnderlying(),
					irSwapOptionTrade.getUnderlyingSettlementDate())) {
				errMsg.append(String.format(
						"In case of cash settlement, the underlying settlement date must be a business day for the currency (%s) of the underlying.%n"));
			}
		}

		// the underlying settlement date cannot be before the option exercise
		// date
		if (irSwapOptionTrade.getUnderlyingSettlementDate() != null && irSwapOptionTrade.getExerciseDate() != null) {
			if (irSwapOptionTrade.getUnderlyingSettlementDate().isBefore(irSwapOptionTrade.getExerciseDate())) {
				errMsg.append(
						String.format("the underlying settlement date cannot be before the option exercise date.%n"));
			}
		}

		// the underlying trade date cannot be before the option exercise
		// date
		if (irSwapOptionTrade.getUnderlyingTradeDate() != null && irSwapOptionTrade.getExerciseDate() != null) {
			if (irSwapOptionTrade.getUnderlyingTradeDate().isBefore(irSwapOptionTrade.getExerciseDate())) {
				errMsg.append(String.format("the underlying trade date cannot be before the option exercise date.%n"));
			}
		}

		// the underlying settlement date cannot be before the underlying trade
		// date
		if (irSwapOptionTrade.getUnderlyingSettlementDate() != null
				&& irSwapOptionTrade.getUnderlyingTradeDate() != null) {
			if (irSwapOptionTrade.getUnderlyingSettlementDate().isBefore(irSwapOptionTrade.getUnderlyingTradeDate())) {
				errMsg.append(
						String.format("the underlying settlement date cannot be before the underlying trade date.%n"));
			}
		}

		if (irSwapOptionTrade.getStrike() == null) {
			errMsg.append(String.format("The strike is mandatory.%n"));
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