package finance.tradista.security.equity.transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import finance.tradista.core.common.exception.TradistaBusinessException;
import finance.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import finance.tradista.core.marketdata.model.QuoteType;
import finance.tradista.core.marketdata.model.QuoteValue;
import finance.tradista.core.pricing.util.PricerUtil;
import finance.tradista.core.transfer.model.CashTransfer;
import finance.tradista.core.transfer.model.FixingError;
import finance.tradista.core.transfer.model.ProductTransfer;
import finance.tradista.core.transfer.model.Transfer;
import finance.tradista.core.transfer.model.TransferManager;
import finance.tradista.core.transfer.model.TransferPurpose;
import finance.tradista.core.transfer.service.FixingErrorBusinessDelegate;
import finance.tradista.core.transfer.service.TransferBusinessDelegate;
import finance.tradista.security.equity.messaging.EquityTradeEvent;
import finance.tradista.security.equity.model.Equity;
import finance.tradista.security.equity.model.EquityTrade;

/*
 * Copyright 2018 Olivier Asuncion
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.    */

public class EquityTransferManager implements TransferManager<EquityTradeEvent> {

	protected TransferBusinessDelegate transferBusinessDelegate;

	protected FixingErrorBusinessDelegate fixingErrorBusinessDelegate;

	protected ConfigurationBusinessDelegate configurationBusinessDelegate;

	public EquityTransferManager() {
		transferBusinessDelegate = new TransferBusinessDelegate();
		fixingErrorBusinessDelegate = new FixingErrorBusinessDelegate();
		configurationBusinessDelegate = new ConfigurationBusinessDelegate();
	}

