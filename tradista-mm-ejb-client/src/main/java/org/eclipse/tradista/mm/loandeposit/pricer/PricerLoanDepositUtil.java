package org.eclipse.tradista.mm.loandeposit.pricer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.mm.loandeposit.model.LoanDepositTrade;
import org.eclipse.tradista.mm.loandeposit.service.LoanDepositTradeBusinessDelegate;

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

public final class PricerLoanDepositUtil {

	public static List<CashFlow> getPayments(LoanDepositTrade mmTrade, LocalDate tradeDate, long quoteSetId,
			long indexCurveId) throws TradistaBusinessException {
		List<CashFlow> payments = new ArrayList<CashFlow>();
		BigDecimal paymentAmount;
		LoanDepositTradeBusinessDelegate loanDepositBusinessDelegate = new LoanDepositTradeBusinessDelegate();

		if (!mmTrade.getPaymentFrequency().equals(Tenor.NO_TENOR)) {

			// Settlement date is the start date of the payments
			LocalDate cfDate = mmTrade.getSettlementDate();

			while (!cfDate.isAfter(mmTrade.getEndDate())) {
				LocalDate endOfPeriod = cfDate;
				LocalDate settlementDate = cfDate;
				LocalDate fixingDate = cfDate;
				try {
					cfDate = DateUtil.addTenor(cfDate, mmTrade.getPaymentFrequency());
				} catch (TradistaBusinessException tbe) {
					// Should not appear here.
				}
				/*
				 * if the end of period is after the trade 's maturity date, new end of period
				 * id trade's maturity date
				 */
				endOfPeriod = cfDate.minusDays(1);
				if (endOfPeriod.isAfter(mmTrade.getEndDate())) {
					endOfPeriod = mmTrade.getEndDate();
				}
				if (mmTrade.getInterestPayment().equals(InterestPayment.END_OF_PERIOD)) {
					settlementDate = endOfPeriod;
				}
				if (mmTrade.getInterestFixing() != null) {
					if (mmTrade.getInterestFixing().equals(InterestPayment.END_OF_PERIOD)) {
						fixingDate = endOfPeriod;
					}
				}
				if (!settlementDate.isBefore(tradeDate)) {
					paymentAmount = loanDepositBusinessDelegate.getPaymentAmount(mmTrade, fixingDate, endOfPeriod,
							quoteSetId, indexCurveId);
					CashFlow cf = new CashFlow();
					cf.setDate(settlementDate);
					cf.setAmount(paymentAmount);
					cf.setPurpose(TransferPurpose.INTEREST_PAYMENT);
					cf.setCurrency(mmTrade.getCurrency());
					if (mmTrade.isBuy()) {
						cf.setDirection(CashFlow.Direction.PAY);
					} else {
						cf.setDirection(CashFlow.Direction.RECEIVE);
					}
					payments.add(cf);
				}
			}
		} else {
			LocalDate settlementDate = mmTrade.getSettlementDate();
			LocalDate fixingDate = mmTrade.getSettlementDate();
			if (mmTrade.getInterestPayment().equals(InterestPayment.END_OF_PERIOD)) {
				settlementDate = mmTrade.getEndDate();
			}
			if (mmTrade.getInterestFixing() != null) {
				if (mmTrade.getInterestFixing().equals(InterestPayment.END_OF_PERIOD)) {
					fixingDate = mmTrade.getEndDate();
				}
			}
			if (!settlementDate.isBefore(tradeDate)) {
				paymentAmount = loanDepositBusinessDelegate.getPaymentAmount(mmTrade, fixingDate, mmTrade.getEndDate(),
						quoteSetId, indexCurveId);
				CashFlow cf = new CashFlow();
				cf.setDate(settlementDate);
				cf.setAmount(paymentAmount);
				cf.setPurpose(TransferPurpose.INTEREST_PAYMENT);
				cf.setCurrency(mmTrade.getCurrency());
				if (mmTrade.isBuy()) {
					cf.setDirection(CashFlow.Direction.PAY);
				} else {
					cf.setDirection(CashFlow.Direction.RECEIVE);
				}
				payments.add(cf);
			}
		}
		return payments;
	}

	public static List<CashFlow> generateCashFlows(LoanDepositTrade mmTrade, LocalDate pricingDate, QuoteSet qs,
			long indexCurveId) throws TradistaBusinessException {
		if (mmTrade == null) {
			throw new TradistaBusinessException("The trade is mandatory.");
		}

		if (pricingDate == null) {
			pricingDate = LocalDate.MIN;
		}

		List<CashFlow> cfs = new ArrayList<CashFlow>();
		CashFlow notional = new CashFlow();
		CashFlow notionalGivenBack = new CashFlow();

		if (!mmTrade.getSettlementDate().isBefore(pricingDate)) {
			notional.setAmount(mmTrade.getAmount());
			notional.setCurrency(mmTrade.getCurrency());
			notional.setDate(mmTrade.getSettlementDate());
			notional.setPurpose(TransferPurpose.NOTIONAL_PAYMENT);
			if (mmTrade.isBuy()) {
				notional.setDirection(CashFlow.Direction.RECEIVE);
			} else {
				notional.setDirection(CashFlow.Direction.PAY);
			}

			cfs.add(notional);
		}

		cfs.addAll(PricerLoanDepositUtil.getPayments(mmTrade, pricingDate, qs.getId(), indexCurveId));

		if (!mmTrade.getEndDate().isBefore(pricingDate)) {
			notionalGivenBack.setAmount(mmTrade.getAmount());
			notionalGivenBack.setCurrency(mmTrade.getCurrency());
			notionalGivenBack.setDate(mmTrade.getEndDate());
			notionalGivenBack.setPurpose(TransferPurpose.NOTIONAL_PAYMENT);
			if (mmTrade.isBuy()) {
				notionalGivenBack.setDirection(CashFlow.Direction.PAY);
			} else {
				notionalGivenBack.setDirection(CashFlow.Direction.RECEIVE);
			}

			cfs.add(notionalGivenBack);
		}

		return cfs;
	}

}