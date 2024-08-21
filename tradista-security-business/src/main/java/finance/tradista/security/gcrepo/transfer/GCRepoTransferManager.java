package finance.tradista.security.gcrepo.transfer;

import finance.tradista.core.common.exception.TradistaBusinessException;
import finance.tradista.core.transfer.model.CashTransfer;
import finance.tradista.core.transfer.model.TransferManager;
import finance.tradista.security.gcrepo.messaging.GCRepoTradeEvent;
import finance.tradista.security.repo.transfer.RepoTransferManager;
import finance.tradista.security.repo.transfer.RepoTransferUtil;

/********************************************************************************
 * Copyright (c) 2023 Olivier Asuncion
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

public class GCRepoTransferManager extends RepoTransferManager implements TransferManager<GCRepoTradeEvent> {

	@Override
	public void createTransfers(GCRepoTradeEvent event) throws TradistaBusinessException {
		createRepoTransfers(event);
	}

	@Override
	public void fixCashTransfer(CashTransfer transfer, long quoteSetId) throws TradistaBusinessException {
		RepoTransferUtil.fixCashTransfer(transfer, quoteSetId);
	}

}