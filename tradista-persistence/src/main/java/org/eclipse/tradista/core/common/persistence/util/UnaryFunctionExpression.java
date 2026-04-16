package org.eclipse.tradista.core.common.persistence.util;

import java.util.List;

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

public class UnaryFunctionExpression extends FunctionExpression {

	public enum UnaryFunction {
		MIN, MAX, AVG;

		@Override
		public String toString() {
			return switch (this) {
			case MIN -> "MIN";
			case MAX -> "MAX";
			case AVG -> "AVG";
			default -> super.toString();
			};
		}
	}

	public UnaryFunctionExpression(Expression expression, String functionName) {
		super(List.of(expression), functionName);
	}

	public static UnaryFunctionExpression max(Expression expression) {
		return new UnaryFunctionExpression(expression, UnaryFunction.MAX.toString());
	}

	public static UnaryFunctionExpression min(Expression expression) {
		return new UnaryFunctionExpression(expression, UnaryFunction.MIN.toString());
	}

	public static UnaryFunctionExpression avg(Expression expression) {
		return new UnaryFunctionExpression(expression, UnaryFunction.AVG.toString());
	}

	@Override
	public Table getTable() {
		if (arguments.getFirst() instanceof Field f) {
			return f.getTable();
		} else {
			if (arguments.getFirst() instanceof UnaryFunctionExpression ufe) {
				return ufe.getTable();
			}
		}
		return null;
	}

}