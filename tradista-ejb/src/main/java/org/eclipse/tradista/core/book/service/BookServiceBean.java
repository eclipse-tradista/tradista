package org.eclipse.tradista.core.book.service;

import java.util.Set;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.persistence.BookSQL;
import org.eclipse.tradista.core.book.service.BookFilteringInterceptor;
import org.eclipse.tradista.core.book.service.BookService;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

/********************************************************************************
 * Copyright (c) 2019 Olivier Asuncion
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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class BookServiceBean implements BookService {

	@Interceptors(BookFilteringInterceptor.class)
	@Override
	public Set<Book> getAllBooks() {
		return BookSQL.getAllBooks();
	}

	@Override
	public long saveBook(Book book) throws TradistaBusinessException {
		if (book.getId() == 0) {
			checkBookExistence(book);
		} else {
			Book oldBook = BookSQL.getBookById(book.getId());
			if (!oldBook.getName().equals(book.getName())
					|| !oldBook.getProcessingOrg().equals(book.getProcessingOrg())) {
				checkBookExistence(book);
			}
		}
		return BookSQL.saveBook(book);
	}

	private void checkBookExistence(Book book) throws TradistaBusinessException {
		if (BookSQL.bookExists(book.getName(), book.getProcessingOrg().getId())) {
			throw new TradistaBusinessException(
					String.format("A book with the name '%s' and the PO '%s' already exists in the system.",
							book.getName(), book.getProcessingOrg().getShortName()));
		}
	}

	@Interceptors(BookFilteringInterceptor.class)
	@Override
	public Book getBookByName(String name) {
		return BookSQL.getBookByName(name);
	}

	@Interceptors(BookFilteringInterceptor.class)
	@Override
	public Book getBookById(long id) {
		return BookSQL.getBookById(id);
	}

}