package org.eclipse.tradista.ai.reasoning.prm.model;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.tradista.core.common.model.Id;
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

public class Function extends TradistaObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6677474058648103366L;

	private Set<Parameter> parameters;

	private Type returnType;

	@Id
	private String name;

	public Function(String name) {
		super();
		this.name = name;
		parameters = new LinkedHashSet<>();
	}

	@SuppressWarnings("unchecked")
	public Set<Parameter> getParameters() {
		return (Set<Parameter>) TradistaModelUtil.deepCopy(parameters);
	}

	public void addParameter(Parameter parameter) {
		parameters.add(parameter);
	}

	public void setParameters(Set<Parameter> parameters) {
		this.parameters = parameters;
	}

	public Type getReturnType() {
		return returnType;
	}

	public void setReturnType(Type returnType) {
		this.returnType = returnType;
	}

	public String getName() {
		return name;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Function clone() {
		Function function = (Function) super.clone();
		function.parameters = (Set<Parameter>) TradistaModelUtil.deepCopy(parameters);
		return function;
	}

}