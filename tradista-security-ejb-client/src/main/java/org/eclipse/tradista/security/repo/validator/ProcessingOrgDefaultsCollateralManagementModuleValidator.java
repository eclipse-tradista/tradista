package org.eclipse.tradista.security.repo.validator;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;
import org.eclipse.tradista.core.processingorgdefaults.model.ProcessingOrgDefaultsModule;
import org.eclipse.tradista.core.processingorgdefaults.service.ProcessingOrgDefaultsModuleValidator;
import org.eclipse.tradista.security.repo.model.AllocationConfiguration;
import org.eclipse.tradista.security.repo.model.ProcessingOrgDefaultsCollateralManagementModule;
import org.eclipse.tradista.security.repo.service.AllocationConfigurationBusinessDelegate;

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

public class ProcessingOrgDefaultsCollateralManagementModuleValidator implements ProcessingOrgDefaultsModuleValidator {

	private QuoteBusinessDelegate quoteBusinessDelegate;

	private AllocationConfigurationBusinessDelegate allocationConfigurationBusinessDelegate;

	public ProcessingOrgDefaultsCollateralManagementModuleValidator() {
		quoteBusinessDelegate = new QuoteBusinessDelegate();
		allocationConfigurationBusinessDelegate = new AllocationConfigurationBusinessDelegate();
	}

	@Override
	public void validateModule(ProcessingOrgDefaultsModule module, LegalEntity po) throws TradistaBusinessException {
		if (po == null) {
			throw new TradistaBusinessException("The PO is mandatory.");
		}
		StringBuilder errMsg = new StringBuilder();
		QuoteSet qs = ((ProcessingOrgDefaultsCollateralManagementModule) module).getQuoteSet();
		AllocationConfiguration allocConfig = ((ProcessingOrgDefaultsCollateralManagementModule) module)
				.getAllocationConfiguration();
		if (qs != null) {
			if (qs.getProcessingOrg() != null && !qs.getProcessingOrg().equals(po)) {
				errMsg.append(String.format(
						"The Processing Org Defaults's PO and the Collateral Quote Set %s's PO should be the same.%n",
						qs));
			}
			if (qs.getProcessingOrg() == null) {
				errMsg.append(String.format(
						"The Collateral Quote Set %s is a global one, it cannot be added to this Processing Org Defaults.%n",
						qs));
			}
		}
		if (allocConfig != null) {
			if (!allocConfig.getProcessingOrg().equals(po)) {
				errMsg.append(String.format(
						"the Processing Org Defaults's PO and the Allocation Configuration %s's PO should be the same.%n",
						allocConfig));
			}
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

	@Override
	public void checkAccess(ProcessingOrgDefaultsModule module, StringBuilder errMsg) throws TradistaBusinessException {
		QuoteSet qs = ((ProcessingOrgDefaultsCollateralManagementModule) module).getQuoteSet();
		AllocationConfiguration allocConfig = ((ProcessingOrgDefaultsCollateralManagementModule) module)
				.getAllocationConfiguration();
		if (qs != null) {
			QuoteSet checkQs = quoteBusinessDelegate.getQuoteSetById(qs.getId());
			if (checkQs == null) {
				errMsg.append(String.format("the Collateral Quote Set %s was not found.%n", qs));
			}
		}
		if (allocConfig != null) {
			AllocationConfiguration checkAc = allocationConfigurationBusinessDelegate
					.getAllocationConfigurationById(allocConfig.getId());
			if (checkAc == null) {
				errMsg.append(String.format("the Allocation Configuration %s was not found.%n", allocConfig));
			}
		}
	}

}