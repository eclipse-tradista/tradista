package org.eclipse.tradista.core.book.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.test.TradistaTestUtil;
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

class BookBusinessDelegateTest {

	private BookBusinessDelegate businessDelegate = new BookBusinessDelegate();

	@Test
	void testSaveBookNameExceeds50Characters() {
		Book book = TradistaTestUtil.createBook("A".repeat(51));

		assertThrows(TradistaBusinessException.class, () -> businessDelegate.saveBook(book));
	}

	@Test
	void testSaveBookBlankOrEmptyName() {
		Book book = TradistaTestUtil.createBook(StringUtils.EMPTY);

		assertThrows(TradistaBusinessException.class, () -> businessDelegate.saveBook(book));
	}
}