package org.eclipse.tradista.security.common.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.product.model.Product;

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

public abstract class Security extends Product {

	private static final long serialVersionUID = 2413499120965894034L;

	@Id
	private String isin;
	private LegalEntity issuer;
	private LocalDate issueDate;
	private BigDecimal issuePrice;
	private Currency currency;

	public Security(Exchange exchange, String isin) {
		super(exchange);
		this.isin = isin;
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
}