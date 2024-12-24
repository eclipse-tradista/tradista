package org.eclipse.tradista.ir.irswap.validator;

import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.validator.DefaultTradeValidator;
import org.eclipse.tradista.ir.irswap.model.IRSwapTrade;

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

public class IRSwapTradeValidator extends DefaultTradeValidator {

	private static final long serialVersionUID = 7848048519693419248L;

	@Override
	public void validateTrade(Trade<? extends Product> trade) throws TradistaBusinessException {
		IRSwapTrade irSwapTrade = (IRSwapTrade) trade;
		StringBuilder errMsg = new StringBuilder();

		errMsg.append(validateTradeBasics(trade));

		if (irSwapTrade.getMaturityDate() == null) {
			errMsg.append(String.format("The maturity date is mandatory.%n"));
		} else {
			if (trade.getTradeDate() != null) {
				if (!irSwapTrade.getMaturityDate().isAfter(trade.getTradeDate())) {
					errMsg.append(String.format("The maturity date must be after the trade date.%n"));
				}
			}
			if (trade.getSettlementDate() != null) {
				if (!irSwapTrade.getMaturityDate().isAfter(trade.getSettlementDate())) {
					errMsg.append(String.format("The maturity date must be after the settlement date.%n"));
				}
			}
		}

		if ((irSwapTrade.getSettlementDate() != null) && (irSwapTrade.getMaturityTenor() != null)
				&& (!irSwapTrade.getMaturityTenor().equals(Tenor.NO_TENOR))
				&& (irSwapTrade.getMaturityDate() != null)) {
			LocalDate expectedMaturityDate = DateUtil.addTenor(irSwapTrade.getSettlementDate().minusDays(1),
					irSwapTrade.getMaturityTenor());
			if (!expectedMaturityDate.isEqual(irSwapTrade.getMaturityDate())) {
				errMsg.append(String.format(
						"Inconsistency detected. With this settlement date %tD and this maturity %s, the maturity date should be %s. %n",
						irSwapTrade.getSettlementDate(), irSwapTrade.getMaturityTenor(), expectedMaturityDate));
			}
		}

		if (irSwapTrade.getPaymentFrequency() == null) {
			errMsg.append(String.format("The payment frequency is mandatory.%n"));
		}

		if (irSwapTrade.getReceptionFrequency() == null) {
			errMsg.append(String.format("The reception frequency is mandatory.%n"));
		}

		if (irSwapTrade.getReceptionReferenceRateIndex() == null) {
			errMsg.append(String.format("The reception reference rate index is mandatory.%n"));
		}

		if (irSwapTrade.getReceptionReferenceRateIndexTenor() == null) {
			errMsg.append(String.format("The reception reference rate index tenor is mandatory.%n"));
		} else {
			if (irSwapTrade.getReceptionReferenceRateIndexTenor().equals(Tenor.NO_TENOR)) {
				errMsg.append(String.format("The reception reference rate index tenor must be different from %s.%n",
						Tenor.NO_TENOR));
			}
		}

		if (irSwapTrade.getReceptionDayCountConvention() == null) {
			errMsg.append(String.format("The reception day count convention is mandatory.%n"));
		}

		if (irSwapTrade.isInterestsToPayFixed()) {
			if (irSwapTrade.getPaymentFixedInterestRate() == null) {
				errMsg.append(String.format("The payment fixed interest rate is mandatory.%n"));
			}
		} else {
			if (irSwapTrade.getPaymentReferenceRateIndex() == null) {
				errMsg.append(String.format("The payment reference rate index is mandatory.%n"));
			}
			if (irSwapTrade.getPaymentReferenceRateIndexTenor() == null) {
				errMsg.append(String.format("The payment reference rate index tenor is mandatory.%n"));
			} else {
				if (irSwapTrade.getPaymentReferenceRateIndexTenor().equals(Tenor.NO_TENOR)) {
					errMsg.append(String.format("The payment reference rate index tenor must be different from %s.%n",
							Tenor.NO_TENOR));
				}
			}
			if (irSwapTrade.getPaymentInterestFixing() == null) {
				errMsg.append(String.format("The payment interest fixing is mandatory.%n"));
			}
		}

		if (irSwapTrade.getPaymentDayCountConvention() == null) {
			errMsg.append(String.format("The payment day count convention is mandatory.%n"));
		}

		if (irSwapTrade.getPaymentInterestPayment() == null) {
			errMsg.append(String.format("The payment interest payment is mandatory.%n"));
		}

		if (irSwapTrade.getReceptionInterestPayment() == null) {
			errMsg.append(String.format("The reception interest payment is mandatory.%n"));
		}

		if (irSwapTrade.getReceptionInterestFixing() == null) {
			errMsg.append(String.format("The reception interest fixing is mandatory.%n"));
		}

		if (irSwapTrade.getPaymentInterestPayment() != null && irSwapTrade.getPaymentInterestFixing() != null) {
			if (irSwapTrade.getPaymentInterestPayment().equals(InterestPayment.BEGINNING_OF_PERIOD)
					&& irSwapTrade.getPaymentInterestFixing().equals(InterestPayment.END_OF_PERIOD)) {
				errMsg.append(String.format(
						"It is not possible to have payment interest payment before payment interest fixing.%n"));
			}
		}

		if (irSwapTrade.getReceptionInterestPayment() != null && irSwapTrade.getReceptionInterestFixing() != null) {
			if (irSwapTrade.getReceptionInterestPayment().equals(InterestPayment.BEGINNING_OF_PERIOD)
					&& irSwapTrade.getReceptionInterestFixing().equals(InterestPayment.END_OF_PERIOD)) {
				errMsg.append(String.format(
						"It is not possible to have reception interest payment before reception interest fixing.%n"));
			}
		}

		// Other business controls
		if (trade.getAmount() != null && trade.getAmount().doubleValue() <= 0) {
			errMsg.append(
					String.format("The notional amount (%s) must be positive.%n", trade.getAmount().doubleValue()));
		}

		if (trade.getSettlementDate() == null) {
			errMsg.append(String.format("The settlement date is mandatory.%n"));
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

	}

}
