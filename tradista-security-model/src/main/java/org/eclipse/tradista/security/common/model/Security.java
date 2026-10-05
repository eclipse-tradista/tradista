package org.eclipse.tradista.security.common.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.rating.model.Ratable;

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

public abstract class Security extends Product implements Ratable {

	private static final long serialVersionUID = 2413499120965894034L;

	@Id
	private String isin;
	private LegalEntity issuer;
	private LocalDate issueDate;
	private BigDecimal issuePrice;
	private Currency currency;

	/**
	 * @deprecated use {@link Builder} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public Security(Exchange exchange, String isin) {
		super(exchange);
		this.isin = isin;
	}

	protected Security(Builder<?, ?> builder) {
		super(builder);
		this.isin = builder.isin;
		this.issuer = builder.issuer;
		this.issueDate = builder.issueDate;
		this.issuePrice = builder.issuePrice;
		this.currency = builder.currency;
	}

	public String getIsin() {
		return isin;
	}

	public LegalEntity getIssuer() {
		return TradistaModelUtil.clone(issuer);
	}

	public void setIssuer(LegalEntity issuer) {
		this.issuer = issuer;
	}

	public long getIssuerId() {
		if (issuer != null) {
			return issuer.getId();
		}
		return 0;
	}

	public Currency getCurrency() {
		return TradistaModelUtil.clone(currency);
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public long getCurrencyId() {
		if (currency != null) {
			return currency.getId();
		} else {
			return 0;
		}
	}

	public LocalDate getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(LocalDate issueDate) {
		this.issueDate = issueDate;
	}

	public BigDecimal getIssuePrice() {
		return issuePrice;
	}

	public void setIssuePrice(BigDecimal issuePrice) {
		this.issuePrice = issuePrice;
	}

	@Override
	public String toString() {
		return getIsin() + " - " + getExchange();
	}

	@Override
	public Security clone() {
		Security security = (Security) super.clone();
		security.issuer = TradistaModelUtil.clone(issuer);
		security.currency = TradistaModelUtil.clone(currency);
		return security;
	}

	public abstract static class Builder<T extends Security, B extends Builder<T, B>> extends Product.Builder<T, B> {
		protected String isin;
		protected LegalEntity issuer;
		protected LocalDate issueDate;
		protected BigDecimal issuePrice;
		protected Currency currency;

		protected Builder(Exchange exchange, String isin) {
			this.exchange = exchange;
			this.isin = isin;
		}

		public B issuer(LegalEntity issuer) {
			this.issuer = issuer;
			return self();
		}

		public B issueDate(LocalDate issueDate) {
			this.issueDate = issueDate;
			return self();
		}

		public B issuePrice(BigDecimal issuePrice) {
			this.issuePrice = issuePrice;
			return self();
		}

		public B currency(Currency currency) {
			this.currency = currency;
			return self();
		}
	}
}