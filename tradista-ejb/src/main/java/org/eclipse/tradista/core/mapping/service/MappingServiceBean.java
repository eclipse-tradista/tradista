package org.eclipse.tradista.core.mapping.service;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.mapping.model.MappingType;
import org.eclipse.tradista.core.mapping.persistence.MappingSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
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
public class MappingServiceBean implements MappingService {

	@Override
	public String getMappingValue(String importerName, MappingType mappingType, String value) {
		String mappedValue = MappingSQL.getMappingValue(importerName, mappingType, value);
		if (StringUtils.isEmpty(mappedValue) && !StringUtils.isEmpty(importerName)) {
			// if mapping is not found, we try to search for a global mapping, ie applying
			// to all importers
			mappedValue = MappingSQL.getMappingValue(null, mappingType, value);
		}
		return mappedValue;
	}

	@Override
	public String getOriginalValue(String importerName, MappingType mappingType, String mappedValue) {
		String value = MappingSQL.getOriginalValue(importerName, mappingType, mappedValue);
		if (StringUtils.isEmpty(value) && !StringUtils.isEmpty(importerName)) {
			// if mapping is not found, we try to search for a global mapping, ie applying
			// to all importers
			value = MappingSQL.getOriginalValue(null, mappingType, mappedValue);
		}
		return value;
	}

}