package org.eclipse.tradista.core.importer.service;

import java.util.SortedSet;

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

public class ImporterConfiguration {

	private SortedSet<String> modules;

	private SortedSet<String> importerNames;

	public SortedSet<String> getModules() {
		return modules;
	}

	public void setModules(SortedSet<String> modules) {
		this.modules = modules;
	}

	public SortedSet<String> getImporterNames() {
		return importerNames;
	}

	public void setImporterNames(SortedSet<String> importerNames) {
		this.importerNames = importerNames;
	}

}
