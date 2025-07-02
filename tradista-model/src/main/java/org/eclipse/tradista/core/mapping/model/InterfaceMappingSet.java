package org.eclipse.tradista.core.mapping.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.tradista.core.common.model.Id;
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

	public class Mapping implements Serializable {

		private static final long serialVersionUID = 3025216935232998072L;

		private String value;

		private String mappedValue;

		protected Mapping(String value, String mappedValue) {
			this.value = value;
			this.mappedValue = mappedValue;
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + Objects.hash(value);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Mapping other = (Mapping) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			return Objects.equals(value, other.value);
		}

		private InterfaceMappingSet getEnclosingInstance() {
			return InterfaceMappingSet.this;
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

	public Set<Mapping> getMappings() {
		return mappings;
	}

	public void setMappings(Set<Mapping> mappings) {
		this.mappings = mappings;
	}

	public LegalEntity getProcessingOrg() {
		return processingOrg;
	}

	public void addMapping(String value, String mappedValue) {
		mappings.add(new Mapping(value, mappedValue));
	}

	public void removeMapping(Mapping mapping) {
		System.out.println(mappings.toArray()[1].hashCode());
		System.out.println(mapping.hashCode());
		System.out.println(mappings.toArray()[1].equals(mapping));
		System.out.println(mapping.equals(mappings.toArray()[1]));
		System.out.println(mappings.contains(mapping));
		System.out.println("IMS hashcode: " + hashCode());
		System.out.println("IMS id" + getId());
		System.out.println("IMS " + this);
		System.out.println("IMS mapping type hashcode " + getMappingType().hashCode());
		System.out.println("IMS po hashcode  " + getProcessingOrg().hashCode());
		System.out.println("IMS direction hashcode " + getDirection().hashCode());
		System.out.println("ClassLoader of IMS direction: " + getDirection().getClass().getClassLoader());
		mappings.remove(mapping);
	}

	public boolean isIncoming() {
		return direction.equals(Direction.INCOMING);
	}

}