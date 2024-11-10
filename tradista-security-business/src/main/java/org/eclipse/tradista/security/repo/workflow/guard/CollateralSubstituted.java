package org.eclipse.tradista.security.repo.workflow.guard;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.security.repo.workflow.mapping.RepoTrade;

import finance.tradista.flow.model.Guard;
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
public class CollateralSubstituted extends Guard<RepoTrade> {

	private static final long serialVersionUID = 1L;

	public CollateralSubstituted() {
		setPredicate(trade -> {
			StringBuilder errMsg = new StringBuilder();
			boolean collateralAdded = trade.getCollateralToAdd() != null && !trade.getCollateralToAdd().isEmpty();
			boolean collateralRemoved = trade.getCollateralToRemove() != null
					&& !trade.getCollateralToRemove().isEmpty();
			if (!collateralAdded) {
				errMsg.append(String.format("No collateral has been added.%n"));
			}
			if (!collateralRemoved) {
				errMsg.append("No collateral has been removed.");
			}
			if (!errMsg.isEmpty()) {
				throw new TradistaBusinessException(errMsg.toString());
			}
			return collateralAdded && collateralRemoved;
		});
	}

}