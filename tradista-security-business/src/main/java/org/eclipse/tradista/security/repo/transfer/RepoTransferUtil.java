package org.eclipse.tradista.security.repo.transfer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.daycountconvention.model.DayCountConvention;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.FixingError;
import org.eclipse.tradista.core.transfer.model.ProductTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.Transfer.Status;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.FixingErrorBusinessDelegate;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.eclipse.tradista.security.common.model.Security;
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;
import org.eclipse.tradista.security.repo.model.RepoTrade;
import org.eclipse.tradista.security.repo.trade.RepoTradeUtil;
import org.eclipse.tradista.security.specificrepo.model.SpecificRepoTrade;
import org.springframework.util.CollectionUtils;

/********************************************************************************
 * Copyright (c) 2024 Olivier Asuncion
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

public final class RepoTransferUtil {

	private static ConfigurationBusinessDelegate configurationBusinessDelegate = new ConfigurationBusinessDelegate();

	private static TransferBusinessDelegate transferBusinessDelegate = new TransferBusinessDelegate();

	private static FixingErrorBusinessDelegate fixingErrorBusinessDelegate = new FixingErrorBusinessDelegate();

	private RepoTransferUtil() {
	}

	public static List<CashTransfer> createOrUpdateCashPaymentOpeningLeg(CashTransfer existingCashTransfer,
			RepoTrade trade, boolean isPartiallyTerminated) {

		List<CashTransfer> cashPayments = new ArrayList<>();

		if (!isPartiallyTerminated) {
			if (existingCashTransfer != null) {
				existingCashTransfer.setStatus(Status.CANCELED);
				cashPayments.add(existingCashTransfer);
			}
		}

		// New cash settlement (opening leg)
		CashTransfer newCashPayment = new CashTransfer(trade.getBook(), TransferPurpose.CASH_SETTLEMENT,
				trade.getSettlementDate(), trade, trade.getCurrency());
		newCashPayment.setAmount(trade.getAmount());
		newCashPayment.setCreationDateTime(LocalDateTime.now());
		newCashPayment.setDirection(trade.isBuy() ? Transfer.Direction.RECEIVE : Transfer.Direction.PAY);
		newCashPayment.setStatus(Transfer.Status.KNOWN);
		newCashPayment.setFixingDateTime(trade.getCreationDate().atStartOfDay());
		cashPayments.add(newCashPayment);

		return cashPayments;
	}

	public static List<CashTransfer> createOrUpdateCashPaymentClosingLeg(List<Transfer> existingCashTransfers,
			RepoTrade trade, BigDecimal notionalReduction) {

		List<CashTransfer> cashPayments = new ArrayList<>();

		// No partial termination
		if (notionalReduction == null) {
			if (existingCashTransfers != null) {
				existingCashTransfers.stream().forEach(t -> {
					t.setStatus(Status.CANCELED);
					cashPayments.add((CashTransfer) t);
				});
			}
			// Returned cash settlement (closing leg)
			CashTransfer newCashPayment = new CashTransfer(trade.getBook(), TransferPurpose.RETURNED_CASH_PLUS_INTEREST,
					trade.getEndDate(), trade, trade.getCurrency());
			if (trade.isFixedRepoRate()) {
				BigDecimal repoRate = trade.getRepoRate().divide(new BigDecimal(100),
						configurationBusinessDelegate.getScale(), configurationBusinessDelegate.getRoundingMode());
				BigDecimal interestAmount = trade.getAmount().multiply(repoRate)
						.multiply(PricerUtil.daysToYear(new DayCountConvention(DayCountConvention.ACT_360),
								trade.getSettlementDate(), trade.getEndDate()));
				newCashPayment.setAmount(trade.getAmount().add(interestAmount));
				newCashPayment.setStatus(Transfer.Status.KNOWN);
				newCashPayment.setFixingDateTime(trade.getCreationDate().atStartOfDay());
			} else {
				newCashPayment.setStatus(Transfer.Status.UNKNOWN);
			}
			newCashPayment.setCreationDateTime(LocalDateTime.now());
			newCashPayment.setDirection(trade.isBuy() ? Transfer.Direction.PAY : Transfer.Direction.RECEIVE);

			cashPayments.add(newCashPayment);

		} else {
			// Partial termination

			// 1. Return of the fraction of the cash amount + interest as of partial
			// termination date
			CashTransfer cashPartialPayment = new CashTransfer(trade.getBook(),
					TransferPurpose.RETURNED_CASH_PLUS_INTEREST, LocalDate.now(), trade, trade.getCurrency());

			BigDecimal interestAmount = null;
			if (trade.isFixedRepoRate()) {
				BigDecimal repoRate = trade.getRepoRate().divide(new BigDecimal(100),
						configurationBusinessDelegate.getScale(), configurationBusinessDelegate.getRoundingMode());
				interestAmount = notionalReduction.multiply(repoRate)
						.multiply(PricerUtil.daysToYear(new DayCountConvention(DayCountConvention.ACT_360),
								trade.getSettlementDate(), LocalDate.now()));
				cashPartialPayment.setStatus(Transfer.Status.KNOWN);
				cashPartialPayment.setAmount(notionalReduction.add(interestAmount));
				cashPartialPayment.setFixingDateTime(trade.getCreationDate().atStartOfDay());
				cashPartialPayment.setCreationDateTime(LocalDateTime.now());
				cashPartialPayment.setDirection(trade.isBuy() ? Transfer.Direction.PAY : Transfer.Direction.RECEIVE);
				cashPayments.add(cashPartialPayment);
			} else {
				if (!existingCashTransfers.contains(cashPartialPayment)) {
					cashPartialPayment.setStatus(Transfer.Status.UNKNOWN);
					cashPartialPayment.setCreationDateTime(LocalDateTime.now());
					cashPartialPayment
							.setDirection(trade.isBuy() ? Transfer.Direction.PAY : Transfer.Direction.RECEIVE);
					cashPayments.add(cashPartialPayment);
				}
			}

			// 2. Return of the remaining cash + interest as of trade end date
			CashTransfer reducedCashPayment = new CashTransfer(trade.getBook(),
					TransferPurpose.RETURNED_CASH_PLUS_INTEREST, trade.getEndDate(), trade, trade.getCurrency());
			if (trade.isFixedRepoRate()) {
				Map<Transfer, Transfer> existingReturnedCashTransfersMap = existingCashTransfers.stream()
						.collect(Collectors.toMap(Function.identity(), Function.identity()));
				BigDecimal repoRate = trade.getRepoRate().divide(new BigDecimal(100),
						configurationBusinessDelegate.getScale(), configurationBusinessDelegate.getRoundingMode());
				BigDecimal reducedNotional = trade.getAmount().subtract(notionalReduction);
				interestAmount = reducedNotional.multiply(repoRate).multiply(PricerUtil.daysToYear(
						new DayCountConvention(DayCountConvention.ACT_360), LocalDate.now(), trade.getEndDate()));
				CashTransfer existingReturnedCashTransfer = (CashTransfer) existingReturnedCashTransfersMap
						.get(reducedCashPayment);
				existingReturnedCashTransfer.setAmount(trade.getAmount().add(interestAmount));
				cashPayments.add(existingReturnedCashTransfer);
			}
		}

		return cashPayments;

	}

	public static List<ProductTransfer> createOrUpdateCollateralPayment(List<Transfer> existingCollateralTransfers,
			RepoTrade trade, boolean isAllocated) {

		List<ProductTransfer> collateralPaymentsToBeSaved = new ArrayList<>();

		// First case: Repo not allocated
		if (!isAllocated) {

			List<ProductTransfer> newCollateralPayments = new ArrayList<>();

			List<Transfer> existingPotentialCollateralTransfers = null;
			if (existingCollateralTransfers != null) {
				existingPotentialCollateralTransfers = existingCollateralTransfers.stream()
						.filter(t -> t.getStatus().equals(Status.POTENTIAL)).toList();
			}

			if (trade instanceof SpecificRepoTrade specificRepoTrade) {
				generatePotentialCollateralTransfer(trade, newCollateralPayments, existingPotentialCollateralTransfers,
						collateralPaymentsToBeSaved, specificRepoTrade.getSecurity(), false);
			} else if (trade instanceof GCRepoTrade gcRepoTrade) {

				Set<Security> basketSec = gcRepoTrade.getGcBasket().getSecurities();
				if (!CollectionUtils.isEmpty(basketSec)) {
					for (Security sec : basketSec) {
						generatePotentialCollateralTransfer(trade, newCollateralPayments,
								existingPotentialCollateralTransfers, collateralPaymentsToBeSaved, sec, false);
					}
				}
			}

			// we cancel the existing collateral transfers that are not relevant anymore
			if (existingCollateralTransfers != null) {
				for (Transfer transfer : existingCollateralTransfers) {
					if (!newCollateralPayments.contains(transfer) || !transfer.getStatus().equals(Status.POTENTIAL)) {
						transfer.setStatus(Status.CANCELED);
						collateralPaymentsToBeSaved.add((ProductTransfer) transfer);
					}
				}
			}

		} else {
			// 2nd case : allocations

			// 2.1 First, remove existing potential transfers
			List<Transfer> existingPotentialCollateralTransfers = null;
			if (existingCollateralTransfers != null) {
				existingPotentialCollateralTransfers = existingCollateralTransfers.stream()
						.filter(t -> t.getStatus().equals(Status.POTENTIAL)).toList();

				if (existingPotentialCollateralTransfers != null) {
					for (Transfer transfer : existingPotentialCollateralTransfers) {
						try {
							transferBusinessDelegate.deleteTransfer(transfer.getId());
						} catch (TradistaBusinessException tbe) {
							// Not expected here
						}
					}
				}

			}

			// 2.2 Create allocation transfers
			existingCollateralTransfers = existingCollateralTransfers.stream()
					.filter(t -> !t.getStatus().equals(Status.POTENTIAL)).toList();
			Map<Transfer, Transfer> existingCollateralTransfersMap = existingCollateralTransfers.stream()
					.collect(Collectors.toMap(Function.identity(), Function.identity()));
			if (trade.getCollateralToAdd() != null) {
				for (Map.Entry<Security, Map<Book, BigDecimal>> entry : trade.getCollateralToAdd().entrySet()) {
					for (Map.Entry<Book, BigDecimal> bookEntry : entry.getValue().entrySet()) {
						ProductTransfer newCollateralPayment = new ProductTransfer(bookEntry.getKey(), entry.getKey(),
								TransferPurpose.COLLATERAL_SETTLEMENT, LocalDate.now(), trade);
						newCollateralPayment.setCreationDateTime(LocalDateTime.now());
						newCollateralPayment
								.setDirection(trade.isBuy() ? Transfer.Direction.PAY : Transfer.Direction.RECEIVE);
						newCollateralPayment.setQuantity(bookEntry.getValue());
						newCollateralPayment.setFixingDateTime(LocalDateTime.now());
						newCollateralPayment.setStatus(Transfer.Status.KNOWN);
						if (existingCollateralTransfers.contains(newCollateralPayment)) {
							ProductTransfer existingTransfer = (ProductTransfer) existingCollateralTransfersMap
									.get(newCollateralPayment);
							existingTransfer.setQuantity(
									existingTransfer.getQuantity().add(newCollateralPayment.getQuantity()));
							collateralPaymentsToBeSaved.add(existingTransfer);
						} else {
							collateralPaymentsToBeSaved.add(newCollateralPayment);
						}
					}
				}
			}
			if (trade.getCollateralToRemove() != null) {
				for (Map.Entry<Security, Map<Book, BigDecimal>> entry : trade.getCollateralToRemove().entrySet()) {
					for (Map.Entry<Book, BigDecimal> bookEntry : entry.getValue().entrySet()) {
						ProductTransfer newCollateralPayment = new ProductTransfer(bookEntry.getKey(), entry.getKey(),
								TransferPurpose.COLLATERAL_SETTLEMENT, LocalDate.now(), trade);
						newCollateralPayment.setCreationDateTime(LocalDateTime.now());
						newCollateralPayment
								.setDirection(trade.isBuy() ? Transfer.Direction.PAY : Transfer.Direction.RECEIVE);
						newCollateralPayment.setQuantity(bookEntry.getValue());
						newCollateralPayment.setFixingDateTime(LocalDateTime.now());
						newCollateralPayment.setStatus(Transfer.Status.KNOWN);
						// If existingCollateralTransfers contains newCollateralPayment, it means that
						// the settlement of the substituted collateral was planned today.
						if (existingCollateralTransfers.contains(newCollateralPayment)) {
							ProductTransfer existingTransfer = (ProductTransfer) existingCollateralTransfersMap
									.get(newCollateralPayment);
							existingTransfer.setQuantity(existingTransfer.getQuantity().subtract(bookEntry.getValue()));
							if (existingTransfer.getQuantity().equals(BigDecimal.ZERO)) {
								try {
									transferBusinessDelegate.deleteTransfer(existingTransfer.getId());
								} catch (TradistaBusinessException tbe) {
									// Not expected here
								}
							} else {
								collateralPaymentsToBeSaved.add(existingTransfer);
							}
						}
					}
				}
			}
		}

		return collateralPaymentsToBeSaved;
	}

	public static List<ProductTransfer> createOrUpdateReturnedCollateralPayment(
			List<Transfer> existingCollateralTransfers, RepoTrade trade, boolean isAllocated) {

		List<ProductTransfer> collateralPaymentsToBeSaved = new ArrayList<>();

		if (!isAllocated) {

			List<ProductTransfer> newCollateralPayments = new ArrayList<>();

			List<Transfer> existingPotentialCollateralTransfers = null;
			if (existingCollateralTransfers != null) {
				existingPotentialCollateralTransfers = existingCollateralTransfers.stream()
						.filter(t -> t.getStatus().equals(Status.POTENTIAL)).toList();
			}

			if (trade.getEndDate() != null) {
				if (trade instanceof SpecificRepoTrade specificRepoTrade) {
					generatePotentialCollateralTransfer(trade, newCollateralPayments,
							existingPotentialCollateralTransfers, collateralPaymentsToBeSaved,
							specificRepoTrade.getSecurity(), true);
				} else if (trade instanceof GCRepoTrade gcRepoTrade) {
					Set<Security> basketSec = gcRepoTrade.getGcBasket().getSecurities();
					if (!CollectionUtils.isEmpty(basketSec)) {
						for (Security sec : basketSec) {
							generatePotentialCollateralTransfer(trade, newCollateralPayments,
									existingPotentialCollateralTransfers, collateralPaymentsToBeSaved, sec, true);
						}
					}
				}
			}

			// we cancel the existing collateral transfers that are not relevant anymore
			if (existingCollateralTransfers != null) {
				for (Transfer transfer : existingCollateralTransfers) {
					if (!newCollateralPayments.contains(transfer) || !transfer.getStatus().equals(Status.POTENTIAL)) {
						transfer.setStatus(Status.CANCELED);
						collateralPaymentsToBeSaved.add((ProductTransfer) transfer);
					}
				}
			}
		} else {
			// 2nd case : allocations

			// 2.1 First, remove existing potential transfers
			List<Transfer> existingPotentialCollateralTransfers = null;
			if (existingCollateralTransfers != null) {
				existingPotentialCollateralTransfers = existingCollateralTransfers.stream()
						.filter(t -> t.getStatus().equals(Status.POTENTIAL)).toList();

				if (existingPotentialCollateralTransfers != null) {
					for (Transfer transfer : existingPotentialCollateralTransfers) {
						try {
							transferBusinessDelegate.deleteTransfer(transfer.getId());
						} catch (TradistaBusinessException tbe) {
							// Not expected here.
						}
					}
				}

			}

			// 2.2 Create or update allocation transfers
			existingCollateralTransfers = existingCollateralTransfers.stream()
					.filter(t -> !t.getStatus().equals(Status.POTENTIAL)).toList();
			Map<Transfer, Transfer> existingCollateralTransfersMap = existingCollateralTransfers.stream()
					.collect(Collectors.toMap(Function.identity(), Function.identity()));
			if (trade.getCollateralToAdd() != null) {
				for (Map.Entry<Security, Map<Book, BigDecimal>> entry : trade.getCollateralToAdd().entrySet()) {
					for (Map.Entry<Book, BigDecimal> bookEntry : entry.getValue().entrySet()) {
						ProductTransfer newCollateralPayment = new ProductTransfer(bookEntry.getKey(), entry.getKey(),
								TransferPurpose.RETURNED_COLLATERAL, trade.getEndDate(), trade);
						newCollateralPayment.setCreationDateTime(LocalDateTime.now());
						newCollateralPayment
								.setDirection(trade.isBuy() ? Transfer.Direction.RECEIVE : Transfer.Direction.PAY);
						newCollateralPayment.setQuantity(bookEntry.getValue());
						newCollateralPayment.setFixingDateTime(LocalDateTime.now());
						newCollateralPayment.setStatus(Transfer.Status.KNOWN);
						if (existingCollateralTransfers.contains(newCollateralPayment)) {
							ProductTransfer existingTransfer = (ProductTransfer) existingCollateralTransfersMap
									.get(newCollateralPayment);
							existingTransfer.setQuantity(
									existingTransfer.getQuantity().add(newCollateralPayment.getQuantity()));
							collateralPaymentsToBeSaved.add(existingTransfer);
						} else {
							collateralPaymentsToBeSaved.add(newCollateralPayment);
						}
					}
				}
			}
			if (trade.getCollateralToRemove() != null) {
				// 2.3 Delete (or decrease) return of substituted collateral at trade end date
				// and generate new return on today's date
				for (Map.Entry<Security, Map<Book, BigDecimal>> entry : trade.getCollateralToRemove().entrySet()) {
					for (Map.Entry<Book, BigDecimal> bookEntry : entry.getValue().entrySet()) {
						ProductTransfer newCollateralPayment = new ProductTransfer(bookEntry.getKey(), entry.getKey(),
								TransferPurpose.RETURNED_COLLATERAL, LocalDate.now(), trade);
						newCollateralPayment.setCreationDateTime(LocalDateTime.now());
						newCollateralPayment
								.setDirection(trade.isBuy() ? Transfer.Direction.RECEIVE : Transfer.Direction.PAY);
						newCollateralPayment.setQuantity(bookEntry.getValue());
						newCollateralPayment.setFixingDateTime(LocalDateTime.now());
						newCollateralPayment.setStatus(Transfer.Status.KNOWN);
						// If the transfer is not in existingCollateralTransfers, it means that the
						// substitution date is not the trade end date
						if (!existingCollateralTransfers.contains(newCollateralPayment)) {
							ProductTransfer substitutedCollateralTransfer = (ProductTransfer) existingCollateralTransfers
									.stream().filter(t -> t.getProduct().equals(entry.getKey())).findFirst().get();
							substitutedCollateralTransfer.setQuantity(
									substitutedCollateralTransfer.getQuantity().subtract(bookEntry.getValue()));
							if (substitutedCollateralTransfer.getQuantity().equals(BigDecimal.ZERO)) {
								try {
									transferBusinessDelegate.deleteTransfer(substitutedCollateralTransfer.getId());
								} catch (TradistaBusinessException tbe) {
									// Not expected here
								}
							} else {
								collateralPaymentsToBeSaved.add(substitutedCollateralTransfer);
							}
							collateralPaymentsToBeSaved.add(newCollateralPayment);
						}
					}
				}
			}
		}

		return collateralPaymentsToBeSaved;
	}

	private static void generatePotentialCollateralTransfer(RepoTrade trade,
			List<ProductTransfer> newCollateralPayments, List<Transfer> existingPotentialCollateralTransfers,
			List<ProductTransfer> collateralPaymentsToBeSaved, Security sec, boolean isReturn) {
		LocalDate date = null;
		TransferPurpose purpose = null;
		Transfer.Direction direction = null;
		if (!isReturn) {
			date = trade.getSettlementDate();
			purpose = TransferPurpose.COLLATERAL_SETTLEMENT;
			direction = trade.isBuy() ? Transfer.Direction.PAY : Transfer.Direction.RECEIVE;
		} else {
			date = trade.getEndDate();
			purpose = TransferPurpose.RETURNED_COLLATERAL;
			direction = trade.isBuy() ? Transfer.Direction.RECEIVE : Transfer.Direction.PAY;
		}
		ProductTransfer newCollateralPayment = new ProductTransfer(trade.getBook(), sec, purpose, date, trade);
		newCollateralPayment.setCreationDateTime(LocalDateTime.now());
		newCollateralPayment.setDirection(direction);
		newCollateralPayment.setStatus(Status.POTENTIAL);
		if (existingPotentialCollateralTransfers == null
				|| !existingPotentialCollateralTransfers.contains(newCollateralPayment)) {
			collateralPaymentsToBeSaved.add(newCollateralPayment);
		}
		newCollateralPayments.add(newCollateralPayment);

	}

	public static void fixCashTransfer(CashTransfer transfer, long quoteSetId) throws TradistaBusinessException {
		RepoTrade trade = (RepoTrade) transfer.getTrade();
		BigDecimal amount = RepoTradeUtil.getClosingLegPayment(trade, quoteSetId, new DayCountConvention(DayCountConvention.ACT_360));
		if (amount.signum() == 0) {
			// No transfer
			transferBusinessDelegate.deleteTransfer(transfer.getId());
			return;
			// TODO add a warn somewhere ?
		}
		Transfer.Direction direction;

		if (trade.isBuy()) {
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

		transfer.setDirection(direction);
		transfer.setAmount(amount);
		transferBusinessDelegate.saveTransfer(transfer);
	}

	public static void createFixingError(CashTransfer transfer, String errorMsg) throws TradistaBusinessException {
		FixingError fixingError = new FixingError();
		fixingError.setCashTransfer(transfer);
		fixingError.setErrorDate(LocalDateTime.now());
		fixingError.setErrorMessage(errorMsg);
		fixingError.setStatus(org.eclipse.tradista.core.error.model.Error.Status.UNSOLVED);
		List<FixingError> errors = new ArrayList<>(1);
		errors.add(fixingError);
		fixingErrorBusinessDelegate.saveFixingErrors(errors);
		throw new TradistaBusinessException(errorMsg);
	}

}