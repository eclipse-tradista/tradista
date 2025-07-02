package org.eclipse.tradista.core.mapping.service;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.mapping.model.InterfaceMappingSet;
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
	public String getMappingValue(String interfaceName, MappingType mappingType,
			InterfaceMappingSet.Direction direction, String value) {
		String mappedValue = MappingSQL.getMappingValue(interfaceName, mappingType, direction, value);
		if (StringUtils.isEmpty(mappedValue) && !StringUtils.isEmpty(interfaceName)) {
			// if mapping is not found, we try to search for a global mapping, ie applying
			// to all interfaces
			mappedValue = MappingSQL.getMappingValue(null, mappingType, direction, value);
		}
		return mappedValue;
	}

	@Override
	public String getOriginalValue(String interfaceName, MappingType mappingType,
			InterfaceMappingSet.Direction direction, String mappedValue) {
		String value = MappingSQL.getOriginalValue(interfaceName, mappingType, direction, mappedValue);
		if (StringUtils.isEmpty(value) && !StringUtils.isEmpty(interfaceName)) {
			// if mapping is not found, we try to search for a global mapping, ie applying
			// to all interfaces
			value = MappingSQL.getOriginalValue(null, mappingType, direction, mappedValue);
		}
		return value;
	}

	@Override
	public long saveInterfaceMappingSet(InterfaceMappingSet ims) {
		return MappingSQL.saveInterfaceMappingSet(ims);
	}

	@Override
	public InterfaceMappingSet getInterfaceMappingSet(String interfaceName, MappingType mappingType,
			InterfaceMappingSet.Direction direction) {
		return MappingSQL.getInterfaceMappingSet(interfaceName, mappingType, direction);
	}

}