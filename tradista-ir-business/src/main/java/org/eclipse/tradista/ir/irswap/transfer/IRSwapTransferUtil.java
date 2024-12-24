package org.eclipse.tradista.ir.irswap.transfer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.ir.ccyswap.model.CcySwapTrade;
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

public final class IRSwapTransferUtil {

	/**
	 * @param trade the irswap for which we want to generate the paid cash transfers
	 */
	public static List<CashTransfer> generatePaymentCashTransfers(IRSwapTrade trade) {
		List<CashTransfer> cashTransfers = null;
		LocalDate cashFlowDate = trade.getSettlementDate();

		// When the trade is a Ccy, fixeg legs cashflows are based on notional 2
		// and currency 2.
		Currency currency;
		BigDecimal notional;
		if (trade instanceof CcySwapTrade) {
			notional = ((CcySwapTrade) trade).getNotionalAmountTwo();
			currency = ((CcySwapTrade) trade).getCurrencyTwo();
		} else {
			notional = trade.getAmount();
			currency = trade.getCurrency();
		}

		if (!trade.getPaymentFrequency().equals(Tenor.NO_TENOR)) {
			while (!cashFlowDate.isAfter(trade.getMaturityDate())) {
				LocalDate fixingDate = cashFlowDate;
				LocalDate settlementDate = cashFlowDate;
				LocalDate beginningOfPeriod = cashFlowDate;
				LocalDate endOfPeriod = cashFlowDate;
				if (cashTransfers == null) {
					cashTransfers = new ArrayList<CashTransfer>();
				}
				try {
					cashFlowDate = DateUtil.addTenor(cashFlowDate, trade.getPaymentFrequency());
				} catch (TradistaBusinessException abe) {
					// Should not appear here.
				}
				/*
				 * if the end of period is after the trade 's maturity date, new end of period
				 * id trade's maturity date
				 */
				endOfPeriod = cashFlowDate.minusDays(1);
				if (endOfPeriod.isAfter(trade.getMaturityDate())) {
					endOfPeriod = trade.getMaturityDate();
				}
				if (trade.getPaymentInterestPayment().equals(InterestPayment.END_OF_PERIOD)) {
					settlementDate = endOfPeriod;
				}
				if (trade.getPaymentInterestFixing() != null) {
					if (trade.getPaymentInterestFixing().equals(InterestPayment.END_OF_PERIOD)) {
						fixingDate = endOfPeriod;
					}
				}

				cashTransfers.add(createPaymentCashTransfer(trade, fixingDate, settlementDate, beginningOfPeriod,
						endOfPeriod, currency, notional));
			}
		} else {
			cashTransfers = new ArrayList<CashTransfer>();
			LocalDate settlementDate = trade.getSettlementDate();
			LocalDate fixingDate = trade.getSettlementDate();
			LocalDate beginningOfPeriod = trade.getSettlementDate();
			LocalDate endOfPeriod = trade.getMaturityDate();
			if (trade.getPaymentInterestPayment().equals(InterestPayment.END_OF_PERIOD)) {
				settlementDate = trade.getMaturityDate();
			}
			if (trade.getPaymentInterestFixing() != null) {
				if (trade.getPaymentInterestFixing().equals(InterestPayment.END_OF_PERIOD)) {
					fixingDate = trade.getMaturityDate();
				}
			}
			cashTransfers.add(createPaymentCashTransfer(trade, fixingDate, settlementDate, beginningOfPeriod,
					endOfPeriod, currency, notional));
		}

		// When it is a CcySwapTrade, notional for this leg is received at
		// settlement date by the trade buyer (who pays the fixed leg) and he
		// pays it back
		// at maturity date
		if (trade instanceof CcySwapTrade) {
			CashTransfer cashTransfer = new CashTransfer(trade.getBook(), TransferPurpose.FIXED_LEG_NOTIONAL_PAYMENT,
					trade.getSettlementDate(), trade, currency);
			if (trade.isBuy()) {
				cashTransfer.setDirection(Transfer.Direction.PAY);
			} else {
				cashTransfer.setDirection(Transfer.Direction.RECEIVE);
			}
			if (cashTransfers == null) {
				cashTransfers = new ArrayList<CashTransfer>();
			}
			cashTransfer.setFixingDateTime(trade.getCreationDate().atStartOfDay());
			cashTransfer.setAmount(notional);
			cashTransfer.setStatus(Transfer.Status.KNOWN);
			cashTransfer.setCreationDateTime(LocalDateTime.now());
			cashTransfers.add(cashTransfer);

			cashTransfer = new CashTransfer(trade.getBook(), TransferPurpose.FIXED_LEG_NOTIONAL_REPAYMENT,
					trade.getMaturityDate(), trade, currency);
			if (trade.isSell()) {
				cashTransfer.setDirection(Transfer.Direction.PAY);
			} else {
				cashTransfer.setDirection(Transfer.Direction.RECEIVE);
			}
			cashTransfer.setFixingDateTime(trade.getCreationDate().atStartOfDay());
			cashTransfer.setAmount(notional);
			cashTransfer.setStatus(Transfer.Status.KNOWN);
			cashTransfer.setCreationDateTime(LocalDateTime.now());
			cashTransfers.add(cashTransfer);
		}

		return cashTransfers;
	}

