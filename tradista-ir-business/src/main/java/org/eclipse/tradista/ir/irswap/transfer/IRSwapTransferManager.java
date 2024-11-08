package org.eclipse.tradista.ir.irswap.transfer;

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
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.FixingError;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.Transfer.Status;
import org.eclipse.tradista.core.transfer.model.TransferManager;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.FixingErrorBusinessDelegate;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.eclipse.tradista.ir.irswap.messaging.IRSwapTradeEvent;
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

public class IRSwapTransferManager implements TransferManager<IRSwapTradeEvent> {

	protected TransferBusinessDelegate transferBusinessDelegate;

	protected FixingErrorBusinessDelegate fixingErrorBusinessDelegate;

	public IRSwapTransferManager() {
		transferBusinessDelegate = new TransferBusinessDelegate();
		fixingErrorBusinessDelegate = new FixingErrorBusinessDelegate();
	}

	@Override
	public void createTransfers(IRSwapTradeEvent event) throws TradistaBusinessException {
		IRSwapTrade trade = event.getTrade();
		List<Transfer> transfersToBeSaved = new ArrayList<Transfer>();

		if (event.getOldTrade() != null) {
			IRSwapTrade oldTrade = event.getOldTrade();
			if (!oldTrade.getCurrency().equals(trade.getCurrency())
					|| oldTrade.getAmount().compareTo(trade.getAmount()) != 0
					|| ((oldTrade.getSettlementDate() != null && trade.getSettlementDate() == null)
							|| (oldTrade.getSettlementDate() == null && trade.getSettlementDate() != null)
							|| (!oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())))
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
				if (trade.getSettlementDate() != null) {
					transfersToBeSaved.addAll(createNewFixedLegInterestPayments(trade));
				}

			}
			if (!oldTrade.getCurrency().equals(trade.getCurrency())
					|| oldTrade.getAmount().compareTo(trade.getAmount()) != 0
					|| ((oldTrade.getSettlementDate() != null && trade.getSettlementDate() == null)
							|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate()))
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
				if (trade.getSettlementDate() != null) {
					transfersToBeSaved.addAll(createNewFloatingLegInterestPayments(trade));
				}
			}
		} else {
			transfersToBeSaved.addAll(createNewFixedLegInterestPayments(trade));
			transfersToBeSaved.addAll(createNewFloatingLegInterestPayments(trade));
		}
		if (!transfersToBeSaved.isEmpty()) {
			transferBusinessDelegate.saveTransfers(transfersToBeSaved);
		}

	}

	private List<CashTransfer> createNewFixedLegInterestPayments(IRSwapTrade trade) throws TradistaBusinessException {

		List<CashTransfer> paymentTransfers = IRSwapTransferUtil.generatePaymentCashTransfers(trade);
		List<CashTransfer> fixedLegPayments = new ArrayList<CashTransfer>();
		if (paymentTransfers != null && !paymentTransfers.isEmpty()) {
			for (Transfer transfer : paymentTransfers) {
				if (transfer.getPurpose().equals(TransferPurpose.FIXED_LEG_INTEREST_PAYMENT))
					fixedLegPayments.add((CashTransfer) transfer);
			}
		}

		return fixedLegPayments;
	}

	private List<CashTransfer> createNewFloatingLegInterestPayments(IRSwapTrade trade)
			throws TradistaBusinessException {

		List<CashTransfer> receptionTransfers = IRSwapTransferUtil.generateReceptionCashTransfers(trade);
		List<CashTransfer> floatingLegPayments = new ArrayList<CashTransfer>();
		if (receptionTransfers != null && !receptionTransfers.isEmpty()) {
			for (Transfer transfer : receptionTransfers) {
				if (transfer.getPurpose().equals(TransferPurpose.FLOATING_LEG_INTEREST_PAYMENT))
					floatingLegPayments.add((CashTransfer) transfer);
			}
		}

		return floatingLegPayments;
	}

	@Override
	public void fixCashTransfer(CashTransfer transfer, long quoteSetId) throws TradistaBusinessException {
		IRSwapTrade trade = (IRSwapTrade) transfer.getTrade();
		Index index;
		Tenor indexTenor;
		BigDecimal notional = trade.getAmount();
		BigDecimal fractionedNotional;
		BigDecimal amount;
		ConfigurationBusinessDelegate cbs = new ConfigurationBusinessDelegate();
		if (transfer.getPurpose().equals(TransferPurpose.FIXED_LEG_INTEREST_PAYMENT)) {
			index = trade.getPaymentReferenceRateIndex();
			indexTenor = trade.getPaymentReferenceRateIndexTenor();
		} else {
			index = trade.getReceptionReferenceRateIndex();
			indexTenor = trade.getReceptionReferenceRateIndexTenor();
		}
		String quoteName = Index.INDEX + "." + index + "." + indexTenor;
		BigDecimal ir = PricerUtil.getValueAsOfDateFromQuote(quoteName, quoteSetId, QuoteType.INTEREST_RATE,
				QuoteValue.CLOSE, transfer.getFixingDateTime().toLocalDate());
		if (ir == null) {
			FixingError fixingError = new FixingError();
			fixingError.setCashTransfer(transfer);
			fixingError.setErrorDate(LocalDateTime.now());
			String errorMsg = String.format(
					"Transfer %d cannot be fixed. Impossible to get the %s index closing value as of %tD in quote set %s.",
					transfer.getId(), quoteName, transfer.getFixingDateTime(),
					new QuoteBusinessDelegate().getQuoteSetById(quoteSetId));
			fixingError.setMessage(errorMsg);
			fixingError.setStatus(org.eclipse.tradista.core.error.model.Error.Status.UNSOLVED);
			List<FixingError> errors = new ArrayList<FixingError>(1);
			errors.add(fixingError);
			fixingErrorBusinessDelegate.saveFixingErrors(errors);
			throw new TradistaBusinessException(errorMsg);
		}
		fractionedNotional = notional.multiply(
				PricerUtil.daysToYear(trade.getPaymentDayCountConvention(), transfer.getFixingDateTime().toLocalDate(),
						DateUtil.addTenor(transfer.getFixingDateTime().toLocalDate(), indexTenor)));
		amount = fractionedNotional.multiply(ir.divide(BigDecimal.valueOf(100), cbs.getScale(), cbs.getRoundingMode()));
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
					direction = Transfer.Direction.PAY;
				} else {
					direction = Transfer.Direction.RECEIVE;
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
					direction = Transfer.Direction.RECEIVE;
				} else {
					direction = Transfer.Direction.PAY;
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
		transfer.setStatus(Status.KNOWN);
		transferBusinessDelegate.saveTransfer(transfer);
	}

}