package org.eclipse.tradista.core.transfer.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.Trade;

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

public class CashTransfer extends Transfer {

	public CashTransfer(Book book, TransferPurpose purpose, LocalDate settlementDate, Trade<?> trade,
			Currency currency) {
		super(book, null, purpose, settlementDate, trade);
		this.currency = currency;
	}

	public CashTransfer(Book book, Product product, TransferPurpose purpose, LocalDate settlementDate,
			Currency currency) {
		super(book, product, purpose, settlementDate, null);
		this.currency = currency;
	}

	public CashTransfer(Book book, Product product, TransferPurpose purpose, LocalDate settlementDate, Trade<?> trade,
			Currency currency) {
		super(book, product, purpose, settlementDate, trade);
		this.currency = currency;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7123636684384971261L;

	@Id
	private Currency currency;

	public Currency getCurrency() {
		return TradistaModelUtil.clone(currency);
	}

	public BigDecimal getAmount() {
		return quantityOrAmount;
	}

	public void setAmount(BigDecimal amount) {
		this.quantityOrAmount = amount;
	}

	@Override
	public Type getType() {
		return Transfer.Type.CASH;
	}

	@Override
	public CashTransfer clone() {
		CashTransfer cashTransfer = (CashTransfer) super.clone();
		cashTransfer.currency = TradistaModelUtil.clone(currency);
		return cashTransfer;
	}

}