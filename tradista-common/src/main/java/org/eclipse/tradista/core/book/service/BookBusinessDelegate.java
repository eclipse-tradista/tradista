package org.eclipse.tradista.core.book.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.cashinventory.service.CashInventoryService;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.productinventory.service.ProductInventoryService;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
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

public class BookBusinessDelegate {

	private BookService bookService;

	private ProductInventoryService productInventoryService;

	private CashInventoryService cashInventoryService;

	public BookBusinessDelegate() {
		bookService = TradistaServiceLocator.getInstance().getBookService();
		productInventoryService = TradistaServiceLocator.getInstance().getProductInventoryService();
		cashInventoryService = TradistaServiceLocator.getInstance().getCashInventoryService();
	}

	public long saveBook(Book book) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isBlank(book.getName())) {
			errMsg.append("The name cannot be empty.");
		}
		if (book.getProcessingOrg() == null) {
			errMsg.append("The processing org cannot be null.");
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil.runEx(() -> bookService.saveBook(book));
	}

	public Set<Book> getAllBooks() {
		return SecurityUtil.run(() -> bookService.getAllBooks());
	}

	public Book getBookByName(String name) throws TradistaBusinessException {
		if (StringUtils.isBlank(name)) {
			throw new TradistaBusinessException("The name cannot be empty.");
		}
		return SecurityUtil.run(() -> bookService.getBookByName(name));
	}

	public Book getBookById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException(String.format("The id (%s) must be positive.", id));
		}
		return SecurityUtil.run(() -> bookService.getBookById(id));
	}

	public Map<String, Map<String, BigDecimal>> getBookContent(long bookId) throws TradistaBusinessException {
		if (bookId <= 0) {
			throw new TradistaBusinessException(String.format("The book id (%s) must be positive.", bookId));
		}
		Map<String, Map<String, BigDecimal>> bookContent = new HashMap<String, Map<String, BigDecimal>>();
		Map<String, BigDecimal> bookCashContent = SecurityUtil
				.runEx(() -> cashInventoryService.getBookCashContent(LocalDate.now(), bookId));
		bookContent.put("Currency", bookCashContent);
		Map<String, BigDecimal> bookProductContent = SecurityUtil
				.runEx(() -> productInventoryService.getBookProductContent(LocalDate.now(), bookId));
		bookContent.put("Product", bookProductContent);
		return bookContent;
	}
}