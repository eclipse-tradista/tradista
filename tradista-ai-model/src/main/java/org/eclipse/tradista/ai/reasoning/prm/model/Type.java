package org.eclipse.tradista.ai.reasoning.prm.model;

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

public class Type {

	protected static final String DECIMAL_TYPE_NAME = "Decimal";

	protected static final String INTEGER_TYPE_NAME = "Integer";

	protected static final String STRING_TYPE_NAME = "String";

	public static final Type DECIMAL = new Type(DECIMAL_TYPE_NAME);

	public static final Type INTEGER = new Type(INTEGER_TYPE_NAME);

	public static final Type STRING = new Type(STRING_TYPE_NAME);

	private String name;

	public Type(String name) {
		this.name = name;
	}

	public boolean checkValue(String value, Type type) {
		try {
			switch (type.getName()) {
			case DECIMAL_TYPE_NAME: {
				Double.parseDouble(value);
				break;
			}
			case INTEGER_TYPE_NAME: {
				Integer.parseInt(value);
				break;
			}

			}
		} catch (NullPointerException | NumberFormatException e) {
			return false;
		}
		return true;

	}

	public String getName() {
		return name;
	}

}