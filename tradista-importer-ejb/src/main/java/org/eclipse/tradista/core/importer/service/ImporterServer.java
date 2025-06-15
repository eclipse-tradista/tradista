package org.eclipse.tradista.core.importer.service;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.tradista.core.importer.model.Importer;
import org.eclipse.tradista.core.marketdata.service.ImporterConfigurationService;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

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
@Startup
@Singleton
public class ImporterServer {

	@EJB
	private ImporterConfigurationService importerConfigurationService;

	@PostConstruct
	public void init() {
		Set<Importer<?>> importers = importerConfigurationService.getImporters();

		if (importers == null || importers.isEmpty()) {
			// No importer, we add a log and exit this method.
			// TODO: Add logs
			return;
		}
		ExecutorService executor = Executors.newScheduledThreadPool(importers.size());
		for (Importer<?> importer : importers) {
			executor.submit(importer);
		}
	}

}