	/**
	 * @param trade the irswap for which we want to calculate the received cashflows
	 */
	public static List<CashTransfer> generateReceptionCashTransfers(IRSwapTrade trade)
			throws TradistaBusinessException {
		List<CashTransfer> cashTransfers = null;
		if (!trade.getReceptionFrequency().equals(Tenor.NO_TENOR)) {
			LocalDate cashFlowDate = trade.getSettlementDate();
			while (!cashFlowDate.isAfter(trade.getMaturityDate())) {
				LocalDate fixingDate = cashFlowDate;
				LocalDate settlementDate = cashFlowDate;
				LocalDate endOfPeriod = cashFlowDate;
				if (cashTransfers == null) {
					cashTransfers = new ArrayList<CashTransfer>();
				}
				cashFlowDate = DateUtil.addTenor(cashFlowDate, trade.getReceptionFrequency());
				/*
				 * if the end of period is after the trade 's maturity date, new end of period
				 * id trade's maturity date
				 */
				endOfPeriod = cashFlowDate.minusDays(1);
				if (endOfPeriod.isAfter(trade.getMaturityDate())) {
					endOfPeriod = trade.getMaturityDate();
				}
				if (trade.getReceptionInterestPayment().equals(InterestPayment.END_OF_PERIOD)) {
					settlementDate = endOfPeriod;
				}
				if (trade.getReceptionInterestFixing().equals(InterestPayment.END_OF_PERIOD)) {
					fixingDate = endOfPeriod;
				}

				cashTransfers.add(createReceptionCashTransfer(trade, fixingDate, settlementDate));
			}
		} else {
			cashTransfers = new ArrayList<CashTransfer>();
			LocalDate settlementDate = trade.getSettlementDate();
			LocalDate fixingDate = trade.getSettlementDate();
			if (trade.getReceptionInterestPayment().equals(InterestPayment.END_OF_PERIOD)) {
				settlementDate = trade.getMaturityDate();
			}
			if (trade.getReceptionInterestFixing().equals(InterestPayment.END_OF_PERIOD)) {
				fixingDate = trade.getMaturityDate();
			}
			cashTransfers.add(createReceptionCashTransfer(trade, fixingDate, settlementDate));
		}

		// When it is a CcySwapTrade, notional for this leg is received at
		// settlement date by the trade seller (who pays the floating leg) and
		// he pays it back
		// at maturity date
		if (trade instanceof CcySwapTrade) {
			CashTransfer cashTransfer = new CashTransfer(trade.getBook(), TransferPurpose.FLOATING_LEG_NOTIONAL_PAYMENT,
					trade.getSettlementDate(), trade, trade.getCurrency());
			if (trade.isBuy()) {
				cashTransfer.setDirection(Transfer.Direction.PAY);
			} else {
				cashTransfer.setDirection(Transfer.Direction.RECEIVE);
			}
			if (cashTransfers == null) {
				cashTransfers = new ArrayList<CashTransfer>();
			}
			cashTransfer.setFixingDateTime(trade.getCreationDate().atStartOfDay());
			cashTransfer.setAmount(trade.getAmount());
			cashTransfer.setStatus(Transfer.Status.KNOWN);
			cashTransfer.setCreationDateTime(LocalDateTime.now());
			cashTransfers.add(cashTransfer);
			cashTransfer = new CashTransfer(trade.getBook(), TransferPurpose.FLOATING_LEG_NOTIONAL_REPAYMENT,
					trade.getMaturityDate(), trade, trade.getCurrency());
			if (trade.isSell()) {
				cashTransfer.setDirection(Transfer.Direction.PAY);
			} else {
				cashTransfer.setDirection(Transfer.Direction.RECEIVE);
			}
			cashTransfer.setFixingDateTime(trade.getCreationDate().atStartOfDay());
			cashTransfer.setAmount(trade.getAmount());
			cashTransfer.setStatus(Transfer.Status.KNOWN);
			cashTransfer.setCreationDateTime(LocalDateTime.now());
			cashTransfers.add(cashTransfer);
		}

		return cashTransfers;
	}

