package org.eclipse.tradista.core.common.persistence.util;

import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;

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

public record Join(Field[] fields) {

	public Join {
		StringBuilder errMsg = new StringBuilder();
		if (ArrayUtils.isEmpty(fields)) {
			errMsg.append("fields are mandatory.");
		}
		if (!errMsg.isEmpty()) {
			throw new IllegalArgumentException(errMsg.toString());
		}
	}

	public String getJoinField(Table table) {
		Optional<Field> field = Arrays.stream(fields).filter(f -> f.table().equals(table)).findFirst();
		String fieldName = null;
		if (field.isPresent()) {
			if (field.get().alias().length > 1) {
				fieldName = field.get().alias()[0];
			} else {
				fieldName = field.get().name();
			}
		}
		return fieldName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(fields);
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
		Join other = (Join) obj;
		return Arrays.equals(fields, other.fields);
	}

	@Override
	public String toString() {
		return Arrays.toString(fields);
	}
}