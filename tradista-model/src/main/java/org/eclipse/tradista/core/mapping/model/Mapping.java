package org.eclipse.tradista.core.mapping.model;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaObject;

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

public class Mapping extends TradistaObject {

	private static final long serialVersionUID = 8531096552298461212L;

	@Id
	private String importerName;

	@Id
	private MappingType mappingType;

	@Id
	private String value;

	private String mappedValue;

	public Mapping(String importerName, MappingType mappingType, String value) {
		this.importerName = importerName;
		this.mappingType = mappingType;
		this.value = value;
	}

	public String getImporterName() {
		return importerName;
	}

	public MappingType getMappingType() {
		return mappingType;
	}

	public String getValue() {
		return value;
	}

	public String getMappedValue() {
		return mappedValue;
	}

	public void setMappedValue(String mappedValue) {
		this.mappedValue = mappedValue;
	}

}