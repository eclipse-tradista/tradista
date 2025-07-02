package org.eclipse.tradista.core.importer.service;

import java.util.Map;

import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.importer.service.ImporterInformationService;

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

public class ImporterInformationBusinessDelegate {

	private ImporterInformationService importerInformationService;

	public ImporterInformationBusinessDelegate() {
		importerInformationService = TradistaServiceLocator.getInstance().getImporterInformationService();
	}

	public Map<String, String> getImporterModuleVersions() {
		return SecurityUtil.run(() -> importerInformationService.getImporterModuleVersions());
	}

}