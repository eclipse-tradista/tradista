package org.eclipse.tradista.fx.fxndf.transfer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.FixingError;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.TransferManager;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.FixingErrorBusinessDelegate;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.eclipse.tradista.fx.fxndf.messaging.FXNDFTradeEvent;
import org.eclipse.tradista.fx.fxndf.model.FXNDFTrade;
import org.eclipse.tradista.fx.fxndf.service.FXNDFTradeBusinessDelegate;

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

public class FXNDFTransferManager implements TransferManager<FXNDFTradeEvent> {

	protected TransferBusinessDelegate transferBusinessDelegate;
	protected FixingErrorBusinessDelegate fixingErrorBusinessDelegate;
	protected FXNDFTradeBusinessDelegate fxNdfTradeBusinessDelegate;

	public FXNDFTransferManager() {
		transferBusinessDelegate = new TransferBusinessDelegate();
		fxNdfTradeBusinessDelegate = new FXNDFTradeBusinessDelegate();
		fixingErrorBusinessDelegate = new FixingErrorBusinessDelegate();
	}

	@Override
	public void createTransfers(FXNDFTradeEvent event) throws TradistaBusinessException {

		FXNDFTrade trade = event.getTrade();
		List<Transfer> transfersToBeSaved = new ArrayList<Transfer>();

		if (event.getOldTrade() != null) {
			FXNDFTrade oldTrade = event.getOldTrade();
			if (!oldTrade.getCurrency().equals(trade.getCurrency())
					|| !fxNdfTradeBusinessDelegate.getFixingDate(oldTrade)
							.equals(fxNdfTradeBusinessDelegate.getFixingDate(trade))
					|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())
					|| (oldTrade.isBuy() != trade.isBuy()) || (!oldTrade.getBook().equals(trade.getBook()))) {
				List<Transfer> transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
						TransferPurpose.CASH_SETTLEMENT, false);
				// if the transfer is null, it is not normal, but the process should
				// continue.
				if (transfers == null || transfers.isEmpty()) {
					// TODO logs + Errors viewable in the error report ?
				} else {
					transfers.get(0).setStatus(Transfer.Status.CANCELED);
					transfersToBeSaved.add(transfers.get(0));
				}
				transfersToBeSaved.add(createNewCashSettlementTransfer(trade));
			}
		} else {
			transfersToBeSaved.add(createNewCashSettlementTransfer(trade));
		}
		if (!transfersToBeSaved.isEmpty()) {
			transferBusinessDelegate.saveTransfers(transfersToBeSaved);
		}
	}

	private CashTransfer createNewCashSettlementTransfer(FXNDFTrade trade) throws TradistaBusinessException {
		CashTransfer cashSettlementTransfer = new CashTransfer(trade.getBook(), TransferPurpose.CASH_SETTLEMENT,
				trade.getSettlementDate(), trade, trade.getCurrency());
		cashSettlementTransfer.setCreationDateTime(LocalDateTime.now());
		try {
			cashSettlementTransfer.setFixingDateTime(fxNdfTradeBusinessDelegate.getFixingDate(trade).atStartOfDay());
		} catch (TradistaBusinessException abe) {
			// Should not happen here.
		}
		cashSettlementTransfer.setStatus(Transfer.Status.UNKNOWN);

		return cashSettlementTransfer;
	}

	@Override
	public void fixCashTransfer(CashTransfer transfer, long quoteSetId) throws TradistaBusinessException {
		FXNDFTrade trade = (FXNDFTrade) transfer.getTrade();
		BigDecimal fixingRate = null;
		BigDecimal difference = null;
		BigDecimal amount = null;
		// 1. Get the right rate
		// TODO Put the quoteset from the Transfer Configuration as parameter of
		// PricerUtil.getFXClosingRate

		fixingRate = PricerUtil.getFXClosingRate(trade.getNonDeliverableCurrency(), trade.getCurrency(),
				LocalDate.now(), quoteSetId);

		if (fixingRate == null) {
			FixingError fixingError = new FixingError();
			fixingError.setCashTransfer(transfer);
			fixingError.setErrorDate(LocalDateTime.now());
			String errorMsg = String.format(
					"Transfer %d cannot be fixed. Impossible to get the %s/%s FX closing rate as of %tD in QuoteSet %d.",
					transfer.getId(), trade.getNonDeliverableCurrency(), trade.getCurrency(), LocalDate.now(),
					quoteSetId);
			fixingError.setErrorMessage(errorMsg);
			fixingError.setStatus(org.eclipse.tradista.core.error.model.Error.Status.UNSOLVED);
			List<FixingError> errors = new ArrayList<>(1);
			errors.add(fixingError);
			fixingErrorBusinessDelegate.saveFixingErrors(errors);
			throw new TradistaBusinessException(errorMsg);
		}
		difference = trade.getNdfRate().subtract(fixingRate);
		amount = trade.getAmount().multiply(difference.abs());
		if (amount.signum() == 0) {
			// No transfer
			transferBusinessDelegate.deleteTransfer(transfer.getId());
			return;
			// TODO add a warn somewhere ?
		}
		transfer.setAmount(amount);
		if (trade.isBuy()) {
			if (difference.signum() < 0) {
				transfer.setDirection(Transfer.Direction.RECEIVE);
			} else {
				transfer.setDirection(Transfer.Direction.PAY);
			}
		} else {
			if (difference.signum() < 0) {
				transfer.setDirection(Transfer.Direction.PAY);
			} else {
				transfer.setDirection(Transfer.Direction.RECEIVE);
			}
		}
		transfer.setStatus(Transfer.Status.KNOWN);
		transferBusinessDelegate.saveTransfer(transfer);
	}

}