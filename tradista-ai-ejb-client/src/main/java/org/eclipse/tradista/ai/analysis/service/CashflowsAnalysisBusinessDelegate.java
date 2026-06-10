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
package org.eclipse.tradista.ai.analysis.service;

import java.util.List;

import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.springframework.util.CollectionUtils;

public class CashflowsAnalysisBusinessDelegate {

	private CashflowsAnalysisService cashflowsAnalysisService;

	public CashflowsAnalysisBusinessDelegate() {
		cashflowsAnalysisService = TradistaServiceLocator.getInstance().getCashflowsAnalysisService();
	}

	public String analyseCashflows(List<CashFlow> tMinusOneCashflows, List<CashFlow> tCashflows)
			throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (CollectionUtils.isEmpty(tMinusOneCashflows)) {
			errMsg.append(String.format("T-1 Cashflows are mandatory for the analysis.%n"));
		}
		if (CollectionUtils.isEmpty(tCashflows)) {
			errMsg.append("Cashflows are mandatory.");
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil.runEx(() -> cashflowsAnalysisService.analyseCashflows(tMinusOneCashflows, tCashflows));
	}

}