/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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
package org.eclipse.tradista.ai.agent.service;

import java.math.BigDecimal;
import java.util.Map;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.security.common.model.Security;
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;

public class CollateralOptimizationBusinessDelegate {

	private CollateralOptimizationService collateralOptimizationService;

	public CollateralOptimizationBusinessDelegate() {
		collateralOptimizationService = TradistaServiceLocator.getInstance()
				.getService("CollateralOptimizationService", CollateralOptimizationService.class);
	}

	public Map<Security, BigDecimal> optimizeCollateral(GCRepoTrade trade, BigDecimal exposure,
			Map<Security, BigDecimal> availableQuantities, boolean considerBasel3LiquidityRatios,
			boolean excludeBondsPayingCoupons) throws TradistaBusinessException {
		return SecurityUtil.runEx(() -> collateralOptimizationService.optimizeCollateral(trade, exposure,
				availableQuantities, considerBasel3LiquidityRatios, excludeBondsPayingCoupons));
	}

}
