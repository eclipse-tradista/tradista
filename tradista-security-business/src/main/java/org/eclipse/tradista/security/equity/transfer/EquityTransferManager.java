package org.eclipse.tradista.security.equity.transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
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
import org.eclipse.tradista.security.equity.messaging.EquityTradeEvent;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.model.EquityTrade;

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

public class EquityTransferManager implements TransferManager<EquityTradeEvent> {

	protected TransferBusinessDelegate transferBusinessDelegate;

	protected FixingErrorBusinessDelegate fixingErrorBusinessDelegate;

	protected ConfigurationBusinessDelegate configurationBusinessDelegate;

	protected ProductInventoryBusinessDelegate productInventoryBusinessDelegate;

	public EquityTransferManager() {
		transferBusinessDelegate = new TransferBusinessDelegate();
		fixingErrorBusinessDelegate = new FixingErrorBusinessDelegate();
		configurationBusinessDelegate = new ConfigurationBusinessDelegate();
		productInventoryBusinessDelegate = new ProductInventoryBusinessDelegate();
	}

	@Override
	public void createTransfers(EquityTradeEvent event) throws TradistaBusinessException {
		EquityTrade trade = event.getTrade();
		List<Transfer> transfersToBeSaved = new ArrayList<>();

		List<CashTransfer> cashTransfers = null;
		if (event.getOldTrade() != null) {
			EquityTrade oldTrade = event.getOldTrade();
			if (trade.getProduct().isPayDividend()) {
				if (!oldTrade.getProduct().equals(trade.getProduct())
						|| !oldTrade.getCurrency().equals(trade.getCurrency())
						|| oldTrade.getQuantity().compareTo(trade.getQuantity()) != 0
						|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())
						|| !oldTrade.getProduct().getActiveTo().isEqual(trade.getProduct().getActiveTo())
						|| !oldTrade.getProduct().getActiveFrom().isEqual(trade.getProduct().getActiveFrom())
						|| oldTrade.getProduct().getDividendCurrency().equals(trade.getProduct().getDividendCurrency())
						|| !oldTrade.getProduct().getDividendFrequency()
								.equals(trade.getProduct().getDividendFrequency())
						|| (oldTrade.isBuy() != trade.isBuy()) || (!oldTrade.getBook().equals(trade.getBook()))) {
					transfersToBeSaved.addAll(createNewDividends(cashTransfers, trade));
				}
			}

			if (!oldTrade.getProduct().equals(trade.getProduct()) || !oldTrade.getCurrency().equals(trade.getCurrency())
					|| oldTrade.getQuantity().compareTo(trade.getQuantity()) != 0
					|| oldTrade.getAmount().compareTo(trade.getAmount()) != 0
					|| ((oldTrade.getSettlementDate() != null && trade.getSettlementDate() == null)
							|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate()))
					|| !oldTrade.getProduct().getIssueDate().isEqual(trade.getProduct().getIssueDate())
					|| (oldTrade.isBuy() != trade.isBuy()) || (!oldTrade.getBook().equals(trade.getBook()))) {
				List<Transfer> transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
						TransferPurpose.EQUITY_PAYMENT, false);
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
					transfersToBeSaved.add(createNewEquityPayment(trade));
				}

			}

			if (!oldTrade.getProduct().equals(trade.getProduct())
					|| oldTrade.getQuantity().compareTo(trade.getQuantity()) != 0
					|| ((oldTrade.getSettlementDate() != null && trade.getSettlementDate() == null)
							|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate()))
					|| (oldTrade.isBuy() != trade.isBuy()) || (!oldTrade.getBook().equals(trade.getBook()))) {
				List<Transfer> transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
						TransferPurpose.EQUITY_SETTLEMENT, false);
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
					transfersToBeSaved.add(createNewEquitySettlement(trade));
				}

			}

		} else {
			transfersToBeSaved.add(createNewEquitySettlement(trade));
			transfersToBeSaved.add(createNewEquityPayment(trade));
			if (trade.getProduct().isPayDividend()) {
				transfersToBeSaved.addAll(createNewDividends(cashTransfers, trade));
			}
		}

		if (!transfersToBeSaved.isEmpty()) {
			transferBusinessDelegate.saveTransfers(transfersToBeSaved);
		}

	}

	private ProductTransfer createNewEquitySettlement(EquityTrade trade) {
		ProductTransfer productTransfer = new ProductTransfer(trade.getBook(), TransferPurpose.EQUITY_SETTLEMENT,
				trade.getSettlementDate(), trade);
		productTransfer.setCreationDateTime(LocalDateTime.now());
		if (trade.isBuy()) {
			productTransfer.setDirection(Transfer.Direction.RECEIVE);
		} else {
			productTransfer.setDirection(Transfer.Direction.PAY);
		}
		productTransfer.setQuantity(trade.getQuantity());
		productTransfer.setFixingDateTime(trade.getCreationDate().atStartOfDay());
		productTransfer.setStatus(Transfer.Status.KNOWN);

		return productTransfer;
	}

	private List<CashTransfer> createNewDividends(List<CashTransfer> cashTransfers, EquityTrade trade)
			throws TradistaBusinessException {

		List<Transfer> existingDividends = transferBusinessDelegate.getTransfers(Transfer.Type.CASH, null, null,
				TransferPurpose.DIVIDEND, 0, trade.getProductId(), trade.getBook().getId(),
				trade.getProduct().getDividendCurrency().getId(), null, null, trade.getSettlementDate(), null, null,
				null);
		if (existingDividends != null) {
			existingDividends = existingDividends.stream().filter(t -> !t.getStatus().equals(Transfer.Status.CANCELED))
					.toList();
		}

		if (cashTransfers == null) {
			cashTransfers = EquityTransferUtil.generateDividends(trade);
		}
		List<CashTransfer> dividends = new ArrayList<>();

		for (CashTransfer transfer : cashTransfers) {
			if (transfer.getPurpose().equals(TransferPurpose.DIVIDEND)
					&& (existingDividends == null || !existingDividends.contains(transfer))) {
				dividends.add(transfer);
			}
		}

		return dividends;
	}

	private CashTransfer createNewEquityPayment(EquityTrade trade) {

		CashTransfer payment = new CashTransfer(trade.getBook(), TransferPurpose.EQUITY_PAYMENT,
				trade.getSettlementDate(), trade, trade.getCurrency());
		payment.setCreationDateTime(LocalDateTime.now());
		if (trade.isBuy()) {
			payment.setDirection(Transfer.Direction.PAY);
		} else {
			payment.setDirection(Transfer.Direction.RECEIVE);
		}
		payment.setAmount(trade.getAmount().multiply(trade.getQuantity()));
		payment.setFixingDateTime(trade.getCreationDate().atStartOfDay());
		payment.setStatus(Transfer.Status.KNOWN);

		return payment;
	}

	@Override
	public void fixCashTransfer(CashTransfer transfer, long quoteSetId) throws TradistaBusinessException {
		Equity equity = (Equity) transfer.getProduct();
		String quoteName = Equity.EQUITY + "." + equity.getIsin() + equity.getExchange();
		BigDecimal equityPrice = PricerUtil.getValueAsOfDateFromQuote(quoteName, quoteSetId, QuoteType.EQUITY_PRICE,
				QuoteValue.CLOSE, transfer.getFixingDateTime().toLocalDate());
		BigDecimal dividend;
		BigDecimal dividendYield = PricerUtil.getValueAsOfDateFromQuote(quoteName, quoteSetId, QuoteType.DIVIDEND_YIELD,
				QuoteValue.CLOSE, transfer.getFixingDateTime().toLocalDate());

		if (dividendYield == null) {
			FixingError fixingError = new FixingError();
			fixingError.setCashTransfer(transfer);
			fixingError.setErrorDate(LocalDateTime.now());
			String errorMsg = String.format(
					"Transfer %d cannot be fixed. Impossible to get the %s dividend yield closing value as of %tD in QuoteSet %d.",
					transfer.getId(), quoteName, transfer.getFixingDateTime(), quoteSetId);
			fixingError.setErrorMessage(errorMsg);
			fixingError.setStatus(org.eclipse.tradista.core.error.model.Error.Status.UNSOLVED);
			List<FixingError> errors = new ArrayList<>(1);
			errors.add(fixingError);
			fixingErrorBusinessDelegate.saveFixingErrors(errors);
			throw new TradistaBusinessException(errorMsg);
		}

		if (dividendYield.signum() <= 0) {
			// No transfer
			transferBusinessDelegate.deleteTransfer(transfer.getId());
			return;
			// TODO add a warn somewhere ?
		}

		if (equityPrice == null) {
			FixingError fixingError = new FixingError();
			fixingError.setCashTransfer(transfer);
			fixingError.setErrorDate(LocalDateTime.now());
			String errorMsg = String.format(
					"Transfer %d cannot be fixed. Impossible to get the %s price closing value as of %tD in QuoteSet %d.",
					transfer.getId(), quoteName, transfer.getFixingDateTime(), quoteSetId);
			fixingError.setErrorMessage(errorMsg);
			fixingError.setStatus(org.eclipse.tradista.core.error.model.Error.Status.UNSOLVED);
			List<FixingError> errors = new ArrayList<>(1);
			errors.add(fixingError);
			fixingErrorBusinessDelegate.saveFixingErrors(errors);
			throw new TradistaBusinessException(errorMsg);
		}

		BigDecimal quantity = productInventoryBusinessDelegate.getQuantityByDateProductAndBookIds(equity.getId(),
				transfer.getBook().getId(), transfer.getFixingDateTime().toLocalDate());

		dividend = equityPrice.divide(BigDecimal.valueOf(100), configurationBusinessDelegate.getRoundingMode())
				.multiply(dividendYield).multiply(quantity);
		transfer.setDirection(Transfer.Direction.RECEIVE);
		transfer.setAmount(dividend);
		transferBusinessDelegate.saveTransfer(transfer);
	}

}