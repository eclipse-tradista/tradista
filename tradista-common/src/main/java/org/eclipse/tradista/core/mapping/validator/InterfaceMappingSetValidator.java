package org.eclipse.tradista.core.mapping.validator;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.mapping.model.InterfaceMappingSet;
import org.eclipse.tradista.core.mapping.model.InterfaceMappingSet.Mapping;
import org.springframework.util.CollectionUtils;

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

public class InterfaceMappingSetValidator implements Serializable {

	private static final long serialVersionUID = -7405086728254125087L;

	public void validateInterfaceMappingSet(InterfaceMappingSet ims) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (ims == null) {
			throw new TradistaBusinessException("The Interface Maping Set cannot be null.");
		}
		if (ims.getDirection() == null) {
			errMsg.append(String.format("The direction is mandatory.%n"));
		}
		if (ims.getProcessingOrg() == null) {
			errMsg.append(String.format("The processing organization is mandatory.%n"));
		}
		if (CollectionUtils.isEmpty(ims.getMappings())) {
			errMsg.append(String.format("At least one mapping should be defined.%n"));
		} else {
			for (Mapping m : ims.getMappings()) {
				if (StringUtils.isBlank(m.getValue())) {
					errMsg.append(String.format("A mapping cannot have an empty original value.%n"));
				}
				if (StringUtils.isBlank(m.getMappedValue())) {
					errMsg.append(String.format("A mapping cannot have an empty mapped value.%n"));
				}
			}
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}
