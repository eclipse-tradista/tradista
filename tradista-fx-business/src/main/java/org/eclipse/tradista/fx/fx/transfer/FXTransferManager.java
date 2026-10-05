package org.eclipse.tradista.fx.fx.transfer;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.TransferManager;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.eclipse.tradista.fx.fx.messaging.FXTradeEvent;
import org.eclipse.tradista.fx.fx.model.FXTrade;

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

public class FXTransferManager implements TransferManager<FXTradeEvent> {

	protected TransferBusinessDelegate transferBusinessDelegate;

	public FXTransferManager() {
		transferBusinessDelegate = new TransferBusinessDelegate();
	}

	@Override
	public void createTransfers(FXTradeEvent event) throws TradistaBusinessException {

		FXTrade trade = event.getTrade();
		List<Transfer> transfersToBeSaved = new ArrayList<Transfer>();

		if (event.getOldTrade() != null) {
			FXTrade oldTrade = event.getOldTrade();
			if (!oldTrade.getCurrencyOne().equals(trade.getCurrencyOne())
					|| oldTrade.getAmountOne().compareTo(trade.getAmountOne()) != 0
					|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())
					|| (oldTrade.isBuy() != trade.isBuy()) || (!oldTrade.getBook().equals(trade.getBook()))) {
				List<Transfer> transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
						TransferPurpose.PRIMARY_CURRENCY, false);
				// if the transfer is null, it is not normal, but the process should
				// continue.
				if (transfers == null || transfers.isEmpty()) {
					// TODO logs + Errors viewable in the error report ?
				} else {
					transfers.get(0).setStatus(Transfer.Status.CANCELED);
					transfersToBeSaved.add(transfers.get(0));
				}
				transfersToBeSaved.add(createNewPrimaryCurrencyTransfer(trade));

			}
			if (!oldTrade.getCurrency().equals(trade.getCurrency())
					|| oldTrade.getAmount().compareTo(trade.getAmount()) != 0
					|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())
					|| (oldTrade.isBuy() != trade.isBuy()) || (!oldTrade.getBook().equals(trade.getBook()))) {
				List<Transfer> transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
						TransferPurpose.QUOTE_CURRENCY, false);
				// if the transfer is null, it is not normal, but the process should
				// continue.
				if (transfers == null || transfers.isEmpty()) {
					// TODO logs + Errors viewable in the error report ?
				} else {
					transfers.get(0).setStatus(Transfer.Status.CANCELED);
					transfersToBeSaved.add(transfers.get(0));
				}
				transfersToBeSaved.add(createNewQuoteCurrencyTransfer(trade));

			}
		} else {
			transfersToBeSaved.add(createNewPrimaryCurrencyTransfer(trade));
			transfersToBeSaved.add(createNewQuoteCurrencyTransfer(trade));
		}
		if (!transfersToBeSaved.isEmpty()) {
			transferBusinessDelegate.saveTransfers(transfersToBeSaved);
		}
	}

	private CashTransfer createNewPrimaryCurrencyTransfer(FXTrade trade) throws TradistaBusinessException {
		CashTransfer primaryTransfer = CashTransfer
				.builder(trade.getBook(), TransferPurpose.PRIMARY_CURRENCY, trade.getSettlementDate(),
						trade.getCurrencyOne())
				.trade(trade)
				.fixingDateTime(LocalDate.ofInstant(trade.getCreationTime(), ZoneId.systemDefault()).atStartOfDay())
				.status(Transfer.Status.KNOWN).quantityOrAmount(trade.getAmountOne())
				.direction(trade.isBuy() ? Transfer.Direction.RECEIVE : Transfer.Direction.PAY).build();

		return primaryTransfer;
	}

	private CashTransfer createNewQuoteCurrencyTransfer(FXTrade trade) throws TradistaBusinessException {
		CashTransfer quoteTransfer = CashTransfer
				.builder(trade.getBook(), TransferPurpose.QUOTE_CURRENCY, trade.getSettlementDate(),
						trade.getCurrency())
				.trade(trade)
				.fixingDateTime(LocalDate.ofInstant(trade.getCreationTime(), ZoneId.systemDefault()).atStartOfDay())
				.status(Transfer.Status.KNOWN).quantityOrAmount(trade.getAmount())
				.direction(trade.isBuy() ? Transfer.Direction.PAY : Transfer.Direction.RECEIVE).build();

		return quoteTransfer;
	}

	@Override
	public void fixCashTransfer(CashTransfer transfer, long quoteSetId) {
	}
}