package org.eclipse.tradista.fx.fx.model;

import java.math.BigDecimal;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.fx.common.model.AbstractFXTrade;

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

public class FXTrade extends AbstractFXTrade<Product> {

	private static final long serialVersionUID = 8313585047177367366L;

	/**
	 * @deprecated use {@link Builder} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public FXTrade() {
	}

	protected FXTrade(Builder builder) {
		super(builder);
		this.currencyOne = builder.currencyOne;
		this.amountOne = builder.amountOne;
		this.type = builder.type;
	}

	public static final String FX = "FX";

	public enum Type {
		FX_SPOT, FX_FORWARD;

		public String toString() {
			switch (this) {
			case FX_SPOT:
				return "FX Spot";
			case FX_FORWARD:
				return "FX Forward";
			}
			return super.toString();
		}
	};

	private Currency currencyOne;

	private BigDecimal amountOne;

	private Type type;

	public void setType(Type type) {
		this.type = type;
	}

	public Currency getCurrencyOne() {
		return TradistaModelUtil.clone(currencyOne);
	}

	public void setCurrencyOne(Currency currencyOne) {
		this.currencyOne = currencyOne;
	}

	public BigDecimal getAmountOne() {
		return amountOne;
	}

	public void setAmountOne(BigDecimal amountOne) {
		this.amountOne = amountOne;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String getProductType() {
		if (getTradeDate() == null) {
			return "Option underlying";
		}
		if (getType() == null) {
			// Ensure to determnine type using FXTradeBusinessDelegate.determinateType
			// before calling getProductType()
			return "Unspecified FX trade";
		}
		switch (getType()) {
		case FX_SPOT:
			return "FXSpot";

		case FX_FORWARD:
			return "FXForward";
		}

		return "Unspecified FX trade";
	}

	@Override
	public FXTrade clone() {
		FXTrade fxTrade = (FXTrade) super.clone();
		fxTrade.currencyOne = TradistaModelUtil.clone(currencyOne);
		return fxTrade;
	}

	@Override
	public Builder toBuilder() {
		Builder builder = new Builder();
		builder.id(getId()).creationTime(getCreationTime()).lastUpdateTime(getLastUpdateTime()).product(getProduct())
				.tradeDate(getTradeDate()).settlementDate(getSettlementDate()).amount(getAmount())
				.currency(getCurrency()).counterparty(getCounterparty()).book(getBook()).status(getStatus())
				.workflow(getWorkflow()).buySell(isBuy()).currencyOne(this.currencyOne).amountOne(this.amountOne)
				.type(this.type);
		return builder;
	}

	public static class Builder extends AbstractFXTrade.Builder<Product, FXTrade, Builder> {
		protected Currency currencyOne;
		protected BigDecimal amountOne;
		protected Type type;

		public Builder currencyOne(Currency currencyOne) {
			this.currencyOne = currencyOne;
			return this;
		}

		public Builder amountOne(BigDecimal amountOne) {
			this.amountOne = amountOne;
			return this;
		}

		public Builder type(Type type) {
			this.type = type;
			return this;
		}

		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public FXTrade build() {
			return new FXTrade(this);
		}
	}

}