package org.eclipse.tradista.core.mapping.service;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.mapping.model.InterfaceMappingSet;
import org.eclipse.tradista.core.mapping.model.MappingType;
import org.eclipse.tradista.core.mapping.validator.InterfaceMappingSetValidator;

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

	private InterfaceMappingSetValidator interfaceMappingSetValidator;

	protected static final String DIRECTION_IS_MANDATORY = "The direction is mandatory.%n";

	public MappingBusinessDelegate() {
		mappingService = TradistaServiceLocator.getInstance().getMappingService();
		interfaceMappingSetValidator = new InterfaceMappingSetValidator();
	}

	public String getMappingValue(String interfaceName, MappingType mappingType,
			InterfaceMappingSet.Direction direction, String value, long poId) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (mappingType == null) {
			errMsg.append(String.format("the mapping type is mandatory.%n"));
		}
		if (direction == null) {
			errMsg.append(String.format(DIRECTION_IS_MANDATORY));
		}
		if (poId <= 0) {
			errMsg.append(String.format("The processing org id (%d) must be positive.%n", poId));
		}
		if (StringUtils.isEmpty(value)) {
			errMsg.append("the value is mandatory.");
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return mappingService.getMappingValue(interfaceName, mappingType, direction, value, poId);
	}

	public String getOriginalValue(String interfaceName, MappingType mappingType,
			InterfaceMappingSet.Direction direction, String value, long poId) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (mappingType == null) {
			errMsg.append(String.format("the mapping type is mandatory.%n"));
		}
		if (direction == null) {
			errMsg.append(String.format(DIRECTION_IS_MANDATORY));
		}
		if (poId < 0) {
			errMsg.append(String.format("The processing org id (%d) must be positive.%n", poId));
		}
		if (StringUtils.isEmpty(value)) {
			errMsg.append("the value is mandatory.");
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return mappingService.getOriginalValue(interfaceName, mappingType, direction, value, poId);
	}

	public long saveInterfaceMappingSet(InterfaceMappingSet ims) throws TradistaBusinessException {
		interfaceMappingSetValidator.validateInterfaceMappingSet(ims);
		return mappingService.saveInterfaceMappingSet(ims);
	}

	public InterfaceMappingSet getInterfaceMappingSet(String interfaceName, MappingType mappingType,
			InterfaceMappingSet.Direction direction) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (mappingType == null) {
			errMsg.append(String.format("The mapping type is mandatory.%n"));
		}
		if (direction == null) {
			errMsg.append(String.format(DIRECTION_IS_MANDATORY));
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return mappingService.getInterfaceMappingSet(interfaceName, mappingType, direction);
	}

}