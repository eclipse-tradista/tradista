package org.eclipse.tradista.ir.fra.transfer;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.FixingError;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.TransferManager;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.FixingErrorBusinessDelegate;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.eclipse.tradista.ir.fra.messaging.FRATradeEvent;
import org.eclipse.tradista.ir.fra.model.FRATrade;

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

public class FRATransferManager implements TransferManager<FRATradeEvent> {

	protected TransferBusinessDelegate transferBusinessDelegate;

	protected FixingErrorBusinessDelegate fixingErrorBusinessDelegate;

	protected ConfigurationBusinessDelegate configurationBusinessDelegate;

	public FRATransferManager() {
		transferBusinessDelegate = new TransferBusinessDelegate();
		fixingErrorBusinessDelegate = new FixingErrorBusinessDelegate();
		configurationBusinessDelegate = new ConfigurationBusinessDelegate();
	}

	@Override
	public void createTransfers(FRATradeEvent event) throws TradistaBusinessException {

		FRATrade trade = event.getTrade();
		List<Transfer> transfersToBeSaved = new ArrayList<Transfer>();

		if (event.getOldTrade() != null) {
			FRATrade oldTrade = event.getOldTrade();
			if (oldTrade.getAmount().compareTo(trade.getAmount()) != 0
					|| !oldTrade.getCurrency().equals(trade.getCurrency())
					|| !oldTrade.getPaymentDate().isEqual(trade.getPaymentDate()) || (oldTrade.isBuy() != trade.isBuy())
					|| (!oldTrade.getBook().equals(trade.getBook()))) {
				List<Transfer> transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
						TransferPurpose.CASH_SETTLEMENT, false);
				// if the transfer list is null or empty, it is not normal, but the process
				// should
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

	private CashTransfer createNewCashSettlementTransfer(FRATrade trade) throws TradistaBusinessException {
		CashTransfer cashTransfer = new CashTransfer(trade.getBook(), TransferPurpose.CASH_SETTLEMENT,
				trade.getPaymentDate(), trade, trade.getCurrency());
		cashTransfer.setCreationDateTime(LocalDateTime.now());
		cashTransfer.setFixingDateTime(
				DateUtil.addBusinessDay(trade.getPaymentDate(), trade.getCurrency().getCalendar(), -2).atStartOfDay());
		cashTransfer.setStatus(Transfer.Status.UNKNOWN);

		return cashTransfer;
	}

	@Override
	public void fixCashTransfer(CashTransfer transfer, long quoteSetId) throws TradistaBusinessException {
		FRATrade trade = (FRATrade) transfer.getTrade();
		BigDecimal fixingRate = null;
		BigDecimal difference = null;
		BigDecimal amount = null;
		// 1. Get the right rate
		// TODO Put the quoteset from the Transfer Configuration as parameter of
		// PricerUtil.getValueAsOfDateFromQuote
		String quoteName = Index.INDEX + "." + trade.getReferenceRateIndex() + "." + trade.getReferenceRateIndexTenor();
		fixingRate = PricerUtil.getValueAsOfDateFromQuote(quoteName, quoteSetId, QuoteType.INTEREST_RATE,
				QuoteValue.CLOSE, transfer.getFixingDateTime().toLocalDate());

		if (fixingRate == null) {
			FixingError fixingError = new FixingError();
			fixingError.setCashTransfer(transfer);
			fixingError.setErrorDate(LocalDateTime.now());
			String errorMsg = String.format(
					"Transfer %d cannot be fixed. Impossible to get the %s quote value (CLOSE) as of %tD in QuoteSet %d.",
					transfer.getId(), quoteName, LocalDate.now(), quoteSetId);
			fixingError.setMessage(errorMsg);
			fixingError.setStatus(org.eclipse.tradista.core.error.model.Error.Status.UNSOLVED);
			List<FixingError> errors = new ArrayList<>(1);
			errors.add(fixingError);
			fixingErrorBusinessDelegate.saveFixingErrors(errors);
			throw new TradistaBusinessException(errorMsg);
		}
		difference = trade.getFixedRate().subtract(fixingRate);
		amount = trade.getAmount()
				.multiply((difference.abs()).divide(new BigDecimal("100"),
						configurationBusinessDelegate.getRoundingMode()))
				.multiply(
						PricerUtil.daysToYear(trade.getDayCountConvention(), trade.getStartDate(), trade.getEndDate()));
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