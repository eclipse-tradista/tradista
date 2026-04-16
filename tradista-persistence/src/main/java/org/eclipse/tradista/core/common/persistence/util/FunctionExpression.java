package org.eclipse.tradista.core.common.persistence.util;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

public class FunctionExpression implements Expression {

	private String functionName;

	protected List<Expression> arguments;

	public FunctionExpression(List<Expression> arguments, String functionName) {
		this.arguments = arguments;
		this.functionName = functionName;
	}

	public void setArguments(List<Expression> arguments) {
		this.arguments = arguments;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	@Override
	public String getRepresentation() {
		String args = arguments.stream().map(Expression::getRepresentation).collect(Collectors.joining(", "));
		return functionName + "(" + args + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof FunctionExpression that)) {
			return false;
		}

		return Objects.equals(functionName, that.functionName) && Objects.equals(arguments, that.arguments);
	}

	@Override
	public int hashCode() {
		return Objects.hash(functionName, arguments);
	}

	@Override
	public String toString() {
		return getRepresentation();
	}
}