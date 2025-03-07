package org.eclipse.tradista.ir.future.transfer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.productinventory.service.ProductInventoryBusinessDelegate;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.FixingError;
import org.eclipse.tradista.core.transfer.model.ProductTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.TransferManager;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.FixingErrorBusinessDelegate;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.eclipse.tradista.ir.future.messaging.FutureTradeEvent;
import org.eclipse.tradista.ir.future.model.Future;
import org.eclipse.tradista.ir.future.model.FutureTrade;

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

public class FutureTransferManager implements TransferManager<FutureTradeEvent> {

	protected TransferBusinessDelegate transferBusinessDelegate;

	protected FixingErrorBusinessDelegate fixingErrorBusinessDelegate;

	protected ProductInventoryBusinessDelegate productInventoryBusinessDelegate;

	public FutureTransferManager() {
		transferBusinessDelegate = new TransferBusinessDelegate();
		fixingErrorBusinessDelegate = new FixingErrorBusinessDelegate();
		productInventoryBusinessDelegate = new ProductInventoryBusinessDelegate();
	}

	@Override
	public void createTransfers(FutureTradeEvent event) throws TradistaBusinessException {

		FutureTrade trade = event.getTrade();
		List<Transfer> transfersToBeSaved = new ArrayList<>();

		// Get the cash transfers currently planned to be received for this
		// future
		List<CashTransfer> existingTransfers = transferBusinessDelegate
				.getCashTransfersByProductIdAndStartDate(trade.getProduct().getId(), trade.getSettlementDate());

		if (event.getOldTrade() != null) {
			FutureTrade oldTrade = event.getOldTrade();

			if (!oldTrade.getProduct().equals(trade.getProduct()) || !oldTrade.getCurrency().equals(trade.getCurrency())
					|| !oldTrade.getMaturityDate().isEqual(trade.getMaturityDate())
					|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())
					|| (oldTrade.isBuy() != trade.isBuy()) || (!oldTrade.getBook().equals(trade.getBook()))) {
				CashTransfer cashTransfer = createCashSettlement(existingTransfers, trade);
				if (cashTransfer != null) {
					transfersToBeSaved.add(cashTransfer);
				}
			}

			if (!oldTrade.getProduct().equals(trade.getProduct())
					|| oldTrade.getQuantity().compareTo(trade.getQuantity()) != 0
					|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())
					|| (oldTrade.isBuy() != trade.isBuy()) || (!oldTrade.getBook().equals(trade.getBook()))) {
				List<Transfer> transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
						TransferPurpose.FUTURE_SETTLEMENT, false);
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
				transfersToBeSaved.add(createNewFutureSettlement(trade));
			}

		} else {
			transfersToBeSaved.add(createNewFutureSettlement(trade));
			CashTransfer cashTransfer = createCashSettlement(existingTransfers, trade);
			if (cashTransfer != null) {
				transfersToBeSaved.add(cashTransfer);
			}
		}
		if (!transfersToBeSaved.isEmpty()) {
			transferBusinessDelegate.saveTransfers(transfersToBeSaved);
		}
	}

	private ProductTransfer createNewFutureSettlement(FutureTrade trade) {

		ProductTransfer futureSettlement = new ProductTransfer(trade.getBook(), TransferPurpose.FUTURE_SETTLEMENT,
				trade.getSettlementDate(), trade);
		futureSettlement.setCreationDateTime(LocalDateTime.now());
		if (trade.isBuy()) {
			futureSettlement.setDirection(Transfer.Direction.RECEIVE);
		} else {
			futureSettlement.setDirection(Transfer.Direction.PAY);
		}
		futureSettlement.setFixingDateTime(trade.getCreationDate().atStartOfDay());
		futureSettlement.setQuantity(trade.getQuantity());
		futureSettlement.setStatus(Transfer.Status.KNOWN);

		return futureSettlement;
	}

	private CashTransfer createCashSettlement(List<CashTransfer> existingTransfers, FutureTrade trade) {

		CashTransfer cashSettlement = new CashTransfer(trade.getBook(), trade.getProduct(),
				TransferPurpose.CASH_SETTLEMENT, trade.getSettlementDate(), trade.getCurrency());
		cashSettlement.setCreationDateTime(LocalDateTime.now());
		cashSettlement.setFixingDateTime(trade.getMaturityDate().atStartOfDay());
		cashSettlement.setStatus(Transfer.Status.UNKNOWN);
		boolean exists = false;
		if (existingTransfers != null) {
			for (CashTransfer existingTransfer : existingTransfers) {
				if (existingTransfer.getSettlementDate().isEqual(trade.getSettlementDate())
						&& existingTransfer.getPurpose().equals(TransferPurpose.CASH_SETTLEMENT)
						&& !existingTransfer.getStatus().equals(Transfer.Status.CANCELED)) {
					exists = true;
					break;
				}
			}
		}
		if (!exists && trade.isBuy()) {
			return cashSettlement;
		}
		return null;
	}

	/**
	 * Fixes the cash transfer (the final payment when the related contract
	 * matures). We calculate the average price and compare it to the fixing rate,
	 * so we can assess if a payment should be made or received
	 * 
	 * @param transfer   the cash transfer to fix
	 * @param quoteSetId the quote set id used to retrieve the index to determine
	 *                   the fixing rate
	 * @throws TradistaBusinessException when: - the fixing rate could not be found
	 *                                   - the quantity or the average price from
	 *                                   the inventory could not be found for this
	 *                                   book - the transfer could not be deleted or
	 *                                   could not be updated.
	 */
	@Override
	public void fixCashTransfer(CashTransfer transfer, long quoteSetId) throws TradistaBusinessException {
		Future future = (Future) transfer.getProduct();
		BigDecimal fixingRate = null;
		BigDecimal difference = null;
		BigDecimal amount = null;
		// 1. Get the right rate
		// TODO Put the quoteset from the Transfer Configuration as parameter of
		// PricerUtil.getValueAsOfDateFromQuote
		String quoteName = Index.INDEX + "." + future.getReferenceRateIndex() + "."
				+ future.getReferenceRateIndexTenor();
		fixingRate = PricerUtil.getValueAsOfDateFromQuote(quoteName, quoteSetId, QuoteType.INTEREST_RATE,
				QuoteValue.CLOSE, transfer.getFixingDateTime().toLocalDate());

		if (fixingRate == null) {
			FixingError fixingError = new FixingError();
			fixingError.setCashTransfer(transfer);
			fixingError.setErrorDate(LocalDateTime.now());
			String errorMsg = String.format(
					"Transfer %d cannot be fixed. Impossible to get the %s index value (CLOSE) as of %tD in QuoteSet %d.",
					transfer.getId(), quoteName, LocalDate.now(), quoteSetId);
			fixingError.setMessage(errorMsg);
			fixingError.setStatus(org.eclipse.tradista.core.error.model.Error.Status.UNSOLVED);
			List<FixingError> errors = new ArrayList<>(1);
			errors.add(fixingError);
			fixingErrorBusinessDelegate.saveFixingErrors(errors);
			throw new TradistaBusinessException(errorMsg);
		}
		BigDecimal averagePrice = productInventoryBusinessDelegate.getAveragePriceByDateProductAndBookIds(
				future.getId(), transfer.getBook().getId(), transfer.getFixingDateTime().toLocalDate());
		BigDecimal quantity = productInventoryBusinessDelegate.getQuantityByDateProductAndBookIds(future.getId(),
				transfer.getBook().getId(), transfer.getFixingDateTime().toLocalDate());

		BigDecimal settlementPrice = new BigDecimal("100").subtract(fixingRate);
		difference = averagePrice.subtract(settlementPrice);
		amount = difference.abs().multiply(new BigDecimal("100"))
				.multiply(future.getContractSpecification().getPriceVariationByBasisPoint()).multiply(quantity);
		if (amount.signum() == 0) {
			// No transfer
			transferBusinessDelegate.deleteTransfer(transfer.getId());
			return;
			// TODO add a warn somewhere ?
		}
		transfer.setAmount(amount);

		if (difference.signum() < 0) {
			transfer.setDirection(Transfer.Direction.RECEIVE);
		} else {
			transfer.setDirection(Transfer.Direction.PAY);
		}

		transfer.setStatus(Transfer.Status.KNOWN);
		transferBusinessDelegate.saveTransfer(transfer);
	}

}