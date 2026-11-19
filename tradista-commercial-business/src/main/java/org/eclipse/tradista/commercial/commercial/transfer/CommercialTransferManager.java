package org.eclipse.tradista.commercial.commercial.transfer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.commercial.commercial.messaging.CommercialTradeEvent;
import org.eclipse.tradista.commercial.commercial.model.CommercialTrade;
import org.eclipse.tradista.commercial.commercial.model.CommercialTrade.CommercialCashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.Transfer.Direction;
import org.eclipse.tradista.core.transfer.model.TransferManager;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.springframework.util.CollectionUtils;

public class CommercialTransferManager implements TransferManager<CommercialTradeEvent> {

	protected TransferBusinessDelegate transferBusinessDelegate;

	public CommercialTransferManager() {
		transferBusinessDelegate = new TransferBusinessDelegate();
	}

	@Override
	public void createTransfers(CommercialTradeEvent event) throws TradistaBusinessException {
		CommercialTrade trade = event.getTrade();
		List<Transfer> transfersToBeSaved = new ArrayList<>();

		List<Transfer> existingTransfers = null;
		if (event.getOldTrade() != null) {
			existingTransfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(event.getOldTrade().getId(),
					TransferPurpose.CASH_SETTLEMENT, false);
		}

		boolean isCancelled = trade.getStatus() != null && "CANCELLED".equalsIgnoreCase(trade.getStatus().getName());
		List<Transfer> matchedTransfers = new ArrayList<>();

		if (!isCancelled && !CollectionUtils.isEmpty(trade.getItems())) {
			Direction direction = trade.isBuy() ? Direction.PAY : Direction.RECEIVE;
			for (CommercialCashFlow item : trade.getItems()) {
				CashTransfer existing = findMatchingTransfer(existingTransfers, item, matchedTransfers);
				if (existing != null) {
					matchedTransfers.add(existing);
					if (existing.getStatus() != Transfer.Status.SETTLED) {
						existing.setDirection(direction);
						existing.setAmount(item.getAmount());
						existing.setFixingDateTime(LocalDateTime.now());
						if (item.isSettled()) {
							existing.setStatus(Transfer.Status.SETTLED);
						}
						transfersToBeSaved.add(existing);
					}
				} else {
					Transfer.Status status = item.isSettled() ? Transfer.Status.SETTLED : Transfer.Status.KNOWN;
					CashTransfer cashTransfer = CashTransfer
							.builder(trade.getBook(), TransferPurpose.CASH_SETTLEMENT, item.getSettlementDate(),
									trade.getCurrency())
							.trade(trade).direction(direction).fixingDateTime(LocalDateTime.now())
							.quantityOrAmount(item.getAmount()).status(status).build();
					transfersToBeSaved.add(cashTransfer);
				}
			}
		}

		if (existingTransfers != null) {
			for (Transfer transfer : existingTransfers) {
				if (!matchedTransfers.contains(transfer) && transfer.getStatus() != Transfer.Status.SETTLED
						&& transfer.getStatus() != Transfer.Status.CANCELED) {
					transfer.setStatus(Transfer.Status.CANCELED);
					transfersToBeSaved.add(transfer);
				}
			}
		}

		if (!transfersToBeSaved.isEmpty()) {
			transferBusinessDelegate.saveTransfers(transfersToBeSaved);
		}
	}

	private CashTransfer findMatchingTransfer(List<Transfer> existingTransfers, CommercialCashFlow item,
			List<Transfer> matchedTransfers) {
		if (CollectionUtils.isEmpty(existingTransfers) || item.getSettlementDate() == null) {
			return null;
		}
		for (Transfer transfer : existingTransfers) {
			if (transfer instanceof CashTransfer cashTransfer && !matchedTransfers.contains(transfer)
					&& transfer.getStatus() != Transfer.Status.CANCELED
					&& item.getSettlementDate().equals(transfer.getSettlementDate())) {
				return cashTransfer;
			}
		}
		return null;
	}

	@Override
	public void fixCashTransfer(CashTransfer transfer, long quoteSetId) throws TradistaBusinessException {
		// Commercial cash flows have fixed amounts, no market-rate fixing required.
	}

}