	private static CashTransfer createReceptionCashTransfer(IRSwapTrade trade, LocalDate fixingDate,
			LocalDate settlementDate) throws TradistaBusinessException {
		CashTransfer cashTransfer = new CashTransfer(trade.getBook(), TransferPurpose.FLOATING_LEG_INTEREST_PAYMENT,
				settlementDate, trade, trade.getCurrency());
		cashTransfer.setFixingDateTime(fixingDate.atStartOfDay());
		if (trade.isBuy()) {
			cashTransfer.setDirection(Transfer.Direction.RECEIVE);
		} else {
			cashTransfer.setDirection(Transfer.Direction.PAY);
		}
		cashTransfer.setStatus(Transfer.Status.UNKNOWN);
		cashTransfer.setCreationDateTime(LocalDateTime.now());
		return cashTransfer;
	}

	private static CashTransfer createPaymentCashTransfer(IRSwapTrade trade, LocalDate fixingDate,
			LocalDate settlementDate, LocalDate beginningOfPeriod, LocalDate endOfPeriod, Currency currency,
			BigDecimal notional) {
		CashTransfer cashTransfer = new CashTransfer(trade.getBook(), TransferPurpose.FIXED_LEG_INTEREST_PAYMENT,
				settlementDate, trade, currency);
		BigDecimal ir = null;
		if (trade.isInterestsToPayFixed()) {
			ir = trade.getPaymentFixedInterestRate();
		}
		if (!trade.isInterestsToPayFixed()) {
			cashTransfer.setFixingDateTime(fixingDate.atStartOfDay());
		} else {
			cashTransfer.setFixingDateTime(trade.getCreationDate().atStartOfDay());
		}

		// the fractioned notional is the notional of the reception leg
		// * accrual
		// factor calculated using the period between the fixing
		// date and the payment date.
		if (trade.isInterestsToPayFixed()) {
			ConfigurationBusinessDelegate cbs = new ConfigurationBusinessDelegate();
			BigDecimal fractionedNotional = notional.multiply(
					PricerUtil.daysToYear(trade.getPaymentDayCountConvention(), beginningOfPeriod, endOfPeriod));
			BigDecimal payment = fractionedNotional
					.multiply(ir.divide(BigDecimal.valueOf(100), cbs.getScale(), cbs.getRoundingMode()));
			if (trade.isBuy()) {
				if (payment.signum() > 0) {
					cashTransfer.setAmount(payment);
					cashTransfer.setDirection(Transfer.Direction.PAY);
				} else {
					cashTransfer.setDirection(Transfer.Direction.RECEIVE);
					cashTransfer.setAmount(payment.negate());
				}
			} else {
				if (payment.signum() > 0) {
					cashTransfer.setAmount(payment);
					cashTransfer.setDirection(Transfer.Direction.RECEIVE);
				} else {
					cashTransfer.setDirection(Transfer.Direction.PAY);
					cashTransfer.setAmount(payment.negate());
				}
			}
			cashTransfer.setStatus(Transfer.Status.KNOWN);
		} else {
			cashTransfer.setStatus(Transfer.Status.UNKNOWN);
		}
		cashTransfer.setCreationDateTime(LocalDateTime.now());

		return cashTransfer;
	}

}