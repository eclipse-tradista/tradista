package org.eclipse.tradista.fx.fxswap.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

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
 * License for the specific language governing permissions and limitations
 * under the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
/**
 * Class representing FX Swaps.
 * 
 * @author Tradista
 * 
 *         Please note that : - getAmount() gets the quote amount of the spot
 *         leg - getCurrency() gets the quote currency
 *
 * @param <P>
 */
public class FXSwapTrade extends AbstractFXTrade<Product> {

	private static final long serialVersionUID = 6321516419712885556L;
	public static final String FX_SWAP = "FXSwap";

	private Currency currencyOne;

	private LocalDate settlementDateForward;

	private BigDecimal amountOneForward;

	private BigDecimal amountOneSpot;

	private BigDecimal amountTwoForward;

	/**
	 * @deprecated use {@link Builder} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public FXSwapTrade() {
	}

	protected FXSwapTrade(Builder builder) {
		super(builder);
		this.currencyOne = builder.currencyOne;
		this.settlementDateForward = builder.settlementDateForward;
		this.amountOneForward = builder.amountOneForward;
		this.amountOneSpot = builder.amountOneSpot;
		this.amountTwoForward = builder.amountTwoForward;
	}

	public Currency getCurrencyOne() {
		return TradistaModelUtil.clone(currencyOne);
	}

	public void setCurrencyOne(Currency currencyOne) {
		this.currencyOne = currencyOne;
	}

	public LocalDate getSettlementDateForward() {
		return settlementDateForward;
	}

	public void setSettlementDateForward(LocalDate settlementDateForward) {
		this.settlementDateForward = settlementDateForward;
	}

	public BigDecimal getAmountOneForward() {
		return amountOneForward;
	}

	public void setAmountOneForward(BigDecimal amountOneForward) {
		this.amountOneForward = amountOneForward;
	}

	public BigDecimal getAmountOneSpot() {
		return amountOneSpot;
	}

	public void setAmountOneSpot(BigDecimal amountOneSpot) {
		this.amountOneSpot = amountOneSpot;
	}

	public BigDecimal getAmountTwoForward() {
		return amountTwoForward;
	}

	public void setAmountTwoForward(BigDecimal amountTwoForward) {
		this.amountTwoForward = amountTwoForward;
	}

	public String getProductType() {
		return FX_SWAP;
	}

	@Override
	public FXSwapTrade clone() {
		FXSwapTrade fxSwapTrade = (FXSwapTrade) super.clone();
		fxSwapTrade.currencyOne = TradistaModelUtil.clone(currencyOne);
		return fxSwapTrade;
	}

	@Override
	public Builder toBuilder() {
		Builder builder = new Builder();
		builder.id(getId()).creationTime(getCreationTime()).lastUpdateTime(getLastUpdateTime()).product(getProduct())
				.tradeDate(getTradeDate()).settlementDate(getSettlementDate()).amount(getAmount())
				.currency(getCurrency()).counterparty(getCounterparty()).book(getBook()).status(getStatus())
				.workflow(getWorkflow()).buySell(isBuy()).currencyOne(this.currencyOne)
				.settlementDateForward(this.settlementDateForward).amountOneForward(this.amountOneForward)
				.amountOneSpot(this.amountOneSpot).amountTwoForward(this.amountTwoForward);
		return builder;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static FXSwapTrade create() {
		return builder().build();
	}

	public static FXSwapTrade of(Instant creationTime) {
		return builder().creationTime(creationTime).build();
	}

	public static class Builder extends AbstractFXTrade.Builder<Product, FXSwapTrade, Builder> {
		private Builder() {
		}

		protected Currency currencyOne;
		protected LocalDate settlementDateForward;
		protected BigDecimal amountOneForward;
		protected BigDecimal amountOneSpot;
		protected BigDecimal amountTwoForward;

		public Builder currencyOne(Currency currencyOne) {
			this.currencyOne = currencyOne;
			return this;
		}

		public Builder settlementDateForward(LocalDate settlementDateForward) {
			this.settlementDateForward = settlementDateForward;
			return this;
		}

		public Builder amountOneForward(BigDecimal amountOneForward) {
			this.amountOneForward = amountOneForward;
			return this;
		}

		public Builder amountOneSpot(BigDecimal amountOneSpot) {
			this.amountOneSpot = amountOneSpot;
			return this;
		}

		public Builder amountTwoForward(BigDecimal amountTwoForward) {
			this.amountTwoForward = amountTwoForward;
			return this;
		}

		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public FXSwapTrade build() {
			return new FXSwapTrade(this);
		}
	}

}