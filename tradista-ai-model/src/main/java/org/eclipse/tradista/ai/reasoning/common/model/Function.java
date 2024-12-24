package org.eclipse.tradista.ai.reasoning.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
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

public abstract class Function<X> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1449344242599084561L;

	private X returnType;

	private String name;

	private List<Parameter> parameterTypes;

	private class Parameter {
		private String name;
		private Class<? extends Object> type;
		private boolean isMandatory;

		private Parameter(String name, Class<? extends Object> type, boolean mandatory) {
			this.name = name;
			this.type = type;
			isMandatory = mandatory;
		}
	}

	public Function() {
		parameterTypes = new ArrayList<Parameter>();
	}

	public X getReturnType() {
		return returnType;
	}

	public void setReturnType(X returnType) {
		this.returnType = returnType;
	}

	public List<Parameter> getParameters() {
		return parameterTypes;
	}

	public void addParameterType(String name, Class<? extends Object> type, boolean mandatory) {
		this.parameterTypes.add(new Parameter(name, type, mandatory));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	protected void check(Object... parameters) throws TradistaBusinessException {
		if (parameters != null && parameterTypes != null && parameters.length != parameterTypes.size()) {
			throw new TradistaBusinessException(
					String.format("Number of parameters is not correct. There should be %s parameters and not %s.",
							parameterTypes.size(), parameters.length));
		}
		StringBuilder errMsg = new StringBuilder();
		short pos = 0;
		for (Parameter p : parameterTypes) {
			Object o = parameters[pos];
			if ((o == null) && (p.isMandatory)) {
				errMsg.append(String.format("Parameter %s is mandatory.%n", p.name));
			} else {
				if ((o != null) && (!p.type.isInstance(o))) {
					errMsg.append(String.format("Parameter %s should be an instance of %s, not %s.%n", p.name, p.type,
							o.getClass()));
				}
			}
			pos++;
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Function<X> other = (Function<X>) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}