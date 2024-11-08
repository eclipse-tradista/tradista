package org.eclipse.tradista.ai.reasoning.prm.probability.model;

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

public enum Operator {

	EQUALS, GREATER, LESS, GREATER_OR_EQUALS, LESS_OR_EQUALS, DIFFERENT;

	@Override
	public String toString() {
		switch (this) {
		case EQUALS:
			return "=";
		case GREATER:
			return ">";
		case LESS:
			return "<";
		case GREATER_OR_EQUALS:
			return ">=";
		case LESS_OR_EQUALS:
			return "<=";
		case DIFFERENT:
			return "<>";
		default:
			throw new IllegalArgumentException();
		}
	}

}