	@Override
	public void createTransfers(EquityTradeEvent event) throws TradistaBusinessException {
		EquityTrade trade = event.getTrade();
		List<Transfer> transfersToBeSaved = new ArrayList<Transfer>();

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
						// Need to set trade to the transfer because if it has a null trade date (not
						// exercised equity
						// option underlying), it was not set to the transfer in
						// transferBusinessDelegate.getTransfersByTradeIdAndPurpose
						transfer.setTrade(oldTrade);
						transfersToBeSaved.add(transfer);
					}
				}
				if (trade.getSettlementDate() != null) {
					transfersToBeSaved.add(createNewEquityPayment(cashTransfers, trade));
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
						// Need to set trade to the transfer because if it has a null trade date (not
						// exercised equity
						// option underlying), it was not set to the transfer in
						// transferBusinessDelegate.getTransfersByTradeIdAndPurpose
						transfer.setTrade(oldTrade);
						transfersToBeSaved.add(transfer);
					}
				}
				if (trade.getSettlementDate() != null) {
					transfersToBeSaved.add(createNewEquitySettlement(trade));
				}

			}

		} else {
			transfersToBeSaved.add(createNewEquitySettlement(trade));
			transfersToBeSaved.add(createNewEquityPayment(cashTransfers, trade));
			if (trade.getProduct().isPayDividend()) {
				transfersToBeSaved.addAll(createNewDividends(cashTransfers, trade));
			}
		}

		if (!transfersToBeSaved.isEmpty()) {
			transferBusinessDelegate.saveTransfers(transfersToBeSaved);
		}

	}

	private ProductTransfer createNewEquitySettlement(EquityTrade trade) throws TradistaBusinessException {
		ProductTransfer productTransfer = new ProductTransfer();
		productTransfer.setCreationDateTime(LocalDateTime.now());
		if (trade.isBuy()) {
			productTransfer.setDirection(Transfer.Direction.RECEIVE);
		} else {
			productTransfer.setDirection(Transfer.Direction.PAY);
		}
		productTransfer.setSettlementDate(trade.getSettlementDate());
		productTransfer.setProduct(trade.getProduct());
		productTransfer.setPurpose(TransferPurpose.EQUITY_SETTLEMENT);
		productTransfer.setQuantity(trade.getQuantity());
		productTransfer.setFixingDateTime(trade.getCreationDate().atStartOfDay());
		productTransfer.setStatus(Transfer.Status.KNOWN);
		productTransfer.setTrade(trade);
		productTransfer.setBook(trade.getBook());

		return productTransfer;
	}

	private List<CashTransfer> createNewDividends(List<CashTransfer> cashTransfers, EquityTrade trade)
			throws TradistaBusinessException {

		List<Transfer> existingDividends = transferBusinessDelegate.getTransfers(Transfer.Type.CASH, null, null,
				TransferPurpose.DIVIDEND, 0, trade.getProductId(), trade.getBook().getId(),
				trade.getProduct().getDividendCurrency().getId(), null, null, trade.getSettlementDate(), null, null,
				null);
		existingDividends = existingDividends.stream().filter(t -> !t.getStatus().equals(Transfer.Status.CANCELED))
				.collect(Collectors.toList());

		if (cashTransfers == null) {
			cashTransfers = EquityTransferUtil.generateDividends(trade);
		}
		List<CashTransfer> dividends = new ArrayList<CashTransfer>();

		for (CashTransfer transfer : cashTransfers) {
			if (transfer.getPurpose().equals(TransferPurpose.DIVIDEND) && !existingDividends.contains(transfer)) {
				dividends.add(transfer);
			}
		}

		return dividends;
	}

	private CashTransfer createNewEquityPayment(List<CashTransfer> cashTransfers, EquityTrade trade)
			throws TradistaBusinessException {

		CashTransfer payment = new CashTransfer();
		payment.setSettlementDate(trade.getSettlementDate());
		payment.setCreationDateTime(LocalDateTime.now());
		if (trade.isBuy()) {
			payment.setDirection(Transfer.Direction.PAY);
		} else {
			payment.setDirection(Transfer.Direction.RECEIVE);
		}
		payment.setAmount(trade.getAmount().multiply(trade.getQuantity()));
		payment.setCurrency(trade.getCurrency());
		payment.setFixingDateTime(trade.getCreationDate().atStartOfDay());
		payment.setPurpose(TransferPurpose.EQUITY_PAYMENT);
		payment.setStatus(Transfer.Status.KNOWN);
		payment.setTrade(trade);
		payment.setBook(trade.getBook());

		return payment;
	}

	@Override
	public void fixCashTransfer(CashTransfer transfer, long quoteSetId) throws TradistaBusinessException {
		EquityTrade trade = (EquityTrade) transfer.getTrade();
		Equity equity = trade.getProduct();
		String quoteName = Equity.EQUITY + "." + equity.getIsin() + equity.getExchange();
		BigDecimal equityPrice = PricerUtil.getValueAsOfDateFromQuote(quoteName, quoteSetId, QuoteType.EQUITY_PRICE,
				QuoteValue.CLOSE, transfer.getFixingDateTime().toLocalDate());
		BigDecimal dividend;
		if (equityPrice == null) {
			FixingError fixingError = new FixingError();
			fixingError.setCashTransfer(transfer);
			fixingError.setErrorDate(LocalDateTime.now());
			String errorMsg = String.format(
					"Transfer %n cannot be fixed. Impossible to get the %s price closing value as of %tD in QuoteSet %s.",
					transfer.getId(), quoteName, transfer.getFixingDateTime(), quoteSetId);
			fixingError.setMessage(errorMsg);
			fixingError.setStatus(finance.tradista.core.error.model.Error.Status.UNSOLVED);
			List<FixingError> errors = new ArrayList<FixingError>(1);
			errors.add(fixingError);
			fixingErrorBusinessDelegate.saveFixingErrors(errors);
			throw new TradistaBusinessException(errorMsg);
		}
		BigDecimal dividendYield = PricerUtil.getValueAsOfDateFromQuote(quoteName, quoteSetId, QuoteType.DIVIDEND_YIELD,
				QuoteValue.CLOSE, transfer.getFixingDateTime().toLocalDate());
		if (dividendYield == null) {
			FixingError fixingError = new FixingError();
			fixingError.setCashTransfer(transfer);
			fixingError.setErrorDate(LocalDateTime.now());
			String errorMsg = String.format(
					"Transfer %n cannot be fixed. Impossible to get the %s dividend yield closing value as of %tD in QuoteSet %s.",
					transfer.getId(), quoteName, transfer.getFixingDateTime(), quoteSetId);
			fixingError.setMessage(errorMsg);
			fixingError.setStatus(finance.tradista.core.error.model.Error.Status.UNSOLVED);
			List<FixingError> errors = new ArrayList<FixingError>(1);
			errors.add(fixingError);
			fixingErrorBusinessDelegate.saveFixingErrors(errors);
			throw new TradistaBusinessException(errorMsg);
		}

		dividend = equityPrice.divide(BigDecimal.valueOf(100), configurationBusinessDelegate.getRoundingMode())
				.multiply(dividendYield);

		if (dividend.signum() == 0) {
			// No transfer
			transferBusinessDelegate.deleteTransfer(transfer.getId());
			return;
			// TODO add a warn somewhere ?
		}
		Transfer.Direction direction;
		if (trade.isBuy()) {
			if (dividend.signum() > 0) {
				direction = Transfer.Direction.RECEIVE;
			} else {
				direction = Transfer.Direction.PAY;
				dividend = dividend.negate();
			}
		} else {
			if (dividend.signum() > 0) {
				direction = Transfer.Direction.RECEIVE;
			} else {
				direction = Transfer.Direction.PAY;
				dividend = dividend.negate();
			}
		}
		transfer.setDirection(direction);
		transfer.setAmount(dividend);
		transferBusinessDelegate.saveTransfer(transfer);
	}

}