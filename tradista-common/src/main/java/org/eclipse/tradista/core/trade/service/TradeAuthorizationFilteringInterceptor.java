package org.eclipse.tradista.core.trade.service;

import java.util.List;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.Trade;
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

public class TradeAuthorizationFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	private BookBusinessDelegate bookBusinessDelegate;

	private TradeBusinessDelegate tradeBusinessDelegate;

	public TradeAuthorizationFilteringInterceptor() {
		super();
		bookBusinessDelegate = new BookBusinessDelegate();
		tradeBusinessDelegate = new TradeBusinessDelegate();
	}

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	@Override
	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		Object[] parameters = ic.getParameters();
		if (parameters.length > 0) {
			if (parameters[0] instanceof Trade<?> trade) {
				StringBuilder errMsg = new StringBuilder();
				if (trade.getId() != 0) {
					Trade<?> t = tradeBusinessDelegate.getTradeById(trade.getId(), true);
					if (t == null) {
						errMsg.append(String.format("The trade %d was not found.%n", trade.getId()));
					}
				}
				Book book = bookBusinessDelegate.getBookById(trade.getBook().getId());
				if (book == null) {
					errMsg.append(String.format("The book %s was not found.", trade.getBook().getName()));
				}
				if (errMsg.length() > 0) {
					throw new TradistaBusinessException(errMsg.toString());
				}
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Object postFilter(Object value) {
		if (value != null) {
			User user = getCurrentUser();
			if (value instanceof Trade) {
				Trade<Product> trade = (Trade<Product>) value;
				if (!trade.getBook().getProcessingOrg().equals(user.getProcessingOrg())) {
					value = null;
				}
			}
			if (value instanceof List) {
				List<Trade<? extends Product>> trades = (List<Trade<? extends Product>>) value;
				value = trades.stream().filter(t -> t.getBook().getProcessingOrg().equals(user.getProcessingOrg()))
						.toList();
			}
		}
		return value;
	}

}