package org.eclipse.tradista.fx.common.transfer;

import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.trade.messaging.TradeEvent;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.TransferManager;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;

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

public class AbstractFXTransferManager<X extends TradeEvent<?>> implements TransferManager<TradeEvent<?>> {

	protected TransferBusinessDelegate transferBusinessDelegate;

	public AbstractFXTransferManager() {
		transferBusinessDelegate = new TransferBusinessDelegate();
	}

	@Override
	public void createTransfers(TradeEvent<?> message) throws TradistaBusinessException {

		// If the trade is not new, we cancel the former transfers.
		if (message.getTrade().getId() != 0) {
			List<Transfer> transfers = transferBusinessDelegate.getTransfersByTradeId(message.getTrade().getId());
			// if the transfers's list is null or empty, it is not normal, but the process
			// should
			// continue.
			if (transfers == null || transfers.isEmpty()) {
				// TODO logs + Errors viewable in the error report ?
			} else {
				for (Transfer transfer : transfers) {
					transfer.setStatus(Transfer.Status.CANCELED);
					transferBusinessDelegate.saveTransfer(transfer);
				}
			}
		}
	}

	@Override
	public void fixCashTransfer(CashTransfer transfer, long quoteSetId) {
	}

}