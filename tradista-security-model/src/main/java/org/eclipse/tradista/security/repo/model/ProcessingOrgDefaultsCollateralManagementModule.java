package org.eclipse.tradista.security.repo.model;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.processingorgdefaults.model.ProcessingOrgDefaultsModule;

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

public class ProcessingOrgDefaultsCollateralManagementModule extends ProcessingOrgDefaultsModule {

	private static final long serialVersionUID = -6779143569735077767L;

	public static final String COLLATERAL_MANAGEMENT = "Collateral Management";

	private QuoteSet quoteSet;

	private AllocationConfiguration allocationConfiguration;

	public ProcessingOrgDefaultsCollateralManagementModule() {
		name = COLLATERAL_MANAGEMENT;
	}

	public QuoteSet getQuoteSet() {
		return TradistaModelUtil.clone(quoteSet);
	}

	public void setQuoteSet(QuoteSet qs) {
		quoteSet = qs;
	}

	public AllocationConfiguration getAllocationConfiguration() {
		return allocationConfiguration;
	}

	public void setAllocationConfiguration(AllocationConfiguration allocationConfiguration) {
		this.allocationConfiguration = allocationConfiguration;
	}

	@Override
	public ProcessingOrgDefaultsCollateralManagementModule clone() {
		ProcessingOrgDefaultsCollateralManagementModule module = (ProcessingOrgDefaultsCollateralManagementModule) super.clone();
		module.quoteSet = TradistaModelUtil.clone(quoteSet);
		module.allocationConfiguration = TradistaModelUtil.clone(allocationConfiguration);
		return module;
	}

}