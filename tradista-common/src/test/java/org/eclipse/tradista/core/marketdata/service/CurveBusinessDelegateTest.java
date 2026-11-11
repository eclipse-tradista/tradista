package org.eclipse.tradista.core.marketdata.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.marketdata.model.Curve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
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

class CurveBusinessDelegateTest {

	private CurveBusinessDelegate businessDelegate = new CurveBusinessDelegate();

	@Test
	void testSaveCurveNameExceeds50Characters() {
		Curve<LocalDate, BigDecimal> curve = new InterestRateCurve("A".repeat(51), null);

		assertThrows(TradistaBusinessException.class, () -> businessDelegate.saveCurve(curve));
	}

	@Test
	void testSaveCurveBlankOrEmptyName() {
		Curve<LocalDate, BigDecimal> curve = new InterestRateCurve(StringUtils.EMPTY, null);

		assertThrows(TradistaBusinessException.class, () -> businessDelegate.saveCurve(curve));
	}
}
