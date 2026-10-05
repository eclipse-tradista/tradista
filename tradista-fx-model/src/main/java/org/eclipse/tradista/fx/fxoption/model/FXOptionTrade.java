package org.eclipse.tradista.fx.fxoption.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.fx.fx.model.FXTrade;

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

public class FXOptionTrade extends VanillaOptionTrade<FXTrade> {

	public static final String FX_OPTION = "FXOption";

	private static final long serialVersionUID = 8952352869490542697L;

	/**
	 * @deprecated use {@link Builder} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public FXOptionTrade() {
	}

	protected FXOptionTrade(Builder builder) {
		super(builder);
	}

	/**
	 * For FX Options, the strike is the following rate : Quote Amount / Primary
	 * Amount (the primary amount being the one to be bought).
	 */
	@Override
	public BigDecimal getStrike() {
		FXTrade underlying = getUnderlying();
		if (underlying != null && underlying.getAmount() != null && underlying.getAmountOne() != null
				&& underlying.getAmountOne().signum() != 0) {
			return underlying.getAmount().divide(underlying.getAmountOne(), RoundingMode.HALF_EVEN);
		} else {
			return null;
		}
	}

	@Override
	public Exchange getExchange() {
		// Exchange is the one of FX
		return FXTrade.create().getExchange();
	}

	@Override
	public String getProductType() {
		return FX_OPTION;
	}

	@Override
	public Builder toBuilder() {
		Builder builder = new Builder();
		builder.id(getId()).creationTime(getCreationTime()).lastUpdateTime(getLastUpdateTime()).product(getProduct())
				.tradeDate(getTradeDate()).settlementDate(getSettlementDate()).amount(getAmount())
				.currency(getCurrency()).counterparty(getCounterparty()).book(getBook()).status(getStatus())
				.workflow(getWorkflow()).buySell(isBuy()).type(getType()).underlying(getUnderlying())
				.maturityDate(getMaturityDate()).strike(getStrike()).settlementType(getSettlementType())
				.settlementDateOffset(getSettlementDateOffset()).exerciseDate(getExerciseDate()).style(getStyle());
		return builder;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static FXOptionTrade create() {
		return builder().build();
	}

	public static FXOptionTrade of(Instant creationTime) {
		return builder().creationTime(creationTime).build();
	}

	public static class Builder extends VanillaOptionTrade.Builder<FXTrade, FXOptionTrade, Builder> {
		private Builder() {
		}

		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public FXOptionTrade build() {
			return new FXOptionTrade(this);
		}
	}

}