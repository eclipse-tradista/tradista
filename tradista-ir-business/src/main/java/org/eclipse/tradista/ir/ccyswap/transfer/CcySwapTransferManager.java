package org.eclipse.tradista.ir.ccyswap.transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.FixingError;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.TransferManager;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.FixingErrorBusinessDelegate;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.eclipse.tradista.ir.ccyswap.messaging.CcySwapTradeEvent;
import org.eclipse.tradista.ir.ccyswap.model.CcySwapTrade;
import org.eclipse.tradista.ir.irswap.transfer.IRSwapTransferUtil;

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

public class CcySwapTransferManager implements TransferManager<CcySwapTradeEvent> {

	protected TransferBusinessDelegate transferBusinessDelegate;

	protected FixingErrorBusinessDelegate fixingErrorBusinessDelegate;

	protected ConfigurationBusinessDelegate configurationBusinessDelegate;

	public CcySwapTransferManager() {
		transferBusinessDelegate = new TransferBusinessDelegate();
		fixingErrorBusinessDelegate = new FixingErrorBusinessDelegate();
		configurationBusinessDelegate = new ConfigurationBusinessDelegate();
	}

	@Override
	public void createTransfers(CcySwapTradeEvent event) throws TradistaBusinessException {
		CcySwapTrade trade = event.getTrade();
		List<CashTransfer> paymentTransfers = null;
		List<CashTransfer> receptionTransfers = null;
		List<Transfer> transfersToBeSaved = new ArrayList<Transfer>();

		if (event.getOldTrade() != null) {
			CcySwapTrade oldTrade = event.getOldTrade();
			if (!oldTrade.getCurrencyTwo().equals(trade.getCurrencyTwo())
					|| oldTrade.getNotionalAmountTwo().compareTo(trade.getNotionalAmountTwo()) != 0
					|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())
					|| !oldTrade.getMaturityDate().isEqual(trade.getMaturityDate())
					|| oldTrade.isInterestsToPayFixed() != trade.isInterestsToPayFixed()
					|| (oldTrade.getPaymentFixedInterestRate() != null && trade.getPaymentFixedInterestRate() != null
							&& (oldTrade.getPaymentFixedInterestRate()
									.compareTo(trade.getPaymentFixedInterestRate()) != 0))
					|| !oldTrade.getPaymentFrequency().equals(trade.getPaymentFrequency())
					|| !oldTrade.getPaymentInterestPayment().equals(trade.getPaymentInterestPayment())
					// At this stage, we know that trade and old trade are both payment fixed rates
					// or floating rates
					|| ((oldTrade.getPaymentInterestFixing() != null)
							&& (!oldTrade.getPaymentInterestFixing().equals(trade.getPaymentInterestFixing())))
					|| (oldTrade.isBuy() != trade.isBuy()) || (!oldTrade.getBook().equals(trade.getBook()))) {
				List<Transfer> transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
						TransferPurpose.FIXED_LEG_INTEREST_PAYMENT, false);
				// if the transfers list is null or empty, it is not normal, but
				// the process
				// should
				// continue.
				if (transfers == null || transfers.isEmpty()) {
					// TODO logs + Errors viewable in the error report ?
				} else {
					for (Transfer transfer : transfers) {
						transfer.setStatus(Transfer.Status.CANCELED);
						transfersToBeSaved.add(transfer);
					}
				}
				transfersToBeSaved.addAll(createNewFixedLegInterestPayments(paymentTransfers, trade));

			}
			if (!oldTrade.getCurrencyTwo().equals(trade.getCurrencyTwo())
					|| oldTrade.getNotionalAmountTwo().compareTo(trade.getNotionalAmountTwo()) != 0
					|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())
					|| !oldTrade.getMaturityDate().isEqual(trade.getMaturityDate())
					|| (oldTrade.isBuy() != trade.isBuy()) || (!oldTrade.getBook().equals(trade.getBook()))) {
				List<Transfer> transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
						TransferPurpose.FIXED_LEG_NOTIONAL_PAYMENT, false);

				if (transfers != null) {
					transfers.addAll(transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
							TransferPurpose.FIXED_LEG_NOTIONAL_REPAYMENT, false));
				} else {
					transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
							TransferPurpose.FIXED_LEG_NOTIONAL_REPAYMENT, false);
				}
				// if the transfers list is null or empty, it is not normal, but
				// the process
				// should
				// continue.
				if (transfers == null || transfers.isEmpty()) {
					// TODO logs + Errors viewable in the error report ?
				} else {
					for (Transfer transfer : transfers) {
						transfer.setStatus(Transfer.Status.CANCELED);
						transfersToBeSaved.add(transfer);
					}
				}
				transfersToBeSaved.addAll(createNewFixedLegNotionalPayments(paymentTransfers, trade));

			}
			if (!oldTrade.getCurrency().equals(trade.getCurrency())
					|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())
					|| !oldTrade.getMaturityDate().isEqual(trade.getMaturityDate())
					|| !oldTrade.getReceptionFrequency().equals(trade.getReceptionFrequency())
					|| !oldTrade.getReceptionInterestPayment().equals(trade.getReceptionInterestPayment())
					|| !oldTrade.getReceptionInterestFixing().equals(trade.getReceptionInterestFixing())
					|| (oldTrade.isBuy() != trade.isBuy()) || (!oldTrade.getBook().equals(trade.getBook()))) {
				List<Transfer> transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
						TransferPurpose.FLOATING_LEG_INTEREST_PAYMENT, false);
				// if the transfer is null, it is not normal, but the process
				// should
				// continue.
				if (transfers == null || transfers.isEmpty()) {
					// TODO logs + Errors viewable in the error report ?
				} else {
					for (Transfer transfer : transfers) {
						transfer.setStatus(Transfer.Status.CANCELED);
						transfersToBeSaved.add(transfer);
					}
				}
				transfersToBeSaved.addAll(createNewFloatingLegInterestPayments(receptionTransfers, trade));
			}
			if (!oldTrade.getCurrency().equals(trade.getCurrency())
					|| oldTrade.getAmount().compareTo(trade.getAmount()) != 0
					|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())
					|| !oldTrade.getMaturityDate().isEqual(trade.getMaturityDate())
					|| (oldTrade.isBuy() != trade.isBuy()) || (!oldTrade.getBook().equals(trade.getBook()))) {
				List<Transfer> transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
						TransferPurpose.FLOATING_LEG_NOTIONAL_PAYMENT, false);
				if (transfers != null) {
					transfers.addAll(transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
							TransferPurpose.FLOATING_LEG_NOTIONAL_REPAYMENT, false));
				} else {
					transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
							TransferPurpose.FLOATING_LEG_NOTIONAL_REPAYMENT, false);
				}
				// if the transfers list is null or empty, it is not normal, but
				// the process
				// should
				// continue.
				if (transfers == null || transfers.isEmpty()) {
					// TODO logs + Errors viewable in the error report ?
				} else {
					for (Transfer transfer : transfers) {
						transfer.setStatus(Transfer.Status.CANCELED);
						transfersToBeSaved.add(transfer);
					}
				}
				transfersToBeSaved.addAll(createNewFloatingLegNotionalPayments(receptionTransfers, trade));
			}

		} else {
			transfersToBeSaved.addAll(createNewFixedLegInterestPayments(paymentTransfers, trade));
			transfersToBeSaved.addAll(createNewFloatingLegInterestPayments(receptionTransfers, trade));
			transfersToBeSaved.addAll(createNewFixedLegNotionalPayments(paymentTransfers, trade));
			transfersToBeSaved.addAll(createNewFloatingLegNotionalPayments(receptionTransfers, trade));
		}
		if (!transfersToBeSaved.isEmpty()) {
			transferBusinessDelegate.saveTransfers(transfersToBeSaved);
		}

	}

	private List<Transfer> createNewFixedLegInterestPayments(List<CashTransfer> paymentTransfers, CcySwapTrade trade)
			throws TradistaBusinessException {
		List<Transfer> transfers = new ArrayList<Transfer>();
		if (paymentTransfers == null) {
			paymentTransfers = IRSwapTransferUtil.generatePaymentCashTransfers(trade);
		}
		for (Transfer transfer : paymentTransfers) {
			if (transfer.getPurpose().equals(TransferPurpose.FIXED_LEG_INTEREST_PAYMENT)) {
				transfers.add(transfer);
			}
		}
		return transfers;
	}

	private List<Transfer> createNewFloatingLegInterestPayments(List<CashTransfer> receptionTransfers,
			CcySwapTrade trade) throws TradistaBusinessException {
		List<Transfer> transfers = new ArrayList<Transfer>();
		if (receptionTransfers == null) {
			receptionTransfers = IRSwapTransferUtil.generateReceptionCashTransfers(trade);
		}
		for (Transfer transfer : receptionTransfers) {
			if (transfer.getPurpose().equals(TransferPurpose.FLOATING_LEG_INTEREST_PAYMENT)) {
				transfers.add(transfer);
			}
		}
		return transfers;
	}

	private List<Transfer> createNewFixedLegNotionalPayments(List<CashTransfer> paymentTransfers, CcySwapTrade trade)
			throws TradistaBusinessException {
		List<Transfer> transfers = new ArrayList<Transfer>();
		if (paymentTransfers == null) {
			paymentTransfers = IRSwapTransferUtil.generatePaymentCashTransfers(trade);
		}
		for (Transfer transfer : paymentTransfers) {
			if (transfer.getPurpose().equals(TransferPurpose.FIXED_LEG_NOTIONAL_PAYMENT)
					|| transfer.getPurpose().equals(TransferPurpose.FIXED_LEG_NOTIONAL_REPAYMENT)) {
				transfers.add(transfer);
			}
		}
		return transfers;
	}

	private List<Transfer> createNewFloatingLegNotionalPayments(List<CashTransfer> receptionTransfers,
			CcySwapTrade trade) throws TradistaBusinessException {
		List<Transfer> transfers = new ArrayList<Transfer>();
		if (receptionTransfers == null) {
			receptionTransfers = IRSwapTransferUtil.generateReceptionCashTransfers(trade);
		}
		for (Transfer transfer : receptionTransfers) {
			if (transfer.getPurpose().equals(TransferPurpose.FLOATING_LEG_NOTIONAL_PAYMENT)
					|| transfer.getPurpose().equals(TransferPurpose.FLOATING_LEG_NOTIONAL_REPAYMENT)) {
				transfers.add(transfer);
			}
		}
		return transfers;
	}

	@Override
	public void fixCashTransfer(CashTransfer transfer, long quoteSetId) throws TradistaBusinessException {
		CcySwapTrade trade = (CcySwapTrade) transfer.getTrade();
		Index index;
		Tenor indexTenor;
		BigDecimal notional;
		if (transfer.getPurpose().equals(TransferPurpose.FIXED_LEG_INTEREST_PAYMENT)) {
			index = trade.getPaymentReferenceRateIndex();
			indexTenor = trade.getPaymentReferenceRateIndexTenor();
			notional = trade.getNotionalAmountTwo();
		} else {
			index = trade.getReceptionReferenceRateIndex();
			indexTenor = trade.getReceptionReferenceRateIndexTenor();
			notional = trade.getAmount();
		}
		String quoteName = Index.INDEX + "." + index + "." + indexTenor;
		BigDecimal ir = PricerUtil.getValueAsOfDateFromQuote(quoteName, quoteSetId, QuoteType.INTEREST_RATE,
				QuoteValue.CLOSE, transfer.getFixingDateTime().toLocalDate());
		if (ir == null) {
			FixingError fixingError = new FixingError();
			fixingError.setCashTransfer(transfer);
			fixingError.setErrorDate(LocalDateTime.now());
			String errorMsg = String.format(
					"Transfer %d cannot be fixed. Impossible to get the %s index value (CLOSE) as of %tD in QuoteSet %d.",
					transfer.getId(), quoteName, transfer.getFixingDateTime(), quoteSetId);
			fixingError.setMessage(errorMsg);
			fixingError.setStatus(org.eclipse.tradista.core.error.model.Error.Status.UNSOLVED);
			List<FixingError> errors = new ArrayList<>(1);
			errors.add(fixingError);
			fixingErrorBusinessDelegate.saveFixingErrors(errors);
			throw new TradistaBusinessException(errorMsg);
		}
		BigDecimal fractionedNotional = notional.multiply(
				PricerUtil.daysToYear(trade.getPaymentDayCountConvention(), transfer.getFixingDateTime().toLocalDate(),
						DateUtil.addTenor(transfer.getFixingDateTime().toLocalDate(), indexTenor)));
		BigDecimal amount = fractionedNotional.multiply(ir.divide(BigDecimal.valueOf(100),
				configurationBusinessDelegate.getScale(), configurationBusinessDelegate.getRoundingMode()));
		if (amount.signum() == 0) {
			// No transfer
			transferBusinessDelegate.deleteTransfer(transfer.getId());
			return;
			// TODO add a warn somewhere ?
		}
		Transfer.Direction direction;
		if (trade.isBuy()) {
			if (transfer.getPurpose().equals(TransferPurpose.FIXED_LEG_INTEREST_PAYMENT)) {
				if (amount.signum() > 0) {
					direction = Transfer.Direction.RECEIVE;
				} else {
					direction = Transfer.Direction.PAY;
					amount = amount.negate();
				}
			} else {
				if (amount.signum() > 0) {
					direction = Transfer.Direction.RECEIVE;
				} else {
					direction = Transfer.Direction.PAY;
					amount = amount.negate();
				}
			}
		} else {
			if (transfer.getPurpose().equals(TransferPurpose.FIXED_LEG_INTEREST_PAYMENT)) {
				if (amount.signum() > 0) {
					direction = Transfer.Direction.PAY;
				} else {
					direction = Transfer.Direction.RECEIVE;
					amount = amount.negate();
				}
			} else {
				if (amount.signum() > 0) {
					direction = Transfer.Direction.PAY;
				} else {
					direction = Transfer.Direction.RECEIVE;
					amount = amount.negate();
				}
			}
		}
		transfer.setDirection(direction);
		transfer.setAmount(amount);
		transferBusinessDelegate.saveTransfer(transfer);
	}

}