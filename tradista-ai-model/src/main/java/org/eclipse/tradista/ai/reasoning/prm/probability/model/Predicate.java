package org.eclipse.tradista.ai.reasoning.prm.probability.model;

import org.eclipse.tradista.ai.reasoning.prm.model.FunctionCall;
import org.eclipse.tradista.ai.reasoning.prm.model.Value;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;

/********************************************************************************
 * Copyright (c) 2017 Olivier Asuncion
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

public class Predicate extends TradistaObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7471677760825414676L;

	private FunctionCall functionCall;

	private Operator operator;

	private Value value;

	private boolean result;

	public FunctionCall getFunctionCall() {
		return TradistaModelUtil.clone(functionCall);
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public void setFunctionCall(FunctionCall functionCall) {
		this.functionCall = functionCall;
	}

	@Override
	public Predicate clone() {
		Predicate predicate = (Predicate) super.clone();
		predicate.functionCall = TradistaModelUtil.clone(functionCall);
		return predicate;
	}

}