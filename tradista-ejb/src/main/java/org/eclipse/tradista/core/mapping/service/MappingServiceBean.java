package org.eclipse.tradista.core.mapping.service;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.mapping.model.InterfaceMappingSet;
import org.eclipse.tradista.core.mapping.model.MappingType;
import org.eclipse.tradista.core.mapping.persistence.MappingSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

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

	@Interceptors(MappingAuthorizationFilteringInterceptor.class)
	@Override
	public String getMappingValue(String interfaceName, MappingType mappingType,
			InterfaceMappingSet.Direction direction, String value, long poId) {
		String mappedValue = MappingSQL.getMappingValue(interfaceName, mappingType, direction, value, poId);
		if (StringUtils.isEmpty(mappedValue) && !StringUtils.isEmpty(interfaceName)) {
			// if mapping is not found, we try to search for a global mapping, ie applying
			// to all interfaces
			mappedValue = MappingSQL.getMappingValue(null, mappingType, direction, value, poId);
		}
		return mappedValue;
	}

	@Interceptors(MappingAuthorizationFilteringInterceptor.class)
	@Override
	public String getOriginalValue(String interfaceName, MappingType mappingType,
			InterfaceMappingSet.Direction direction, String mappedValue, long poId) {
		String value = MappingSQL.getOriginalValue(interfaceName, mappingType, direction, mappedValue, poId);
		if (StringUtils.isEmpty(value) && !StringUtils.isEmpty(interfaceName)) {
			// if mapping is not found, we try to search for a global mapping, ie applying
			// to all interfaces
			value = MappingSQL.getOriginalValue(null, mappingType, direction, mappedValue, poId);
		}
		return value;
	}

	@Interceptors(MappingAuthorizationFilteringInterceptor.class)
	@Override
	public long saveInterfaceMappingSet(InterfaceMappingSet ims) {
		return MappingSQL.saveInterfaceMappingSet(ims);
	}

	@Interceptors(MappingAuthorizationFilteringInterceptor.class)
	@Override
	public InterfaceMappingSet getInterfaceMappingSet(String interfaceName, MappingType mappingType,
			InterfaceMappingSet.Direction direction) {
		return MappingSQL.getInterfaceMappingSet(interfaceName, mappingType, direction);
	}

}