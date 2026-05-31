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

package org.eclipse.tradista.core.book.service;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.AccessChecker;

/**
 * {@link AccessChecker} implementation for
 * {@link org.eclipse.tradista.core.book.model.Book} parameters. Verifies that
 * the book (if id is non-zero) is accessible in the system.
 */
public class BookAccessChecker implements AccessChecker {

	private final BookBusinessDelegate bookBusinessDelegate;

	public BookAccessChecker() {
		bookBusinessDelegate = new BookBusinessDelegate();
	}

	@Override
	public void check(Object parameter, StringBuilder errMsg) throws TradistaBusinessException {
		if (parameter instanceof Book book) {
			if (book.getId() != 0) {
				if (bookBusinessDelegate.getBookById(book.getId()) == null) {
					errMsg.append(String.format("The book %s was not found.%n", book.getName()));
				}
			}
		} else if (parameter instanceof Long bookId) {
			if (bookId != 0) {
				if (bookBusinessDelegate.getBookById(bookId) == null) {
					errMsg.append(String.format("The book with id %d was not found.%n", bookId));
				}
			}
		}
	}

}