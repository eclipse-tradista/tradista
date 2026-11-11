package org.eclipse.tradista.core.pricing.validator;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.test.TradistaTestUtil;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
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

class PricingParameterValidatorTest {

	private PricingParameterValidator validator = new PricingParameterValidator();

	@Test
	void testValidatePricingParameterNameExceeds50Characters() {
		LegalEntity po = TradistaTestUtil.createProcessingOrg();
		PricingParameter param = new PricingParameter("A".repeat(51), po);

		assertThrows(TradistaBusinessException.class, () -> validator.validatePricingParameter(param));
	}

	@Test
	void testValidatePricingParameterBlankOrEmptyName() {
		LegalEntity po = TradistaTestUtil.createProcessingOrg();
		PricingParameter param = new PricingParameter(StringUtils.EMPTY, po);

		assertThrows(TradistaBusinessException.class, () -> validator.validatePricingParameter(param));
	}
}
