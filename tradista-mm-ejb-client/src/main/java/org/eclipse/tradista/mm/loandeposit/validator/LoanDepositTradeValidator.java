package org.eclipse.tradista.mm.loandeposit.validator;

import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.validator.DefaultTradeValidator;
import org.eclipse.tradista.mm.loandeposit.model.LoanDepositTrade;
import org.eclipse.tradista.mm.loandeposit.model.LoanDepositTrade.InterestType;

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

public class LoanDepositTradeValidator extends DefaultTradeValidator {

	private static final long serialVersionUID = -6295799455164597269L;

	@Override
	public void validateTrade(Trade<? extends Product> trade) throws TradistaBusinessException {
		LoanDepositTrade mmTrade = (LoanDepositTrade) trade;
		StringBuilder errMsg = validateTradeBasics(trade);
		if (mmTrade.getSettlementDate() == null) {
			errMsg.append(String.format("The start date is mandatory.%n"));
		} else {
			if (mmTrade.getTradeDate() != null && (!mmTrade.getSettlementDate().isAfter(mmTrade.getTradeDate()))) {
				errMsg.append(String.format("The start date must be after the trade date.%n"));
			}
		}

		if (mmTrade.getEndDate() == null) {
			errMsg.append(String.format("The end date is mandatory.%n"));
		} else {
			if (mmTrade.getTradeDate() != null && (!mmTrade.getEndDate().isAfter(mmTrade.getTradeDate()))) {
				errMsg.append(String.format("The end date must be after the trade date.%n"));
			}
			if (mmTrade.getSettlementDate() != null && (!mmTrade.getEndDate().isAfter(mmTrade.getSettlementDate()))) {
				errMsg.append(String.format("The end date must be after the start date.%n"));
			}
		}

		if (mmTrade.getPaymentFrequency() == null) {
			errMsg.append(String.format("The payment frequency is mandatory.%n"));
		}

		if (mmTrade.getDayCountConvention() == null) {
			errMsg.append(String.format("The day count convention is mandatory.%n"));
		}

		if (mmTrade.getInterestPayment() == null) {
			errMsg.append(String.format("The interest payment is mandatory.%n"));
		}

		if ((mmTrade.getSettlementDate() != null) && (mmTrade.getMaturity() != null)
				&& (!mmTrade.getMaturity().equals(Tenor.NO_TENOR)) && (mmTrade.getEndDate() != null)) {
			LocalDate expectedMaturityDate = DateUtil.addTenor(mmTrade.getSettlementDate().minusDays(1),
					mmTrade.getMaturity());
			if (!expectedMaturityDate.isEqual(mmTrade.getEndDate())) {
				errMsg.append(String.format(
						"Inconsistency detected. With this start date %tD and this maturity %s, the end date should be %s. %n",
						mmTrade.getSettlementDate(), mmTrade.getMaturity(), expectedMaturityDate));
			}
		}

		if (mmTrade.getFloatingRateIndex() != null && mmTrade.getFixedRate() != null) {
			errMsg.append(String.format("Floating and Fixed rates cannot be both present.%n"));
		} else {
			if (mmTrade.getFloatingRateIndex() == null && mmTrade.getFixedRate() == null) {
				errMsg.append(String.format("Floating and Fixed rates cannot be both null.%n"));
			}
			if (mmTrade.getFixedRate() != null && mmTrade.getFixedRate().doubleValue() <= 0) {
				errMsg.append(
						String.format("The fixed rate (%s) must be positive.%n", mmTrade.getFixedRate().doubleValue()));
			}
			if (mmTrade.getFloatingRateIndex() != null && mmTrade.getFixingPeriod() == null) {
				errMsg.append(String.format("Fixing Period is mandatory when floating rate index is present.%n"));
			}
			if (mmTrade.getFloatingRateIndex() != null && mmTrade.getInterestFixing() == null) {
				errMsg.append(String.format("Interest Fixing is mandatory when floating rate index is present.%n"));
			}
			if (mmTrade.getFloatingRateIndex() != null && mmTrade.getFloatingRateIndexTenor() == null) {
				errMsg.append(
						String.format("Floating rate index tenor is mandatory when floating rate index is present.%n"));
			}
			if (mmTrade.getFloatingRateIndex() != null) {
				if (mmTrade.getSpread() != null && mmTrade.getSpread().doubleValue() <= 0) {
					errMsg.append(
							String.format("The spread (%s) must be positive.%n", mmTrade.getSpread().doubleValue()));
				}
			}
			if (mmTrade.getInterestPayment() != null && mmTrade.getInterestFixing() != null) {
				if (mmTrade.getInterestPayment().equals(InterestPayment.BEGINNING_OF_PERIOD)
						&& mmTrade.getInterestFixing().equals(InterestPayment.END_OF_PERIOD)) {
					errMsg.append(
							String.format("It is not possible to have interest payment before interest fixing.%n"));
				}
			}
		}

		if (mmTrade.isCompoundInterest()) {
			if (mmTrade.getCompoundPeriod() == null) {
				errMsg.append(String.format("Compound period is mandatory when interest type is %s.%n",
						InterestType.COMPOUND));
			}
		}

		// Other business controls
		if (trade.getAmount() != null && trade.getAmount().doubleValue() <= 0) {
			errMsg.append(String.format("The principal (%s) must be positive.%n", trade.getAmount().doubleValue()));
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

	}

}