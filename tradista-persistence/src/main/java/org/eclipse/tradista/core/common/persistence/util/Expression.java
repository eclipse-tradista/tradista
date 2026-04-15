package org.eclipse.tradista.core.common.persistence.util;

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

public interface Expression {

	String getRepresentation();

	// For some expressions (field or unary function), a table can be deduced
	default Table getTable() {
		return null;
	}

	default BinaryCondition eq(Expression other) {
		return new BinaryCondition(this, Operator.EQUALS, other);
	}

	default BinaryCondition ne(Expression other) {
		return new BinaryCondition(this, Operator.NOT_EQUALS, other);
	}

	default BinaryCondition gt(Expression other) {
		return new BinaryCondition(this, Operator.GREATER_THAN, other);
	}

	default BinaryCondition ge(Expression other) {
		return new BinaryCondition(this, Operator.GREATER_OR_EQUALS, other);
	}

	default BinaryCondition lt(Expression other) {
		return new BinaryCondition(this, Operator.LESS_THAN, other);
	}

	default BinaryCondition le(Expression other) {
		return new BinaryCondition(this, Operator.LESS_OR_EQUALS, other);
	}
}