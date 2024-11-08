package org.eclipse.tradista.ir.ccyswap.validator;

import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.validator.DefaultTradeValidator;
import org.eclipse.tradista.ir.ccyswap.model.CcySwapTrade;

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

public class CcySwapTradeValidator extends DefaultTradeValidator {

	private static final long serialVersionUID = -526406805298518238L;

	@Override
	public void validateTrade(Trade<? extends Product> trade) throws TradistaBusinessException {
		CcySwapTrade ccySwapTrade = (CcySwapTrade) trade;
		StringBuilder errMsg = new StringBuilder();

		errMsg.append(validateTradeBasics(trade));

		if (ccySwapTrade.getMaturityDate() == null) {
			errMsg.append(String.format("The maturity date is mandatory.%n"));
		} else {
			if (trade.getTradeDate() != null) {
				if (!ccySwapTrade.getMaturityDate().isAfter(trade.getTradeDate())) {
					errMsg.append(String.format("The maturity date must be after the trade date.%n"));
				}
			}
			if (trade.getSettlementDate() != null) {
				if (!ccySwapTrade.getMaturityDate().isAfter(trade.getSettlementDate())) {
					errMsg.append(String.format("The maturity date must be after the settlement date.%n"));
				}
			}
		}

		if ((ccySwapTrade.getSettlementDate() != null) && (ccySwapTrade.getMaturityTenor() != null)
				&& (!ccySwapTrade.getMaturityTenor().equals(Tenor.NO_TENOR))
				&& (ccySwapTrade.getMaturityDate() != null)) {
			LocalDate expectedMaturityDate = DateUtil.addTenor(ccySwapTrade.getSettlementDate().minusDays(1),
					ccySwapTrade.getMaturityTenor());
			if (!expectedMaturityDate.isEqual(ccySwapTrade.getMaturityDate())) {
				errMsg.append(String.format(
						"Inconsistency detected. With this settlement date %tD and this maturity %s, the maturity date should be %s. %n",
						ccySwapTrade.getSettlementDate(), ccySwapTrade.getMaturityTenor(), expectedMaturityDate));
			}
		}

		if (ccySwapTrade.getPaymentFrequency() == null) {
			errMsg.append(String.format("The payment frequency is mandatory.%n"));
		}

		if (ccySwapTrade.getReceptionFrequency() == null) {
			errMsg.append(String.format("The reception frequency is mandatory.%n"));
		}

		if (ccySwapTrade.getReceptionReferenceRateIndex() == null) {
			errMsg.append(String.format("The reception reference rate index is mandatory.%n"));
		}

		if (ccySwapTrade.getReceptionReferenceRateIndexTenor() == null) {
			errMsg.append(String.format("The reception reference rate index tenor is mandatory.%n"));
		} else {
			if (ccySwapTrade.getReceptionReferenceRateIndexTenor().equals(Tenor.NO_TENOR)) {
				errMsg.append(String.format("The reception reference rate index tenor must be different from %s.%n",
						Tenor.NO_TENOR));
			}
		}

		if (ccySwapTrade.getReceptionDayCountConvention() == null) {
			errMsg.append(String.format("The reception day count convention is mandatory.%n"));
		}

		if (ccySwapTrade.getPaymentInterestPayment() == null) {
			errMsg.append(String.format("The payment interest payment is mandatory.%n"));
		}

		if (ccySwapTrade.getReceptionInterestPayment() == null) {
			errMsg.append(String.format("The reception interest payment is mandatory.%n"));
		}

		if (ccySwapTrade.isInterestsToPayFixed()) {
			if (ccySwapTrade.getPaymentFixedInterestRate() == null) {
				errMsg.append(String.format("The payment fixed interest rate is mandatory.%n"));
			}
		} else {
			if (ccySwapTrade.getPaymentReferenceRateIndex() == null) {
				errMsg.append(String.format("The payment index is mandatory.%n"));
			}

			if (ccySwapTrade.getPaymentReferenceRateIndexTenor() == null) {
				errMsg.append(String.format("The payment reference rate index tenor is mandatory.%n"));
			} else {
				if (ccySwapTrade.getPaymentReferenceRateIndexTenor().equals(Tenor.NO_TENOR)) {
					errMsg.append(String.format("The payment reference rate index tenor must be different from %s.%n",
							Tenor.NO_TENOR));
				}
			}

			if (ccySwapTrade.getPaymentInterestFixing() == null) {
				errMsg.append(String.format("The payment interest fixing is mandatory.%n"));
			}
		}

		if (ccySwapTrade.getPaymentInterestPayment() != null && ccySwapTrade.getPaymentInterestFixing() != null) {
			if (ccySwapTrade.getPaymentInterestPayment().equals(InterestPayment.BEGINNING_OF_PERIOD)
					&& ccySwapTrade.getPaymentInterestFixing().equals(InterestPayment.END_OF_PERIOD)) {
				errMsg.append(String.format(
						"It is not possible to have payment interest payment before payment interest fixing.%n"));
			}
		}

		if (ccySwapTrade.getReceptionInterestPayment() != null && ccySwapTrade.getReceptionInterestFixing() != null) {
			if (ccySwapTrade.getReceptionInterestPayment().equals(InterestPayment.BEGINNING_OF_PERIOD)
					&& ccySwapTrade.getReceptionInterestFixing().equals(InterestPayment.END_OF_PERIOD)) {
				errMsg.append(String.format(
						"It is not possible to have reception interest payment before reception interest fixing.%n"));
			}
		}

		if (ccySwapTrade.getPaymentDayCountConvention() == null) {
			errMsg.append(String.format("The payment day count convention is mandatory.%n"));
		}

		if (ccySwapTrade.getCurrencyTwo() == null) {
			errMsg.append(String.format("The currency 2 is mandatory.%n"));
		} else {
			if (ccySwapTrade.getCurrency() != null) {
				if (ccySwapTrade.getCurrency().equals(ccySwapTrade.getCurrencyTwo())) {
					errMsg.append(String.format("The currency 1 (%s) must be different of the currency 2.%n",
							ccySwapTrade.getCurrency(), ccySwapTrade.getCurrencyTwo()));
				}
			}
		}

		if (ccySwapTrade.getReceptionInterestFixing() == null) {
			errMsg.append(String.format("The reception interest fixing is mandatory.%n"));
		}

		if (ccySwapTrade.getNotionalAmountTwo() != null && ccySwapTrade.getNotionalAmountTwo().doubleValue() <= 0) {
			errMsg.append(String.format("The notional amount 2 (%s) must be positive.%n",
					ccySwapTrade.getNotionalAmountTwo().doubleValue()));
		}

		// Other business controls
		if (trade.getAmount() != null && trade.getAmount().doubleValue() <= 0) {
			errMsg.append(
					String.format("The notional amount 1 (%s) must be positive.%n", trade.getAmount().doubleValue()));
		}

		if (trade.getSettlementDate() == null) {
			errMsg.append(String.format("The settlement date is mandatory.%n"));
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

	}

}