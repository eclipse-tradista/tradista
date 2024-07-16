package finance.tradista.security.gcrepo.workflow.guard;

import finance.tradista.core.common.exception.TradistaBusinessException;
import finance.tradista.flow.model.Guard;
import finance.tradista.security.gcrepo.service.GCRepoTradeBusinessDelegate;
import finance.tradista.security.gcrepo.workflow.mapping.GCRepoTrade;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

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

@Entity
public class IsPartiallyTerminated extends Guard<GCRepoTrade> {

	@Transient
	private GCRepoTradeBusinessDelegate gcRepoTradeBusinessDelegate;

	private static final long serialVersionUID = 1L;

	public IsPartiallyTerminated() {
		gcRepoTradeBusinessDelegate = new GCRepoTradeBusinessDelegate();
		setPredicate(trade -> {
			// Get the previous state of the trade
			finance.tradista.security.gcrepo.model.GCRepoTrade oldTrade = gcRepoTradeBusinessDelegate
					.getGCRepoTradeById(trade.getId());
			// The guard returns true only if the notional has been reduced.
			boolean isPartiallyTerminated = (trade.getCashAmount().compareTo(oldTrade.getAmount()) == -1);
			if (!isPartiallyTerminated) {
				throw new TradistaBusinessException("The cash amount has not been reduced.");
			}
			return isPartiallyTerminated;
		});
	}

}