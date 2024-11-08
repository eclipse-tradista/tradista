package org.eclipse.tradista.ir.ircapfloorcollar.transfer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.TransferManager;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.FixingErrorBusinessDelegate;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.eclipse.tradista.ir.ircapfloorcollar.messaging.IRCapFloorCollarTradeEvent;
import org.eclipse.tradista.ir.ircapfloorcollar.model.IRCapFloorCollarTrade;

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

public abstract class IRCapFloorCollarTransferManager implements TransferManager<IRCapFloorCollarTradeEvent> {

	protected TransferBusinessDelegate transferBusinessDelegate;

	protected FixingErrorBusinessDelegate fixingErrorBusinessDelegate;

	public IRCapFloorCollarTransferManager() {
		transferBusinessDelegate = new TransferBusinessDelegate();
		fixingErrorBusinessDelegate = new FixingErrorBusinessDelegate();
	}

	@Override
	public void createTransfers(IRCapFloorCollarTradeEvent event) throws TradistaBusinessException {

		IRCapFloorCollarTrade trade = event.getTrade();
		List<Transfer> transfersToBeSaved = new ArrayList<Transfer>();

		if (event.getOldTrade() != null) {
			IRCapFloorCollarTrade oldTrade = event.getOldTrade();
			if (!oldTrade.getCurrency().equals(trade.getCurrency())
					|| !oldTrade.getIrForwardTrade().getFrequency().equals(trade.getIrForwardTrade().getFrequency())
					|| !oldTrade.getIrForwardTrade().getMaturityDate()
							.isEqual(trade.getIrForwardTrade().getMaturityDate())
					|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())
					|| !oldTrade.getIrForwardTrade().getInterestPayment()
							.equals(trade.getIrForwardTrade().getInterestPayment())
					|| !oldTrade.getIrForwardTrade().getInterestFixing()
							.equals(trade.getIrForwardTrade().getInterestFixing())
					|| (!oldTrade.getBook().equals(trade.getBook()))) {
				List<Transfer> transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
						TransferPurpose.CASH_SETTLEMENT, false);
				// if the transfer list is null or empty, it is not normal, but
				// the process should
				// continue.
				if (transfers == null || transfers.isEmpty()) {
					// TODO logs + Errors viewable in the error report ?
				} else {
					for (Transfer transfer : transfers) {
						transfer.setStatus(Transfer.Status.CANCELED);
						transfersToBeSaved.add(transfer);
					}
				}
				List<CashTransfer> cashTransfers = createNewCashSettlementTransfers(trade);
				if (cashTransfers != null) {
					transfersToBeSaved.addAll(cashTransfers);
				}

			}
		} else {
			List<CashTransfer> cashTransfers = createNewCashSettlementTransfers(trade);
			if (cashTransfers != null) {
				transfersToBeSaved.addAll(cashTransfers);
			}
		}
		if (!transfersToBeSaved.isEmpty()) {
			transferBusinessDelegate.saveTransfers(transfersToBeSaved);
		}
	}

	private List<CashTransfer> createNewCashSettlementTransfers(IRCapFloorCollarTrade trade)
			throws TradistaBusinessException {

		List<CashTransfer> transfers = IRCapFloorCollarTransferUtil.generateCashTransfers(trade);

		if (transfers != null && !transfers.isEmpty()) {
			return transfers;
		}
		return null;
	}

}