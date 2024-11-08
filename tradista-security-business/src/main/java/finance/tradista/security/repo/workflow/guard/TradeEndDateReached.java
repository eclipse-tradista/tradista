package org.eclipse.tradista.security.repo.workflow.guard;

import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.flow.model.Guard;
import org.eclipse.tradista.security.repo.workflow.mapping.RepoTrade;
import jakarta.persistence.Entity;

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
public class TradeEndDateReached extends Guard<RepoTrade> {

	private static final long serialVersionUID = 5851944303197124273L;

	public TradeEndDateReached() {
		setPredicate(trade -> {
			boolean isTradeEndDateReached = !LocalDate.now().isBefore(trade.getEndDate());
			if (!isTradeEndDateReached) {
				throw new TradistaBusinessException("The trade end date has not been reached.");
			}
			return isTradeEndDateReached;
		});
	}

}