package org.eclipse.tradista.core.transfer.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import jakarta.ejb.Remote;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.trade.messaging.TradeEvent;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.Transfer.Direction;
import org.eclipse.tradista.core.transfer.model.Transfer.Status;
import org.eclipse.tradista.core.transfer.model.Transfer.Type;
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

@Remote
public interface TransferService {

	Set<Transfer> getAllTransfers();

	Transfer getTransferById(long id);

	long saveTransfer(Transfer transfer);

	void saveTransfers(List<Transfer> transfers);

	void createTransfers(TradeEvent<?> message) throws TradistaBusinessException;

	List<Transfer> getTransfersByTradeIdAndPurpose(long tradeId, TransferPurpose purpose, boolean includeCancel);

	List<Transfer> getTransfersByTradeId(long tradeId);

	List<CashTransfer> getCashTransfersByProductIdAndStartDate(long productId, LocalDate startDate);

	void deleteTransfer(long transferId);

	List<Transfer> getTransfers(Type type, Status status, Direction direction, TransferPurpose purpose, long tradeId,
			long productId, long bookId, long currencyId, LocalDate startFixingDate, LocalDate endFixingDate,
			LocalDate startSettlementDate, LocalDate endSettlementDate, LocalDate startCreationDate,
			LocalDate endCreationDate);

	void fixCashTransfers(long quoteSetId) throws TradistaBusinessException;
}