package org.eclipse.tradista.security.repo.transfer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.tradista.core.action.constants.ActionConstants;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.status.constants.StatusConstants;
import org.eclipse.tradista.core.trade.messaging.TradeEvent;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;
import org.eclipse.tradista.security.repo.model.RepoTrade;
import org.eclipse.tradista.security.specificrepo.model.SpecificRepoTrade;

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

public abstract class RepoTransferManager {

	protected TransferBusinessDelegate transferBusinessDelegate;

	protected RepoTransferManager() {
		transferBusinessDelegate = new TransferBusinessDelegate();
	}

	public void createRepoTransfers(TradeEvent<?> event) throws TradistaBusinessException {
		RepoTrade trade = (RepoTrade) event.getTrade();
		List<Transfer> transfersToBeSaved = new ArrayList<>();

		if (event.getOldTrade() != null) {
			RepoTrade oldTrade = (RepoTrade) event.getOldTrade();

			// flag used for transfer generation
			boolean isAllocated = trade.getStatus().getName().equals(StatusConstants.ALLOCATED);

			// flag used for partial termination
			boolean isPartiallyTerminated = event.getAppliedAction().equals(ActionConstants.PARTIALLY_TERMINATE);
			BigDecimal notionalReduction = null;
			if (isPartiallyTerminated) {
				notionalReduction = oldTrade.getAmount().subtract(trade.getAmount());
			}

			// Checking if cash transfer of opening leg should be updated
			if ((!oldTrade.getCurrency().equals(trade.getCurrency())
					|| oldTrade.getAmount().compareTo(trade.getAmount()) != 0
					|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())
					|| (oldTrade.isBuy() != trade.isBuy()) || (!oldTrade.getBook().equals(trade.getBook())))
					&& !isPartiallyTerminated) {
				List<Transfer> existingCashTransfers = transferBusinessDelegate
						.getTransfersByTradeIdAndPurpose(oldTrade.getId(), TransferPurpose.CASH_SETTLEMENT, false);
				CashTransfer existingCashTransfer = (CashTransfer) existingCashTransfers.stream().findFirst().get();
				transfersToBeSaved.addAll(RepoTransferUtil.createOrUpdateCashPaymentOpeningLeg(existingCashTransfer,
						trade, isPartiallyTerminated));
			}

			// Checking if cash transfer of closing leg should be updated
			if (!oldTrade.getCurrency().equals(trade.getCurrency())
					|| oldTrade.getAmount().compareTo(trade.getAmount()) != 0
					|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())
					|| !Objects.equals(oldTrade.getEndDate(), trade.getEndDate()) || (oldTrade.isBuy() != trade.isBuy())
					|| (!oldTrade.getBook().equals(trade.getBook())) || isPartiallyTerminated) {
				List<Transfer> existingCashTransfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(
						oldTrade.getId(), TransferPurpose.RETURNED_CASH_PLUS_INTEREST, false);
				transfersToBeSaved.addAll(RepoTransferUtil.createOrUpdateCashPaymentClosingLeg(existingCashTransfers,
						trade, notionalReduction));
			}

			// Checking if collateral payments should be updated
			if (((oldTrade.getEndDate() == null && trade.getEndDate() != null)
					|| !oldTrade.getSettlementDate().isEqual(trade.getSettlementDate())
					|| (!isSameSecurity(trade, oldTrade)) || (oldTrade.isBuy() != trade.isBuy())
					|| (!oldTrade.getBook().equals(trade.getBook()))) || (isAllocated)) {
				List<Transfer> existingCollateralTransfers = transferBusinessDelegate.getTransfersByTradeIdAndPurpose(
						oldTrade.getId(), TransferPurpose.COLLATERAL_SETTLEMENT, false);
				transfersToBeSaved.addAll(RepoTransferUtil.createOrUpdateCollateralPayment(existingCollateralTransfers,
						trade, isAllocated));
			}

			// Checking if returned Collateral payments should be updated
			if ((!Objects.equals(oldTrade.getEndDate(), trade.getEndDate()) || (!isSameSecurity(trade, oldTrade))
					|| (oldTrade.isBuy() != trade.isBuy()) || (!oldTrade.getBook().equals(trade.getBook())))
					|| (isAllocated)) {
				List<Transfer> existingReturnedCollateralTransfers = transferBusinessDelegate
						.getTransfersByTradeIdAndPurpose(oldTrade.getId(), TransferPurpose.RETURNED_COLLATERAL, false);
				transfersToBeSaved.addAll(RepoTransferUtil.createOrUpdateReturnedCollateralPayment(
						existingReturnedCollateralTransfers, trade, isAllocated));
			}

		} else {
			transfersToBeSaved.addAll(RepoTransferUtil.createOrUpdateCashPaymentOpeningLeg(null, trade, false));
			transfersToBeSaved.addAll(RepoTransferUtil.createOrUpdateCashPaymentClosingLeg(null, trade, null));
			transfersToBeSaved.addAll(RepoTransferUtil.createOrUpdateCollateralPayment(null, trade, false));
			transfersToBeSaved.addAll(RepoTransferUtil.createOrUpdateReturnedCollateralPayment(null, trade, false));
		}

		if (!transfersToBeSaved.isEmpty()) {
			transferBusinessDelegate.saveTransfers(transfersToBeSaved);
		}
	}

	public boolean isSameSecurity(RepoTrade trade, RepoTrade oldTrade) {
		if (trade instanceof SpecificRepoTrade specificRepoTrade
				&& oldTrade instanceof SpecificRepoTrade oldSpecificRepoTrade) {
			return Objects.equals(oldSpecificRepoTrade.getSecurity(), specificRepoTrade.getSecurity());
		}
		if (trade instanceof GCRepoTrade gcRepoTrade && oldTrade instanceof GCRepoTrade oldGcRepoTrade) {
			return Objects.equals(oldGcRepoTrade.getGcBasket().getSecurities(),
					gcRepoTrade.getGcBasket().getSecurities());
		}
		// Not expected to arrive there..
		return false;
	}

}