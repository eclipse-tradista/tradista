package org.eclipse.tradista.core.importer.service;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.tradista.core.marketdata.service.ImporterConfigurationService;
import org.eclipse.tradista.core.marketdata.service.ImporterInformationService;
import org.jboss.ejb3.annotation.SecurityDomain;

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
public class ImporterInformationServiceBean implements ImporterInformationService {

	@EJB
	private ImporterConfigurationService importerConfigurationService;

	@Override
	public Map<String, String> getImporterModuleVersions() {
		Map<String, String> map = null;
		Set<String> modules = importerConfigurationService.getModules();
		if (modules != null && !modules.isEmpty()) {
			map = new TreeMap<>();
			for (String m : modules) {
				map.put(m,
						this.getClass().getClassLoader()
								.getDefinedPackage("org.eclipse.tradista.core.importer." + m.toLowerCase())
								.getImplementationVersion());
			}
		}
		return map;
	}

}