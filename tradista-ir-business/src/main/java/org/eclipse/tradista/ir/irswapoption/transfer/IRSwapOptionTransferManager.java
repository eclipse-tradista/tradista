package org.eclipse.tradista.ir.irswapoption.transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.model.OptionTrade.SettlementType;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.FixingError;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.TransferManager;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.FixingErrorBusinessDelegate;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.eclipse.tradista.ir.irswap.model.IRSwapTrade;
import org.eclipse.tradista.ir.irswapoption.messaging.IRSwapOptionTradeEvent;
import org.eclipse.tradista.ir.irswapoption.model.IRSwapOptionTrade;

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

public class IRSwapOptionTransferManager implements TransferManager<IRSwapOptionTradeEvent> {

	protected TransferBusinessDelegate transferBusinessDelegate;

	protected FixingErrorBusinessDelegate fixingErrorBusinessDelegate;

	protected ConfigurationBusinessDelegate configurationBusinessDelegate;

	public IRSwapOptionTransferManager() {
		transferBusinessDelegate = new TransferBusinessDelegate();
		fixingErrorBusinessDelegate = new FixingErrorBusinessDelegate();
	}

	@Override
	public void createTransfers(IRSwapOptionTradeEvent event) throws TradistaBusinessException {
		IRSwapOptionTrade trade = event.getTrade();
		List<Transfer> transfersToBeSaved = new ArrayList<Transfer>();

		if (event.getOldTrade() != null) {
			IRSwapOptionTrade oldTrade = event.getOldTrade();
			// Exercise info changed, the transfer must be canceled and
			// recreated
			if (oldTrade.getExerciseDate() != null && oldTrade.getSettlementType().equals(SettlementType.CASH)
					&& trade.getExerciseDate() != null && trade.getSettlementType().equals(SettlementType.CASH)) {
				if (!oldTrade.getExerciseDate().equals(trade.getExerciseDate())
						|| !oldTrade.getSettlementType().equals(trade.getSettlementType())
						|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())
						|| ((oldTrade.getCashSettlementAmount() == null) && (trade.getCashSettlementAmount() != null))
						|| ((oldTrade.getCashSettlementAmount() != null) && (trade.getCashSettlementAmount() == null))
						|| ((oldTrade.getCashSettlementAmount() != null) && (trade.getCashSettlementAmount() != null)
								&& (oldTrade.getCashSettlementAmount().compareTo(trade.getCashSettlementAmount()) != 0))
						|| ((oldTrade.getAlternativeCashSettlementReferenceRateIndex() == null)
								&& (trade.getAlternativeCashSettlementReferenceRateIndex() != null))
						|| ((oldTrade.getAlternativeCashSettlementReferenceRateIndex() != null)
								&& (trade.getAlternativeCashSettlementReferenceRateIndex() == null))
						|| ((oldTrade.getAlternativeCashSettlementReferenceRateIndex() != null)
								&& (trade.getAlternativeCashSettlementReferenceRateIndex() != null)
								&& (!oldTrade.getAlternativeCashSettlementReferenceRateIndex()
										.equals(trade.getAlternativeCashSettlementReferenceRateIndex())))
						|| ((oldTrade.getAlternativeCashSettlementReferenceRateIndexTenor() == null)
								&& (trade.getAlternativeCashSettlementReferenceRateIndexTenor() != null))
						|| ((oldTrade.getAlternativeCashSettlementReferenceRateIndexTenor() != null)
								&& (trade.getAlternativeCashSettlementReferenceRateIndexTenor() == null))
						|| ((oldTrade.getAlternativeCashSettlementReferenceRateIndexTenor() != null)
								&& (trade.getAlternativeCashSettlementReferenceRateIndexTenor() != null)
								&& (!oldTrade.getAlternativeCashSettlementReferenceRateIndexTenor()
										.equals(trade.getAlternativeCashSettlementReferenceRateIndexTenor())))
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
		} else {
			if (trade.getExerciseDate() != null && trade.getSettlementType().equals(SettlementType.CASH)) {
				transfersToBeSaved.add(createNewCashSettlementTransfer(trade));
			}
			transfersToBeSaved.add(createNewPremiumTransfer(trade));
		}

		if (!transfersToBeSaved.isEmpty()) {
			transferBusinessDelegate.saveTransfers(transfersToBeSaved);
		}
	}

	private CashTransfer createNewCashSettlementTransfer(IRSwapOptionTrade trade) throws TradistaBusinessException {
		CashTransfer cashSettlementTransfer = new CashTransfer(trade.getBook(), TransferPurpose.CASH_SETTLEMENT,
				trade.getSettlementDate(), trade, trade.getUnderlying().getCurrency());
		cashSettlementTransfer.setCreationDateTime(LocalDateTime.now());
		cashSettlementTransfer.setFixingDateTime(trade.getExerciseDate().atStartOfDay());
		cashSettlementTransfer.setStatus(Transfer.Status.UNKNOWN);

		return cashSettlementTransfer;
	}

	private CashTransfer createNewPremiumTransfer(IRSwapOptionTrade trade) throws TradistaBusinessException {
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

	@Override
	public void fixCashTransfer(CashTransfer transfer, long quoteSetId) throws TradistaBusinessException {
		IRSwapOptionTrade trade = (IRSwapOptionTrade) transfer.getTrade();
		BigDecimal notional = trade.getAmount();
		BigDecimal fractionedNotional;
		BigDecimal amount;
		if (trade.getCashSettlementAmount() != null) {
			amount = trade.getCashSettlementAmount();
		} else {
			Index index;
			Tenor indexTenor;
			if (trade.getAlternativeCashSettlementReferenceRateIndex() != null) {
				index = trade.getAlternativeCashSettlementReferenceRateIndex();
				indexTenor = trade.getAlternativeCashSettlementReferenceRateIndexTenor();
			} else {
				index = trade.getUnderlying().getReceptionReferenceRateIndex();
				if (trade.getUnderlying().getMaturityTenor() != null) {
					indexTenor = trade.getUnderlying().getMaturityTenor();
				} else {
					FixingError fixingError = new FixingError();
					fixingError.setCashTransfer(transfer);
					fixingError.setErrorDate(LocalDateTime.now());
					String errorMsg = String.format(
							"Transfer %n cannot be fixed. No defined cash settlement, no alternative index tenor and no underlying maturity.",
							transfer.getId());
					fixingError.setMessage(errorMsg);
					fixingError.setStatus(org.eclipse.tradista.core.error.model.Error.Status.UNSOLVED);
					List<FixingError> errors = new ArrayList<FixingError>(1);
					errors.add(fixingError);
					fixingErrorBusinessDelegate.saveFixingErrors(errors);
					throw new TradistaBusinessException(errorMsg);
				}
			}
			String quoteName = IRSwapTrade.IR_SWAP + "." + index + "." + indexTenor;
			BigDecimal ir = PricerUtil.getValueAsOfDateFromQuote(quoteName, quoteSetId, QuoteType.INTEREST_RATE,
					QuoteValue.CLOSE, transfer.getFixingDateTime().toLocalDate());
			/*
			 * Assuming the Swap Quote represent the value of a Swap, not a spread. Value
			 * being expressed as a fixed rate to be paid for the maturity So, we need to
			 * subtract to this rate the agreed fixed rate in the swaption. Ex. of Swap
			 * quotes: ICE SWAP Rate (formerly ISDAFix):
			 * https://www.theice.com/iba/ice-swap-rate
			 */
			ir = ir.subtract(trade.getUnderlying().getPaymentFixedInterestRate());
			if (ir == null) {
				FixingError fixingError = new FixingError();
				fixingError.setCashTransfer(transfer);
				fixingError.setErrorDate(LocalDateTime.now());
				String errorMsg = String.format(
						"Transfer %n cannot be fixed. Impossible to get the %s index closing value as of %tD in QuoteSet %s.",
						transfer.getId(), quoteName, transfer.getFixingDateTime(), quoteSetId);
				fixingError.setMessage(errorMsg);
				fixingError.setStatus(org.eclipse.tradista.core.error.model.Error.Status.UNSOLVED);
				List<FixingError> errors = new ArrayList<FixingError>(1);
				errors.add(fixingError);
				fixingErrorBusinessDelegate.saveFixingErrors(errors);
				throw new TradistaBusinessException(errorMsg);
			}
			fractionedNotional = notional.multiply(PricerUtil.daysToYear(indexTenor));
			amount = fractionedNotional
					.multiply(ir.divide(BigDecimal.valueOf(100), configurationBusinessDelegate.getRoundingMode()));
		}
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
					direction = Transfer.Direction.RECEIVE;
				} else {
					direction = Transfer.Direction.PAY;
					amount = amount.negate();
				}
			}
		} else {
			if (amount.signum() > 0) {
				direction = Transfer.Direction.PAY;
			} else {
				direction = Transfer.Direction.RECEIVE;
				amount = amount.negate();
			}
		}
		transfer.setDirection(direction);
		transfer.setAmount(amount);
		transferBusinessDelegate.saveTransfer(transfer);
	}

}