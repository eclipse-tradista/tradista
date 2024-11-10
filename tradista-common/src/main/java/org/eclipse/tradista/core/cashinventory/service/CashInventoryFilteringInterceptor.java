package org.eclipse.tradista.core.cashinventory.service;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.inventory.model.CashInventory;
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

public class CashInventoryFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	private BookBusinessDelegate bookBusinessDelegate;

	public CashInventoryFilteringInterceptor() {
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
		long bookId = (long) parameters[3];
		if (bookId != 0) {
			Book book = bookBusinessDelegate.getBookById(bookId);
			if (book == null) {
				throw new TradistaBusinessException(String.format("The Book %s was not found.", bookId));
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Object postFilter(Object value) {
		if (value != null) {
			if (value instanceof Set) {
				Set<CashInventory> cashInventories = (Set<CashInventory>) value;
				User user = getCurrentUser();
				value = cashInventories.stream()
						.filter(pi -> pi.getBook().getProcessingOrg().equals(user.getProcessingOrg()))
						.collect(Collectors.toCollection(TreeSet::new));
			}
		}
		return value;
	}

}