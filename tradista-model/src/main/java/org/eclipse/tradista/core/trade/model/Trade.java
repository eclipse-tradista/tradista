package org.eclipse.tradista.core.trade.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.workflow.model.Status;
import org.eclipse.tradista.core.workflow.model.WorkflowObject;

/********************************************************************************
 * Copyright (c) 2014 Olivier Asuncion
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

public abstract class Trade<P extends Product> extends TradistaObject implements WorkflowObject {

	private static final long serialVersionUID = 3681323495299195621L;

	public enum Direction {
		BUY, SELL;

		@Override
		public String toString() {
			switch (this) {
			case BUY:
				return "Buy";
			case SELL:
				return "Sell";
			}
			return super.toString();
		}
	}

	private P product;

	private LocalDate tradeDate;

	private LocalDate settlementDate;

	private BigDecimal amount;

	private Currency currency;

	private LegalEntity counterparty;

	private Book book;

	private Status status;

	private String workflow;

	// true : BUY, false : SELL
	private boolean buySell;

	public Trade(P product) {
		this.product = product;
	}

	public Trade() {
	}

	@Override
	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public String getWorkflow() {
		return workflow;
	}

	public void setWorkflow(String name) {
		this.workflow = name;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	public Book getBook() {
		return TradistaModelUtil.clone(book);
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public LegalEntity getCounterparty() {
		return TradistaModelUtil.clone(counterparty);
	}

	public void setCounterparty(LegalEntity counterparty) {
		this.counterparty = counterparty;
	}

	public Currency getCurrency() {
		return TradistaModelUtil.clone(currency);
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public LocalDate getSettlementDate() {
		return settlementDate;
	}

	public void setSettlementDate(LocalDate settlementDate) {
		this.settlementDate = settlementDate;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public boolean isBuy() {
		return buySell;
	}

	public boolean isSell() {
		return !buySell;
	}

	public void setBuySell(boolean buySell) {
		this.buySell = buySell;
	}

	public LocalDate getTradeDate() {
		return tradeDate;
	}

	public void setTradeDate(LocalDate tradeDate) {
		this.tradeDate = tradeDate;
	}

	public LocalDate getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDate creationDate) {
		this.creationDate = creationDate;
	}

	private LocalDate creationDate;

	public P getProduct() {
		return TradistaModelUtil.clone(product);
	}

	public void setProduct(P product) {
		this.product = product;
	}

	public long getProductId() {
		if (product != null) {
			return product.getId();
		}
		return 0;
	}

	public String getProductType() {
		if (product != null) {
			return product.getProductType();
		}
		return null;
	}

	public Exchange getExchange() {
		if (product != null) {
			return product.getExchange();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Trade<P> clone() {
		Trade<P> trade = (Trade<P>) super.clone();
		trade.product = TradistaModelUtil.clone(product);
		trade.counterparty = TradistaModelUtil.clone(counterparty);
		trade.currency = TradistaModelUtil.clone(currency);
		trade.book = TradistaModelUtil.clone(book);
		return trade;
	}

}