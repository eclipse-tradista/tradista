package org.eclipse.tradista.security.equity.model;

import java.math.BigDecimal;

import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.trade.model.Trade;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
/**
 * Class representing a trade on Equity. Amount represents the unit price of a
 * equity for this deal. *
 * 
 * @param <B> the traded equity.
 */
public class EquityTrade extends Trade<Equity> {

	private static final long serialVersionUID = 834419757097799136L;

	private BigDecimal quantity;

	/**
	 * @deprecated use {@link Builder} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public EquityTrade(Equity product) {
		super(product);
	}

	/**
	 * @deprecated use {@link Builder} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public EquityTrade() {
	}

	protected EquityTrade(Builder builder) {
		super(builder);
		this.quantity = builder.quantity;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public Currency getCurrency() {
		if (getProduct() != null) {
			return ((Equity) getProduct()).getCurrency();
		}
		return null;
	}

	@Override
	public Builder toBuilder() {
		Builder builder = new Builder();
		builder.id(getId()).creationTime(getCreationTime()).lastUpdateTime(getLastUpdateTime())
				.product(getProduct()).tradeDate(getTradeDate()).settlementDate(getSettlementDate())
				.amount(getAmount()).currency(getCurrency()).counterparty(getCounterparty())
				.book(getBook()).status(getStatus()).workflow(getWorkflow()).buySell(isBuy())
				.quantity(this.quantity);
		return builder;
	}

	public static class Builder extends Trade.Builder<Equity, EquityTrade, Builder> {
		protected BigDecimal quantity;

		public Builder quantity(BigDecimal quantity) {
			this.quantity = quantity;
			return this;
		}

		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public EquityTrade build() {
			return new EquityTrade(this);
		}
	}

}
