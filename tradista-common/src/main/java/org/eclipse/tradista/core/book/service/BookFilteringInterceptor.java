package org.eclipse.tradista.core.book.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.user.model.User;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

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

public class BookFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	private BookBusinessDelegate bookBusinessDelegate;

	public BookFilteringInterceptor() {
		super();
		bookBusinessDelegate = new BookBusinessDelegate();
	}

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	@Override
	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		Object[] parameters = ic.getParameters();
		if (parameters.length > 0 && parameters[0] instanceof Book book) {
			StringBuilder errMsg = new StringBuilder();
			User user = getCurrentUser();
			if (book.getId() != 0) {
				Book b = bookBusinessDelegate.getBookById(book.getId());
				if (b == null) {
					errMsg.append(String.format("The book %s was not found.%n", book.getName()));
				} else if (b.getProcessingOrg() == null) {
					errMsg.append(String.format("The book %s is a global one and you are not allowed to update it.%n",
							book.getName()));
				}
			}
			if (book.getProcessingOrg() != null && !book.getProcessingOrg().equals(user.getProcessingOrg())) {
				errMsg.append(String.format("The processing org %s was not found.", book.getProcessingOrg()));
			}
			if (!errMsg.isEmpty()) {
				throw new TradistaBusinessException(errMsg.toString());
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object postFilter(Object value) throws TradistaBusinessException {
		if (value != null) {
			User user = getCurrentUser();
			if (value instanceof Set) {
				Set<Book> books = (Set<Book>) value;
				if (!books.isEmpty()) {
					value = books.stream().filter(b -> b.getProcessingOrg().equals(user.getProcessingOrg()))
							.collect(Collectors.toSet());
				}
			}
			if (value instanceof Book book) {
				if (!book.getProcessingOrg().equals(user.getProcessingOrg())) {
					value = null;
				}
			}
		}
		return value;
	}

}