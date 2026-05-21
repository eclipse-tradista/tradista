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

package org.eclipse.tradista.legalentity.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.AccessChecker;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;

/**
 * {@link AccessChecker} implementation for
 * {@link org.eclipse.tradista.core.legalentity.model.LegalEntity} parameters.
 * Verifies that a legal entity with the given id actually exists in the system.
 */
public class LegalEntityAccessChecker implements AccessChecker {

	private final LegalEntityBusinessDelegate legalEntityBusinessDelegate;

	public LegalEntityAccessChecker() {
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
	}

	@Override
	public void check(Object parameter, StringBuilder errMsg) throws TradistaBusinessException {
		if (parameter instanceof LegalEntity legalEntity && legalEntity.getId() != 0) {
			if (legalEntityBusinessDelegate.getLegalEntityById(legalEntity.getId()) == null) {
				errMsg.append(String.format("The legal entity %s was not found.%n", legalEntity.getShortName()));
			}
		} else if (parameter instanceof Long leId && leId != 0) {
			if (legalEntityBusinessDelegate.getLegalEntityById(leId) == null) {
				errMsg.append(String.format("The legal entity with id %d was not found.%n", leId));
			}
		}
	}

}