package org.eclipse.tradista.core.mapping.service;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.mapping.model.MappingType;

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

public class MappingBusinessDelegate {

	private MappingService mappingService;

	public MappingBusinessDelegate() {
		mappingService = TradistaServiceLocator.getInstance().getMappingService();
	}

	public String getMappingValue(String importerName, MappingType mappingType, String value) {
		StringBuilder errMsg = new StringBuilder();
		if (mappingType == null) {
			errMsg.append(String.format("Mapping type is mandatory.%n"));
		}
		if (StringUtils.isEmpty(value)) {
			errMsg.append("Value is mandatory.");
		}
		return mappingService.getMappingValue(importerName, mappingType, value);
	}

	public String getOriginalValue(String importerName, MappingType mappingType, String value) {
		StringBuilder errMsg = new StringBuilder();
		if (mappingType == null) {
			errMsg.append(String.format("Mapping type is mandatory.%n"));
		}
		if (StringUtils.isEmpty(value)) {
			errMsg.append("Value is mandatory.");
		}
		return mappingService.getOriginalValue(importerName, mappingType, value);
	}

}