package org.eclipse.tradista.core.common.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

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

class TradistaUtilTest {

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "\t", "\n" })
	void testValidateNameBlankThrowsException(String blankName) {
		TradistaBusinessException ex = assertThrows(TradistaBusinessException.class,
				() -> TradistaUtil.validateName(blankName));
		assertEquals("The name is mandatory.", ex.getMessage());
	}

	@Test
	void testValidateNameExceeds50CharactersThrowsException() {
		String longName = "A".repeat(51);
		TradistaBusinessException ex = assertThrows(TradistaBusinessException.class,
				() -> TradistaUtil.validateName(longName));
		assertEquals("The name cannot exceed 50 characters.", ex.getMessage());
	}

	@Test
	void testValidateNameValidDoesNotThrow() {
		assertDoesNotThrow(() -> TradistaUtil.validateName("Valid Name"));
		assertDoesNotThrow(() -> TradistaUtil.validateName("A".repeat(50)));
	}

	@Test
	void testValidateNameWithStringBuilderBlank() {
		StringBuilder errMsg = new StringBuilder();
		TradistaUtil.validateName(StringUtils.EMPTY, errMsg);
		assertTrue(errMsg.toString().contains("The name is mandatory."));
	}

	@Test
	void testValidateNameWithStringBuilderExceeds50Characters() {
		StringBuilder errMsg = new StringBuilder();
		TradistaUtil.validateName("A".repeat(51), errMsg);
		assertTrue(errMsg.toString().contains("The name cannot exceed 50 characters."));
	}

	@Test
	void testValidateNameWithStringBuilderValid() {
		StringBuilder errMsg = new StringBuilder();
		TradistaUtil.validateName("Valid Name", errMsg);
		assertTrue(errMsg.isEmpty());
	}
}
