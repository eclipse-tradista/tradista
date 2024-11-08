package org.eclipse.tradista.security.repo.workflow.condition;

import java.math.BigDecimal;

import org.eclipse.tradista.flow.model.Condition;
import org.eclipse.tradista.security.repo.pricer.RepoPricerUtil;
import org.eclipse.tradista.security.repo.workflow.mapping.RepoTrade;
import jakarta.persistence.Entity;

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

@Entity
public class IsAllocated extends Condition<RepoTrade> {

	private static final long serialVersionUID = -1790346124051863865L;

	public IsAllocated() {
		setFunction(trade -> {
			// Calculate the total MTM value of the collateral
			BigDecimal collateralValue = RepoPricerUtil.getPendingCollateralValue(trade.getOriginalRepoTrade(),
					trade.getCollateralToAdd(), trade.getCollateralToRemove());

			// Calculate the current value of cash
			BigDecimal cashValue = RepoPricerUtil.getCurrentCashValue(trade.getOriginalRepoTrade());

			// Compare collateral and cash values

			return collateralValue.compareTo(cashValue) == -1 ? 1 : 2;

		});
	}

}