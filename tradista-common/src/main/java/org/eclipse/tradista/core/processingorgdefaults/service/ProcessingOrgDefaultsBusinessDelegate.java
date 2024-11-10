package org.eclipse.tradista.core.processingorgdefaults.service;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.common.util.TradistaUtil;
import org.eclipse.tradista.core.processingorgdefaults.model.ProcessingOrgDefaults;
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

public class ProcessingOrgDefaultsBusinessDelegate {

	private ProcessingOrgDefaultsService poDefaultsService;

	private Map<String, ProcessingOrgDefaultsModuleValidator> validators;

	public ProcessingOrgDefaultsBusinessDelegate() {
		poDefaultsService = TradistaServiceLocator.getInstance().getProcessingOrgDefaultsService();
		validators = new HashMap<>();
		ProcessingOrgDefaultsModuleValidator validator = null;
		try {
			validator = TradistaUtil.getInstance(ProcessingOrgDefaultsModuleValidator.class,
					"org.eclipse.tradista.security.repo.validator.ProcessingOrgDefaultsCollateralManagementModuleValidator");
			validators.put("org.eclipse.tradista.security.repo.model.ProcessingOrgDefaultsCollateralManagementModule",
					validator);
		} catch (TradistaTechnicalException tte) {
			// TODO Add log info
		}
	}

	public ProcessingOrgDefaults getProcessingOrgDefaultsByPoId(long poId) throws TradistaBusinessException {
		if (poId <= 0) {
			throw new TradistaBusinessException("The po id must be positive.");
		}
		return SecurityUtil.run(() -> poDefaultsService.getProcessingOrgDefaultsByPoId(poId));
	}

	public long saveProcessingOrgDefaults(ProcessingOrgDefaults poDefaults) throws TradistaBusinessException {
		if (poDefaults == null) {
			throw new TradistaBusinessException("The Processing Org Defaults cannot be null.");
		}
		if (poDefaults.getProcessingOrg() == null) {
			throw new TradistaBusinessException("The Processing Org Defaults's PO cannot be null.");
		}
		StringBuilder errMsg = new StringBuilder();
		if (poDefaults.getModules() != null && !poDefaults.getModules().isEmpty()) {
			for (ProcessingOrgDefaultsModule module : poDefaults.getModules()) {
				ProcessingOrgDefaultsModuleValidator validator = getValidator(module);
				try {
					validator.validateModule(module, poDefaults.getProcessingOrg());
				} catch (TradistaBusinessException abe) {
					errMsg.append(abe.getMessage());
				}
			}
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil.runEx(() -> poDefaultsService.saveProcessingOrgDefaults(poDefaults));
	}

	ProcessingOrgDefaultsModuleValidator getValidator(ProcessingOrgDefaultsModule module) {
		return validators.get(module.getClass().getName());
	}

}