package org.eclipse.tradista.core.common.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.junit.jupiter.api.Test;

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

class TradistaModelUtilTest {

	@Test
	void testGetPoShortNameWithNullSegregable() {
		assertNull(TradistaModelUtil.getPoShortName(null));
	}

	@Test
	void testGetPoShortNameWithNullProcessingOrg() {
		Segregable segregable = () -> null;

		assertNull(TradistaModelUtil.getPoShortName(segregable));
	}

	@Test
	void testGetPoShortNameWithValidProcessingOrg() {
		LegalEntity po = new LegalEntity("PO_SHORT_NAME");
		Segregable segregable = () -> po;

		assertEquals("PO_SHORT_NAME", TradistaModelUtil.getPoShortName(segregable));
	}
}
