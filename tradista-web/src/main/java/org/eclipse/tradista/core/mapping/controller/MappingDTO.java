package org.eclipse.tradista.core.mapping.controller;

import java.io.Serializable;
import org.eclipse.tradista.core.mapping.model.InterfaceMappingSet.Mapping;

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

public class MappingDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String value;
	private String mappedValue;

	public MappingDTO() {
	}

	public MappingDTO(Mapping mapping) {
		this.value = mapping.getValue();
		this.mappedValue = mapping.getMappedValue();
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getMappedValue() {
		return mappedValue;
	}

	public void setMappedValue(String mappedValue) {
		this.mappedValue = mappedValue;
	}
}
