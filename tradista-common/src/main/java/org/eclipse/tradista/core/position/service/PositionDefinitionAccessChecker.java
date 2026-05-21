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

package org.eclipse.tradista.core.position.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.AccessChecker;
import org.eclipse.tradista.core.position.model.PositionDefinition;

/**
 * access checker for position definitions.
 */
public class PositionDefinitionAccessChecker implements AccessChecker {

	private PositionDefinitionBusinessDelegate positionDefinitionBusinessDelegate;

	public PositionDefinitionAccessChecker() {
		positionDefinitionBusinessDelegate = new PositionDefinitionBusinessDelegate();
	}

	@Override
	public void check(Object value, StringBuilder errMsg) throws TradistaBusinessException {
		if (value instanceof PositionDefinition posDef) {
			if (posDef.getId() != 0) {
				if (positionDefinitionBusinessDelegate.getPositionDefinitionById(posDef.getId()) == null) {
					errMsg.append(String.format("The position definition %s was not found.%n", posDef.getName()));
				}
			}
		} else if (value instanceof Long id) {
			if (id != 0) {
				if (positionDefinitionBusinessDelegate.getPositionDefinitionById(id) == null) {
					errMsg.append(String.format("The Position Definition %d was not found.%n", id));
				}
			}
		}
	}

}