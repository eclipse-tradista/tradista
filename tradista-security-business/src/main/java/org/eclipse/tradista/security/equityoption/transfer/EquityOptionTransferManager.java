package org.eclipse.tradista.security.equityoption.transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.trade.model.OptionTrade.SettlementType;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.FixingError;
import org.eclipse.tradista.core.transfer.model.ProductTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.TransferManager;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.FixingErrorBusinessDelegate;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equityoption.messaging.EquityOptionTradeEvent;
import org.eclipse.tradista.security.equityoption.model.EquityOptionTrade;

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

public class EquityOptionTransferManager implements TransferManager<EquityOptionTradeEvent> {

	protected TransferBusinessDelegate transferBusinessDelegate;

	protected FixingErrorBusinessDelegate fixingErrorBusinessDelegate;

	public EquityOptionTransferManager() {
		transferBusinessDelegate = new TransferBusinessDelegate();
		fixingErrorBusinessDelegate = new FixingErrorBusinessDelegate();
	}

	@Override
	public void createTransfers(EquityOptionTradeEvent event) throws TradistaBusinessException {

		EquityOptionTrade trade = event.getTrade();
		List<Transfer> transfersToBeSaved = new ArrayList<Transfer>();

		if (event.getOldTrade() != null) {
			EquityOptionTrade oldTrade = event.getOldTrade();
			// Exercise info changed, the transfer must be canceled and
			// recreated
			if (oldTrade.getExerciseDate() != null && oldTrade.getSettlementType().equals(SettlementType.CASH)
					&& trade.getExerciseDate() != null && trade.getSettlementType().equals(SettlementType.CASH)) {
				if (!oldTrade.getExerciseDate().equals(trade.getExerciseDate())
						|| !oldTrade.getSettlementType().equals(trade.getSettlementType())
						|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())
						|| (oldTrade.isBuy() != trade.isBuy()) || (!oldTrade.getBook().equals(trade.getBook()))) {
					List<Transfer> transfers = transferBusinessDelegate
							.getTransfersByTradeIdAndPurpose(oldTrade.getId(), TransferPurpose.CASH_SETTLEMENT, false);
					// if the transfer is null, it is not normal, but the
					// process should
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
				// There was no exercise before but now there is one, a transfer
				// must be created
				if ((oldTrade.getExerciseDate() == null || oldTrade.getSettlementType().equals(SettlementType.PHYSICAL))
						&& trade.getExerciseDate() != null && trade.getSettlementType().equals(SettlementType.CASH)) {
					transfersToBeSaved.add(createNewCashSettlementTransfer(trade));
				}
				// There was an exercise before but not anymore, the transfer
				// must be canceled
				if (oldTrade.getExerciseDate() != null && oldTrade.getSettlementType().equals(SettlementType.CASH)
						&& (trade.getExerciseDate() == null
								|| trade.getSettlementType().equals(SettlementType.PHYSICAL))) {
					List<Transfer> transfers = transferBusinessDelegate
							.getTransfersByTradeIdAndPurpose(oldTrade.getId(), TransferPurpose.CASH_SETTLEMENT, false);
					// if the transfer is null, it is not normal, but the
					// process should
					// continue.
					if (transfers == null || transfers.isEmpty()) {
						// TODO logs + Errors viewable in the error report ?
					} else {
						transfers.get(0).setStatus(Transfer.Status.CANCELED);
						transfersToBeSaved.add(transfers.get(0));
					}
				}
			}
			if (!oldTrade.getCurrency().equals(trade.getCurrency())
					|| oldTrade.getAmount().compareTo(trade.getAmount()) != 0
					|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())
					|| (oldTrade.isBuy() != trade.isBuy()) || (!oldTrade.getBook().equals(trade.getBook()))) {
				List<Transfer> transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(oldTrade.getId(),
						TransferPurpose.PREMIUM, false);
				// if the transfer is null, it is not normal, but the process
				// should
				// continue.
				if (transfers == null || transfers.isEmpty()) {
					// TODO logs + Errors viewable in the error report ?
				} else {
					transfers.get(0).setStatus(Transfer.Status.CANCELED);
					transfersToBeSaved.add(transfers.get(0));
				}
				transfersToBeSaved.add(createNewPremiumTransfer(trade));
			}

			if ((oldTrade.getProduct() != null && !oldTrade.getProduct().equals(trade.getProduct()))
					|| (oldTrade.getProduct() == null && trade.getProduct() != null)
					|| (oldTrade.getQuantity() != null && trade.getQuantity() != null
							&& oldTrade.getQuantity().compareTo(trade.getQuantity()) != 0)
					|| (oldTrade.getQuantity() == null && trade.getQuantity() != null)
					|| (oldTrade.getQuantity() != null && trade.getQuantity() == null)
					|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())
					|| (oldTrade.isBuy() != trade.isBuy())) {

				if (oldTrade.getProduct() != null) {
					List<Transfer> transfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(
							oldTrade.getId(), TransferPurpose.EQUITY_OPTION_SETTLEMENT, false);
					// if the transfers list is null or empty, it is not normal,
					// but
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
				}
				if (trade.getProduct() != null) {
					transfersToBeSaved.add(createNewEquityOptionSettlementTransfer(trade));
				}
			}

		} else {
			if (trade.getExerciseDate() != null && trade.getSettlementType().equals(SettlementType.CASH)) {
				transfersToBeSaved.add(createNewCashSettlementTransfer(trade));
			}
			transfersToBeSaved.add(createNewPremiumTransfer(trade));
			if (trade.getEquityOption() != null) {
				transfersToBeSaved.add(createNewEquityOptionSettlementTransfer(trade));
			}
		}

		if (!transfersToBeSaved.isEmpty()) {
			transferBusinessDelegate.saveTransfers(transfersToBeSaved);
		}
	}

	private CashTransfer createNewCashSettlementTransfer(EquityOptionTrade trade) throws TradistaBusinessException {
		CashTransfer cashSettlementTransfer = new CashTransfer(trade.getBook(), TransferPurpose.CASH_SETTLEMENT,
				trade.getExerciseDate(), trade, trade.getUnderlying().getCurrency());
		cashSettlementTransfer.setCreationDateTime(LocalDateTime.now());
		cashSettlementTransfer.setFixingDateTime(trade.getExerciseDate().atStartOfDay());
		cashSettlementTransfer.setStatus(Transfer.Status.UNKNOWN);

		return cashSettlementTransfer;
	}

	private CashTransfer createNewPremiumTransfer(EquityOptionTrade trade) throws TradistaBusinessException {
		CashTransfer premiumTransfer = new CashTransfer(trade.getBook(), TransferPurpose.PREMIUM,
				trade.getSettlementDate(), trade, trade.getCurrency());
		premiumTransfer.setCreationDateTime(LocalDateTime.now());
		premiumTransfer.setFixingDateTime(LocalDateTime.now());
		premiumTransfer.setStatus(Transfer.Status.KNOWN);
		premiumTransfer.setAmount(trade.getAmount());
		if (trade.isBuy()) {
			premiumTransfer.setDirection(Transfer.Direction.PAY);
		} else {
			premiumTransfer.setDirection(Transfer.Direction.RECEIVE);
		}

		return premiumTransfer;
	}

	private ProductTransfer createNewEquityOptionSettlementTransfer(EquityOptionTrade trade)
			throws TradistaBusinessException {
		ProductTransfer eqOptionSettlementTransfer = new ProductTransfer(trade.getBook(),
				TransferPurpose.EQUITY_OPTION_SETTLEMENT, trade.getSettlementDate(), trade);
		eqOptionSettlementTransfer.setCreationDateTime(LocalDateTime.now());
		eqOptionSettlementTransfer.setFixingDateTime(trade.getCreationDate().atStartOfDay());
		eqOptionSettlementTransfer.setStatus(Transfer.Status.KNOWN);
		eqOptionSettlementTransfer.setQuantity(trade.getQuantity());
		if (trade.isBuy()) {
			eqOptionSettlementTransfer.setDirection(Transfer.Direction.RECEIVE);
		} else {
			eqOptionSettlementTransfer.setDirection(Transfer.Direction.PAY);
		}

		return eqOptionSettlementTransfer;
	}

	@Override
	public void fixCashTransfer(CashTransfer transfer, long quoteSetId) throws TradistaBusinessException {
		EquityOptionTrade trade = (EquityOptionTrade) transfer.getTrade();
		Equity equity = trade.getUnderlying().getProduct();
		String quoteName = Equity.EQUITY + "." + equity.getIsin() + equity.getExchange();
		BigDecimal equityPrice = PricerUtil.getValueAsOfDateFromQuote(quoteName, quoteSetId, QuoteType.EQUITY_PRICE,
				QuoteValue.CLOSE, transfer.getFixingDateTime().toLocalDate());
		BigDecimal amount;
		if (equityPrice == null) {
			FixingError fixingError = new FixingError();
			fixingError.setCashTransfer(transfer);
			fixingError.setErrorDate(LocalDateTime.now());
			String errorMsg = String.format(
					"Transfer %n cannot be fixed. Impossible to get the %s price closing value as of %tD in QuoteSet %s.",
					transfer.getId(), quoteName, transfer.getFixingDateTime(), quoteSetId);
			fixingError.setMessage(errorMsg);
			fixingError.setStatus(org.eclipse.tradista.core.error.model.Error.Status.UNSOLVED);
			List<FixingError> errors = new ArrayList<FixingError>(1);
			errors.add(fixingError);
			fixingErrorBusinessDelegate.saveFixingErrors(errors);
			throw new TradistaBusinessException(errorMsg);
		}
		amount = equityPrice.multiply(trade.getUnderlying().getQuantity());

		if (amount.signum() == 0) {
			// No transfer
			transferBusinessDelegate.deleteTransfer(transfer.getId());
			return;
			// TODO add a warn somewhere ?
		}
		Transfer.Direction direction;
		if (trade.isBuy()) {
			if (trade.isCall()) {
				if (amount.signum() > 0) {
					direction = Transfer.Direction.RECEIVE;
				} else {
					direction = Transfer.Direction.PAY;
					amount = amount.negate();
				}
			} else {
				if (amount.signum() > 0) {
					direction = Transfer.Direction.PAY;
				} else {
					direction = Transfer.Direction.RECEIVE;
					amount = amount.negate();
				}
			}
		} else {
			if (trade.isCall()) {
				if (amount.signum() > 0) {
					direction = Transfer.Direction.PAY;
				} else {
					direction = Transfer.Direction.RECEIVE;
					amount = amount.negate();
				}
			} else {
				if (amount.signum() > 0) {
					direction = Transfer.Direction.RECEIVE;
				} else {
					direction = Transfer.Direction.PAY;
					amount = amount.negate();
				}
			}
		}
		transfer.setDirection(direction);
		transfer.setAmount(amount);
		transferBusinessDelegate.saveTransfer(transfer);
	}

}