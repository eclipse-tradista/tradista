package org.eclipse.tradista.core.inventory.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.currency.model.Currency;

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

public class CashInventory extends TradistaObject implements Comparable<CashInventory> {

	private static final long serialVersionUID = 1L;

	@Id
	private Currency currency;

	private BigDecimal amount;

	@Id
	private LocalDate from;

	private LocalDate to;

	@Id
	private Book book;

	public CashInventory(Currency currency, LocalDate from, Book book) {
		this.currency = currency;
		this.from = from;
		this.book = book;
	}

	public Currency getCurrency() {
		return TradistaModelUtil.clone(currency);
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public LocalDate getFrom() {
		return from;
	}

	public LocalDate getTo() {
		return to;
	}

	public void setTo(LocalDate to) {
		this.to = to;
	}

	public Book getBook() {
		return TradistaModelUtil.clone(book);
	}

	@Override
	public int compareTo(CashInventory ci) {
		if (ci == null) {
			return 1;
		}
		int eq = getBook().toString().compareTo(ci.getBook().toString());
		if (eq == 0) {
			eq = getCurrency().toString().compareTo(ci.getCurrency().toString());
			if (eq == 0) {
				return getFrom().compareTo(ci.getFrom());
			} else {
				return eq;
			}
		}
		return eq;
	}

	@Override
	public CashInventory clone() {
		CashInventory ci = new CashInventory(currency, from, book);
		ci.setAmount(amount);
		ci.setId(getId());
		ci.setTo(to);
		return ci;
	}

}