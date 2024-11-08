package org.eclipse.tradista.core.transfer.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.trade.messaging.TradeEvent;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.ProductTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.Transfer.Direction;
import org.eclipse.tradista.core.transfer.model.Transfer.Status;
import org.eclipse.tradista.core.transfer.model.Transfer.Type;
import org.eclipse.tradista.core.transfer.service.TransferService;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;

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

public class TransferBusinessDelegate {

	private TransferService transferService;

	public TransferBusinessDelegate() {
		transferService = TradistaServiceLocator.getInstance().getTransferService();
	}

	public Transfer getTransferById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The transfer id must be positive.");
		}
		return SecurityUtil.run(() -> transferService.getTransferById(id));
	}

	public Set<Transfer> getAllTransfers() {
		return SecurityUtil.run(() -> transferService.getAllTransfers());
	}

	public long saveTransfer(Transfer transfer) throws TradistaBusinessException {
		if (transfer == null) {
			throw new TradistaBusinessException("The transfer cannot be null.");
		}
		StringBuilder errMsg = new StringBuilder();
		validateTransfer(transfer, errMsg);
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil.run(() -> transferService.saveTransfer(transfer));
	}

	public void saveTransfers(List<Transfer> transfers) throws TradistaBusinessException {
		if (transfers == null || transfers.isEmpty()) {
			throw new TradistaBusinessException("The transfers list cannot be null or empty.");
		}
		StringBuilder errMsg = new StringBuilder();
		for (Transfer transfer : transfers) {
			validateTransfer(transfer, errMsg);
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		SecurityUtil.run(() -> transferService.saveTransfers(transfers));
	}

	private void validateTransfer(Transfer transfer, StringBuilder errMsg) {
		if (transfer.getType() == null) {
			errMsg.append("The transfer type cannot be null.");
		}
		if (transfer.getStatus() == null) {
			errMsg.append("The transfer status cannot be null.");
		} else {
			if (transfer.getDirection() == null && transfer.getStatus().equals(Transfer.Status.KNOWN)) {
				errMsg.append(String.format("The transfer direction cannot be null when the transfer status is  %s.",
						Transfer.Status.KNOWN));
			}
			if (transfer instanceof CashTransfer) {
				if (((CashTransfer) transfer).getAmount() == null
						&& transfer.getStatus().equals(Transfer.Status.KNOWN)) {
					errMsg.append(String.format("The transfer amount cannot be null when the transfer status is  %s.",
							Transfer.Status.KNOWN));
				}
			}
			if (transfer instanceof ProductTransfer) {
				if (((ProductTransfer) transfer).getQuantity() == null
						&& transfer.getStatus().equals(Transfer.Status.KNOWN)) {
					errMsg.append(String.format("The transfer quantity cannot be null when the transfer status is  %s.",
							Transfer.Status.KNOWN));
				}
			}
		}
		if (transfer instanceof CashTransfer) {
			if (((CashTransfer) transfer).getAmount() != null && transfer.getFixingDateTime() == null) {
				errMsg.append("The transfer amount must be null when the fixing date is null.");
			}
		}
		if (transfer instanceof ProductTransfer) {
			if (((ProductTransfer) transfer).getQuantity() != null && transfer.getFixingDateTime() == null) {
				errMsg.append("The transfer quantity must be null when the fixing date is null.");
			}
		}
		if (transfer.getProduct() == null && transfer.getTrade() == null) {
			errMsg.append("The transfer trade and product cannot both be null.");
		}
	}

	public void createTransfers(TradeEvent<?> event) throws TradistaBusinessException {
		if (event == null) {
			throw new TradistaBusinessException("The TradeEvent cannot be null.");
		}

		StringBuffer errMsg = new StringBuffer();

		if (event.getTrade() == null) {
			errMsg.append(String.format("The TradeEvent trade cannot be null.%n"));
		}

		// TODO Should we validate the trade and the old trade here ?

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

		SecurityUtil.runEx(() -> transferService.createTransfers(event));

	}

	public List<Transfer> getTransfersByTradeIdAndPurpose(long tradeId, TransferPurpose purpose, boolean includeCancel)
			throws TradistaBusinessException {
		StringBuffer errMsg = new StringBuffer();

		if (purpose == null) {
			errMsg.append("The transferServiceRequest purpose cannot be null.%n");
		}

		if (tradeId <= 0) {
			errMsg.append("The transferServiceRequest trade id must be positive.%n");
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

		return SecurityUtil.run(() -> transferService.getTransfersByTradeIdAndPurpose(tradeId, purpose, includeCancel));

	}

	public List<Transfer> getTransfersByTradeId(long tradeId) throws TradistaBusinessException {
		StringBuffer errMsg = new StringBuffer();

		if (tradeId <= 0) {
			errMsg.append("The trade id must be positive.%n");
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}

		return SecurityUtil.run(() -> transferService.getTransfersByTradeId(tradeId));

	}

	public List<CashTransfer> getCashTransfersByProductIdAndStartDate(long productId, LocalDate startDate)
			throws TradistaBusinessException {

		if (productId <= 0) {
			throw new TradistaBusinessException("The product id must be positive.");
		}

		return SecurityUtil.run(() -> transferService.getCashTransfersByProductIdAndStartDate(productId, startDate));
	}

	public void deleteTransfer(long transferId) throws TradistaBusinessException {
		if (transferId <= 0) {
			throw new TradistaBusinessException("The transfer id must be positive.");
		}

		SecurityUtil.run(() -> transferService.deleteTransfer(transferId));
	}

	public List<Transfer> getTransfers(Type type, Status status, Direction direction, TransferPurpose purpose,
			long tradeId, long productId, long bookId, long currencyId, LocalDate startFixingDate,
			LocalDate endFixingDate, LocalDate startSettlementDate, LocalDate endSettlementDate,
			LocalDate startCreationDate, LocalDate endCreationDate) throws TradistaBusinessException {
		StringBuilder errorMsg = new StringBuilder();
		if (startCreationDate != null && endCreationDate != null) {
			if (endCreationDate.isBefore(startCreationDate)) {
				errorMsg.append(String.format("'To' creation date cannot be before 'From' creation date.%n"));
			}
		}
		if (startFixingDate != null && endFixingDate != null) {
			if (endFixingDate.isBefore(startFixingDate)) {
				errorMsg.append("'To' fixing date cannot be before 'From' fixing date.");
			}
		}
		if (startSettlementDate != null && endSettlementDate != null) {
			if (endSettlementDate.isBefore(startSettlementDate)) {
				errorMsg.append("'To' settlement date cannot be before 'From' settlement date.");
			}
		}
		if (errorMsg.length() > 0) {
			throw new TradistaBusinessException(errorMsg.toString());
		}
		return SecurityUtil.run(() -> transferService.getTransfers(type, status, direction, purpose, tradeId, productId,
				bookId, currencyId, startFixingDate, endFixingDate, startSettlementDate, endSettlementDate,
				startCreationDate, endCreationDate));
	}

	public void fixCashTransfers(long quoteSetId) throws TradistaBusinessException {
		if (quoteSetId <= 0) {
			throw new TradistaBusinessException("The quote set id must be positive.");
		}
		SecurityUtil.runEx(() -> transferService.fixCashTransfers(quoteSetId));
	}

}