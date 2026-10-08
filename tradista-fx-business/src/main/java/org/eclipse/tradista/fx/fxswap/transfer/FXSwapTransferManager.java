package org.eclipse.tradista.fx.fxswap.transfer;

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
import org.eclipse.tradista.fx.fxswap.messaging.FXSwapTradeEvent;
import org.eclipse.tradista.fx.fxswap.model.FXSwapTrade;

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

public class FXSwapTransferManager implements TransferManager<FXSwapTradeEvent> {

	protected TransferBusinessDelegate transferBusinessDelegate;

	public FXSwapTransferManager() {
		transferBusinessDelegate = new TransferBusinessDelegate();
	}

	@Override
	public void createTransfers(FXSwapTradeEvent event) throws TradistaBusinessException {

		FXSwapTrade trade = event.getTrade();
		List<Transfer> transfersToBeSaved = new ArrayList<Transfer>();

		if (event.getOldTrade() != null) {
			FXSwapTrade oldTrade = event.getOldTrade();
			if (!oldTrade.getCurrencyOne().equals(trade.getCurrencyOne())
					|| oldTrade.getAmountOneSpot().compareTo(trade.getAmountOneSpot()) != 0
					|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())
					|| (oldTrade.isBuy() != trade.isBuy()) || (!oldTrade.getBook().equals(trade.getBook()))) {
				List<Transfer> transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
						TransferPurpose.PRIMARY_CURRENCY_SPOT, false);
				// if the transfer is null, it is not normal, but the process should
				// continue.
				if (transfers == null || transfers.isEmpty()) {
					// TODO logs + Errors viewable in the error report ?
				} else {
					transfers.get(0).setStatus(Transfer.Status.CANCELED);
					transfersToBeSaved.add(transfers.get(0));
				}
				transfersToBeSaved.add(createNewPrimaryCurrencySpotTransfer(trade));

			}
			if (!oldTrade.getCurrency().equals(trade.getCurrency())
					|| oldTrade.getAmount().compareTo(trade.getAmount()) != 0
					|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())
					|| (oldTrade.isBuy() != trade.isBuy()) || (!oldTrade.getBook().equals(trade.getBook()))) {
				List<Transfer> transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
						TransferPurpose.QUOTE_CURRENCY_SPOT, false);
				// if the transfer is null, it is not normal, but the process should
				// continue.
				if (transfers == null || transfers.isEmpty()) {
					// TODO logs + Errors viewable in the error report ?
				} else {
					transfers.get(0).setStatus(Transfer.Status.CANCELED);
					transfersToBeSaved.add(transfers.get(0));
				}
				transfersToBeSaved.add(createNewQuoteCurrencySpotTransfer(trade));

			}
			if (!oldTrade.getCurrencyOne().equals(trade.getCurrencyOne())
					|| oldTrade.getAmountOneForward().compareTo(trade.getAmountOneForward()) != 0
					|| !oldTrade.getSettlementDateForward().isEqual(trade.getSettlementDateForward())
					|| (oldTrade.isBuy() != trade.isBuy()) || (!oldTrade.getBook().equals(trade.getBook()))) {
				List<Transfer> transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
						TransferPurpose.PRIMARY_CURRENCY_FORWARD, false);
				// if the transfer is null, it is not normal, but the process should
				// continue.
				if (transfers == null || transfers.isEmpty()) {
					// TODO logs + Errors viewable in the error report ?
				} else {
					transfers.get(0).setStatus(Transfer.Status.CANCELED);
					transfersToBeSaved.add(transfers.get(0));
				}
				transfersToBeSaved.add(createNewPrimaryCurrencyForwardTransfer(trade));

			}
			if (!oldTrade.getCurrency().equals(trade.getCurrency())
					|| oldTrade.getAmountTwoForward().compareTo(trade.getAmountTwoForward()) != 0
					|| !oldTrade.getSettlementDateForward().isEqual(trade.getSettlementDateForward())
					|| (oldTrade.isBuy() != trade.isBuy()) || (!oldTrade.getBook().equals(trade.getBook()))) {
				List<Transfer> transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
						TransferPurpose.QUOTE_CURRENCY_FORWARD, false);
				// if the transfer is null, it is not normal, but the process should
				// continue.
				if (transfers == null || transfers.isEmpty()) {
					// TODO logs + Errors viewable in the error report ?
				} else {
					transfers.get(0).setStatus(Transfer.Status.CANCELED);
					transfersToBeSaved.add(transfers.get(0));
				}
				transfersToBeSaved.add(createNewQuoteCurrencyForwardTransfer(trade));

			}

		} else {
			transfersToBeSaved.add(createNewPrimaryCurrencySpotTransfer(trade));
			transfersToBeSaved.add(createNewQuoteCurrencySpotTransfer(trade));
			transfersToBeSaved.add(createNewPrimaryCurrencyForwardTransfer(trade));
			transfersToBeSaved.add(createNewQuoteCurrencyForwardTransfer(trade));
		}
		if (!transfersToBeSaved.isEmpty()) {
			transferBusinessDelegate.saveTransfers(transfersToBeSaved);
		}
	}

	private CashTransfer createNewPrimaryCurrencySpotTransfer(FXSwapTrade trade) throws TradistaBusinessException {
		CashTransfer primaryTransfer = CashTransfer
				.builder(trade.getBook(), TransferPurpose.PRIMARY_CURRENCY_SPOT, trade.getSettlementDate(),
						trade.getCurrencyOne())
				.trade(trade)
				.fixingDateTime(LocalDate.ofInstant(trade.getCreationTime(), ZoneId.systemDefault()).atStartOfDay())
				.status(Transfer.Status.KNOWN).quantityOrAmount(trade.getAmountOneSpot())
				.direction(trade.isBuy() ? Transfer.Direction.RECEIVE : Transfer.Direction.PAY).build();

		return primaryTransfer;
	}

	private CashTransfer createNewQuoteCurrencySpotTransfer(FXSwapTrade trade) throws TradistaBusinessException {
		CashTransfer quoteTransfer = CashTransfer
				.builder(trade.getBook(), TransferPurpose.QUOTE_CURRENCY_SPOT, trade.getSettlementDate(),
						trade.getCurrency())
				.trade(trade)
				.fixingDateTime(LocalDate.ofInstant(trade.getCreationTime(), ZoneId.systemDefault()).atStartOfDay())
				.status(Transfer.Status.KNOWN).quantityOrAmount(trade.getAmount())
				.direction(trade.isBuy() ? Transfer.Direction.PAY : Transfer.Direction.RECEIVE).build();

		return quoteTransfer;
	}

	private CashTransfer createNewPrimaryCurrencyForwardTransfer(FXSwapTrade trade) throws TradistaBusinessException {
		CashTransfer primaryTransfer = CashTransfer
				.builder(trade.getBook(), TransferPurpose.PRIMARY_CURRENCY_FORWARD, trade.getSettlementDateForward(),
						trade.getCurrencyOne())
				.trade(trade)
				.fixingDateTime(LocalDate.ofInstant(trade.getCreationTime(), ZoneId.systemDefault()).atStartOfDay())
				.status(Transfer.Status.KNOWN).quantityOrAmount(trade.getAmountOneForward())
				.direction(trade.isBuy() ? Transfer.Direction.PAY : Transfer.Direction.RECEIVE).build();

		return primaryTransfer;
	}

	private CashTransfer createNewQuoteCurrencyForwardTransfer(FXSwapTrade trade) throws TradistaBusinessException {
		CashTransfer quoteTransfer = CashTransfer
				.builder(trade.getBook(), TransferPurpose.QUOTE_CURRENCY_FORWARD, trade.getSettlementDateForward(),
						trade.getCurrency())
				.trade(trade)
				.fixingDateTime(LocalDate.ofInstant(trade.getCreationTime(), ZoneId.systemDefault()).atStartOfDay())
				.status(Transfer.Status.KNOWN).quantityOrAmount(trade.getAmountTwoForward())
				.direction(trade.isBuy() ? Transfer.Direction.RECEIVE : Transfer.Direction.PAY).build();

		return quoteTransfer;
	}

	@Override
	public void fixCashTransfer(CashTransfer transfer, long quoteSetId) throws TradistaBusinessException {
	}

}