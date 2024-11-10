package org.eclipse.tradista.security.specificrepo.transfer;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.TransferManager;
import org.eclipse.tradista.security.repo.transfer.RepoTransferManager;
import org.eclipse.tradista.security.repo.transfer.RepoTransferUtil;
import org.eclipse.tradista.security.specificrepo.messaging.SpecificRepoTradeEvent;

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

public class SpecificRepoTransferManager extends RepoTransferManager
		implements TransferManager<SpecificRepoTradeEvent> {

	@Override
	public void createTransfers(SpecificRepoTradeEvent event) throws TradistaBusinessException {
		createRepoTransfers(event);
	}

	@Override
	public void fixCashTransfer(CashTransfer transfer, long quoteSetId) throws TradistaBusinessException {
		RepoTransferUtil.fixCashTransfer(transfer, quoteSetId);
	}

}