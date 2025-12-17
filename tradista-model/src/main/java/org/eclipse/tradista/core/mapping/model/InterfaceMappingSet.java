package org.eclipse.tradista.core.mapping.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;

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

public class InterfaceMappingSet extends TradistaObject {

	private static final long serialVersionUID = 8531096552298461212L;

	public enum Direction {
		INCOMING, OUTGOING;
	}

	public class Mapping extends TradistaObject {

		private static final long serialVersionUID = 3025216935232998072L;

		@Id
		private InterfaceMappingSet enclosingInstance;

		@Id
		private String value;

		private String mappedValue;

		public Mapping(String value, String mappedValue) {
			this.value = value;
			this.mappedValue = mappedValue;
			enclosingInstance = InterfaceMappingSet.this;
		}

		public String getValue() {
			return value;
		}

		// Setter added because used by Primefaces in a datatable
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

	@Id
	private String interfaceName;

	@Id
	private MappingType mappingType;

	@Id
	private LegalEntity processingOrg;

	@Id
	private Direction direction;

	private Set<Mapping> mappings;

	public InterfaceMappingSet(String interfaceName, MappingType mappingType, Direction direction,
			LegalEntity processingOrg) {
		this.interfaceName = interfaceName;
		this.mappingType = mappingType;
		this.direction = direction;
		this.processingOrg = processingOrg;
		mappings = new HashSet<>();
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public MappingType getMappingType() {
		return mappingType;
	}

	public Direction getDirection() {
		return direction;
	}

	@SuppressWarnings("unchecked")
	public Set<Mapping> getMappings() {
		return (Set<Mapping>) TradistaModelUtil.deepCopy(mappings);
	}

	public void setMappings(Set<Mapping> mappings) {
		this.mappings = mappings;
	}

	public LegalEntity getProcessingOrg() {
		return TradistaModelUtil.clone(processingOrg);
	}

	public void addMapping(String value, String mappedValue) {
		mappings.add(new Mapping(value, mappedValue));
	}

	public void removeMapping(Mapping mapping) {
		mappings.remove(mapping);
	}

	public boolean isIncoming() {
		return direction.equals(Direction.INCOMING);
	}

	@SuppressWarnings("unchecked")
	@Override
	public InterfaceMappingSet clone() {
		InterfaceMappingSet ims = (InterfaceMappingSet) super.clone();
		ims.mappings = (Set<Mapping>) TradistaModelUtil.deepCopy(mappings);
		ims.processingOrg = TradistaModelUtil.clone(processingOrg);
		return ims;
	}

}