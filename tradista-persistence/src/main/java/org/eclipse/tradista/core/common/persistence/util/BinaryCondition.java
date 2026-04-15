package org.eclipse.tradista.core.common.persistence.util;

import java.util.Objects;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;

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

public class BinaryCondition {

	private final Expression left;

	private final Operator operator;

	private final Expression right;

	public BinaryCondition(Expression left, Operator operator, Expression right) {
		StringBuilder errMsg = new StringBuilder();
		if (left == null) {
			errMsg.append(String.format("The left expression is mandatory.%n"));
		}
		if (operator == null) {
			errMsg.append(String.format("The operator is mandatory.%n"));
		}
		if (right == null) {
			errMsg.append("The right expression is mandatory.");
		}

		if (!errMsg.isEmpty()) {
			throw new TradistaTechnicalException(errMsg.toString());
		}
		this.left = left;
		this.operator = operator;
		this.right = right;
	}

	public String getRepresentation() {
		return left.getRepresentation() + " " + operator.getSql() + " " + right.getRepresentation();
	}

	public Expression getLeft() {
		return left;
	}

	public Operator getOperator() {
		return operator;
	}

	public Expression getRight() {
		return right;
	}

	@Override
	public int hashCode() {
		return Objects.hash(left, operator, right);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BinaryCondition other = (BinaryCondition) obj;
		return Objects.equals(left, other.left) && operator == other.operator && Objects.equals(right, other.right);
	}

}