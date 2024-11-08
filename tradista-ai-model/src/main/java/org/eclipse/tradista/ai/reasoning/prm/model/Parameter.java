package org.eclipse.tradista.ai.reasoning.prm.model;

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

public class Parameter extends TradistaObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2523448749272012352L;

	@Id
	private short position;

	private Type type;

	@Id
	private Function function;

	public Parameter(Function function, short position) {
		super();
		this.function = function;
		this.position = position;
	}

	public short getPosition() {
		return position;
	}

	public void setPosition(short position) {
		this.position = position;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Function getFunction() {
		return TradistaModelUtil.clone(function);
	}

	public void setFunction(Function function) {
		this.function = function;
	}

	@Override
	public Parameter clone() {
		Parameter parameter = (Parameter) super.clone();
		parameter.function = TradistaModelUtil.clone(function);
		return parameter;
	}

}