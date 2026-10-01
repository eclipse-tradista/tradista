package org.eclipse.tradista.core.trade.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.model.Segregable;
import org.eclipse.tradista.core.common.model.TimestampedObject;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
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

public abstract class Trade<P extends Product> extends TimestampedObject implements WorkflowObject, Segregable {

	private static final long serialVersionUID = 3681323495299195621L;

	public enum Direction {
		BUY, SELL;

		@Override
		public String toString() {
			return switch (this) {
			case BUY -> "Buy";
			case SELL -> "Sell";
			default -> super.toString();
			};
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

	/**
	 * @deprecated use {@link Builder} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	protected Trade(P product) {
		this.product = product;
	}

	/**
	 * @deprecated use {@link Builder} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	protected Trade() {
	}

	protected Trade(Builder<P, ?, ?> builder) {
		super(builder);
		this.product = builder.product;
		this.tradeDate = builder.tradeDate;
		this.settlementDate = builder.settlementDate;
		this.amount = builder.amount;
		this.currency = builder.currency;
		this.counterparty = builder.counterparty;
		this.book = builder.book;
		this.status = builder.status;
		this.workflow = builder.workflow;
		this.buySell = builder.buySell;
	}

	@Override
	public Builder<P, ?, ?> toBuilder() {
		return null;
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

	@Override
	public LegalEntity getProcessingOrg() {
		return book != null ? book.getProcessingOrg() : null;
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

	/**
	 * @deprecated use {@link #getCreationTime()} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public LocalDate getCreationDate() {
		if (getCreationTime() == null) {
			return null;
		}
		return LocalDate.ofInstant(getCreationTime(), ZoneId.systemDefault());
	}

	/**
	 * @deprecated creation time is automatically managed and immutable. Use
	 *             {@link Builder#creationTime(java.time.Instant)} if needed during construction.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public void setCreationDate(LocalDate creationDate) {
		// No-op: creationTime is immutable and managed at construction/builder level.
	}


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

	public abstract static class Builder<P extends Product, T extends Trade<P>, B extends Builder<P, T, B>>
			extends TimestampedObject.Builder<T, B> {
		protected P product;
		protected LocalDate tradeDate;
		protected LocalDate settlementDate;
		protected BigDecimal amount;
		protected Currency currency;
		protected LegalEntity counterparty;
		protected Book book;
		protected Status status;
		protected String workflow;
		protected boolean buySell;

		public B product(P product) {
			this.product = product;
			return self();
		}

		public B tradeDate(LocalDate tradeDate) {
			this.tradeDate = tradeDate;
			return self();
		}

		public B settlementDate(LocalDate settlementDate) {
			this.settlementDate = settlementDate;
			return self();
		}

		public B amount(BigDecimal amount) {
			this.amount = amount;
			return self();
		}

		public B currency(Currency currency) {
			this.currency = currency;
			return self();
		}

		public B counterparty(LegalEntity counterparty) {
			this.counterparty = counterparty;
			return self();
		}

		public B book(Book book) {
			this.book = book;
			return self();
		}

		public B status(Status status) {
			this.status = status;
			return self();
		}

		public B workflow(String workflow) {
			this.workflow = workflow;
			return self();
		}

		public B buySell(boolean buySell) {
			this.buySell = buySell;
			return self();
		}
	}

}