package org.eclipse.tradista.mm.loandeposit.transfer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.Transfer.Direction;
import org.eclipse.tradista.core.transfer.model.TransferManager;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.FixingErrorBusinessDelegate;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.eclipse.tradista.mm.loandeposit.messaging.LoanDepositTradeEvent;
import org.eclipse.tradista.mm.loandeposit.model.LoanDepositTrade;
import org.eclipse.tradista.mm.loandeposit.model.LoanTrade;
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

public abstract class LoanDepositTransferManager implements TransferManager<LoanDepositTradeEvent> {

	protected TransferBusinessDelegate transferBusinessDelegate;

	protected FixingErrorBusinessDelegate fixingErrorBusinessDelegate;

	protected LoanDepositTradeBusinessDelegate loanDepositBusinessDelegate;

	public LoanDepositTransferManager() {
		transferBusinessDelegate = new TransferBusinessDelegate();
		fixingErrorBusinessDelegate = new FixingErrorBusinessDelegate();
		loanDepositBusinessDelegate = new LoanDepositTradeBusinessDelegate();
	}

	@Override
	public void createTransfers(LoanDepositTradeEvent event) throws TradistaBusinessException {
		LoanDepositTrade trade = event.getTrade();
		List<Transfer> transfersToBeSaved = new ArrayList<Transfer>();

		if (event.getOldTrade() != null) {
			LoanDepositTrade oldTrade = event.getOldTrade();
			if (!oldTrade.getCurrency().equals(trade.getCurrency())
					|| oldTrade.getAmount().compareTo(trade.getAmount()) != 0
					|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())
					|| !oldTrade.getEndDate().isEqual(trade.getEndDate()) || oldTrade.isFixed() != trade.isFixed()
					|| (oldTrade.getFixedRate() != null && trade.getFixedRate() != null
							&& (oldTrade.getFixedRate().compareTo(trade.getFixedRate()) != 0))
					|| !oldTrade.getPaymentFrequency().equals(trade.getPaymentFrequency())
					|| !oldTrade.getInterestPayment().equals(trade.getInterestPayment())
					|| ((oldTrade.getInterestFixing() != null && trade.getInterestFixing() == null)
							|| (oldTrade.getInterestFixing() == null && trade.getInterestFixing() != null)
							|| (!oldTrade.getInterestFixing().equals(trade.getInterestFixing())))
					|| (oldTrade.isBuy() != trade.isBuy()) || (!oldTrade.getBook().equals(trade.getBook()))
					|| (!oldTrade.getProductType().equals(trade.getProductType()))) {
				List<Transfer> transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
						TransferPurpose.INTEREST_PAYMENT, false);
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
				transfersToBeSaved.addAll(createNewInterestPayments(trade));

				if (!oldTrade.getCurrency().equals(trade.getCurrency())
						|| oldTrade.getAmount().compareTo(trade.getAmount()) != 0
						|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())
						|| !oldTrade.getEndDate().isEqual(trade.getEndDate()) || (oldTrade.isBuy() != trade.isBuy())
						|| (!oldTrade.getBook().equals(trade.getBook()))
						|| (!oldTrade.getProductType().equals(trade.getProductType()))) {
					transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
							TransferPurpose.NOTIONAL_PAYMENT, false);
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

					transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
							TransferPurpose.NOTIONAL_REPAYMENT, false);

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

					transfersToBeSaved.add(createNewNotionalPayment(trade));
					transfersToBeSaved.add(createNewNotionalRepayment(trade));
				}

			}

		} else {
			transfersToBeSaved.add(createNewNotionalPayment(trade));
			transfersToBeSaved.add(createNewNotionalRepayment(trade));
			transfersToBeSaved.addAll(createNewInterestPayments(trade));
		}

		if (!transfersToBeSaved.isEmpty()) {
			transferBusinessDelegate.saveTransfers(transfersToBeSaved);
		}

	}

	private List<CashTransfer> createNewInterestPayments(LoanDepositTrade trade) throws TradistaBusinessException {
		return LoanDepositTransferUtil.generateInterestPayments(trade);
	}

	private CashTransfer createNewNotionalPayment(LoanDepositTrade trade) throws TradistaBusinessException {
		CashTransfer transfer = new CashTransfer(trade.getBook(), TransferPurpose.NOTIONAL_PAYMENT,
				trade.getSettlementDate(), trade, trade.getCurrency());
		transfer.setAmount(trade.getAmount());
		transfer.setCreationDateTime(LocalDateTime.now());
		Direction direction;
		if (trade.getProductType().equals(LoanTrade.LOAN)) {
			if (trade.isBuy()) {
				direction = Direction.RECEIVE;
			} else {
				direction = Direction.PAY;
			}
		} else {
			if (trade.isBuy()) {
				direction = Direction.PAY;
			} else {
				direction = Direction.RECEIVE;
			}
		}
		transfer.setDirection(direction);
		transfer.setFixingDateTime(LocalDateTime.now());
		transfer.setStatus(Transfer.Status.KNOWN);
		return transfer;
	}

	private CashTransfer createNewNotionalRepayment(LoanDepositTrade trade) throws TradistaBusinessException {
		CashTransfer transfer = new CashTransfer(trade.getBook(), TransferPurpose.NOTIONAL_REPAYMENT,
				trade.getEndDate(), trade, trade.getCurrency());
		transfer.setAmount(trade.getAmount());
		transfer.setCreationDateTime(LocalDateTime.now());
		Direction direction;
		if (trade.getProductType().equals(LoanTrade.LOAN)) {
			if (trade.isBuy()) {
				direction = Direction.PAY;
			} else {
				direction = Direction.RECEIVE;
			}
		} else {
			if (trade.isBuy()) {
				direction = Direction.RECEIVE;
			} else {
				direction = Direction.PAY;
			}
		}
		transfer.setDirection(direction);
		transfer.setFixingDateTime(LocalDateTime.now());
		transfer.setStatus(Transfer.Status.KNOWN);
		return transfer;

	}

}