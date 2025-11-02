package org.eclipse.tradista.core.importer.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.importer.model.Importer;
import org.eclipse.tradista.core.message.model.ImportError;
import org.eclipse.tradista.core.message.model.IncomingMessage;
import org.eclipse.tradista.core.message.service.ImportErrorBusinessDelegate;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

/********************************************************************************
 * Copyright (c) 2025 Olivier Asuncion
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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class ImporterServiceBean implements ImporterService {

	@EJB
	private LocalImporterConfigurationService localImporterConfigurationService;

	private ImportErrorBusinessDelegate importErrorBusinessDelegate;

	@PostConstruct
	private void init() {
		importErrorBusinessDelegate = new ImportErrorBusinessDelegate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void mapIncomingMessage(IncomingMessage msg) throws TradistaBusinessException {
		// Load the importer
		Importer<Object> importer = (Importer<Object>) localImporterConfigurationService
				.getImporterByName(msg.getInterfaceName());
		ImportError existingMappingError = importErrorBusinessDelegate.getImportError(msg.getId(),
				ImportError.ImportErrorType.MAPPING);
		;
		try {
			// Build the message object from a string
			Object msgObject = importer.buildMessage(msg.getContent());
			// Apply the mapping
			importer.processMessage(msgObject, msg);
			// If ok, solve the existing mapping error if any
			if (existingMappingError != null) {
				existingMappingError.solve();
			}
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			// Update the existing mapping error if any, otherwise create a new one.
			if (existingMappingError != null) {
				existingMappingError.update(te.getMessage());
			}
		}
		if (existingMappingError != null) {
			importErrorBusinessDelegate.saveImportError(existingMappingError);
		}
	}

}