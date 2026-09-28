package org.eclipse.tradista.ir.irswapoption.model;

import java.math.BigDecimal;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.ir.irswap.model.SingleCurrencyIRSwapTrade;

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

public class IRSwapOptionTrade extends VanillaOptionTrade<SingleCurrencyIRSwapTrade> {

	private static final long serialVersionUID = 8952352869490542697L;

	/**
	 * @deprecated use {@link Builder} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public IRSwapOptionTrade() {
	}

	protected IRSwapOptionTrade(Builder builder) {
		super(builder);
		this.cashSettlementAmount = builder.cashSettlementAmount;
		this.alternativeCashSettlementReferenceRateIndex = builder.alternativeCashSettlementReferenceRateIndex;
		this.alternativeCashSettlementReferenceRateIndexTenor = builder.alternativeCashSettlementReferenceRateIndexTenor;
	}

	public static final String IR_SWAP_OPTION = "IRSwapOption";

	private BigDecimal cashSettlementAmount;

	private Index alternativeCashSettlementReferenceRateIndex;

	private Tenor alternativeCashSettlementReferenceRateIndexTenor;

	public BigDecimal getCashSettlementAmount() {
		return cashSettlementAmount;
	}

	public void setCashSettlementAmount(BigDecimal cashSettlementAmount) {
		this.cashSettlementAmount = cashSettlementAmount;
	}

	public Index getAlternativeCashSettlementReferenceRateIndex() {
		return TradistaModelUtil.clone(alternativeCashSettlementReferenceRateIndex);
	}

	public void setAlternativeCashSettlementReferenceRateIndex(Index alternativeCashSettlementReferenceRateIndex) {
		this.alternativeCashSettlementReferenceRateIndex = alternativeCashSettlementReferenceRateIndex;
	}

	public Tenor getAlternativeCashSettlementReferenceRateIndexTenor() {
		return alternativeCashSettlementReferenceRateIndexTenor;
	}

	public void setAlternativeCashSettlementReferenceRateIndexTenor(
			Tenor alternativeCashSettlementReferenceRateIndexTenor) {
		this.alternativeCashSettlementReferenceRateIndexTenor = alternativeCashSettlementReferenceRateIndexTenor;
	}

	@Override
	public String getProductType() {
		return IR_SWAP_OPTION;
	}

	@Override
	public IRSwapOptionTrade clone() {
		IRSwapOptionTrade irSwapOptionTrade = (IRSwapOptionTrade) super.clone();
		irSwapOptionTrade.alternativeCashSettlementReferenceRateIndex = TradistaModelUtil
				.clone(alternativeCashSettlementReferenceRateIndex);
		return irSwapOptionTrade;
	}

	@Override
	public Builder toBuilder() {
		Builder builder = new Builder();
		builder.id(getId()).creationTime(getCreationTime()).lastUpdateTime(getLastUpdateTime())
				.product(getProduct()).tradeDate(getTradeDate()).settlementDate(getSettlementDate())
				.amount(getAmount()).currency(getCurrency()).counterparty(getCounterparty())
				.book(getBook()).status(getStatus()).workflow(getWorkflow()).buySell(isBuy())
				.type(getType()).underlying(getUnderlying()).maturityDate(getMaturityDate())
				.strike(getStrike()).settlementType(getSettlementType())
				.settlementDateOffset(getSettlementDateOffset()).exerciseDate(getExerciseDate())
				.style(getStyle()).cashSettlementAmount(this.cashSettlementAmount)
				.alternativeCashSettlementReferenceRateIndex(this.alternativeCashSettlementReferenceRateIndex)
				.alternativeCashSettlementReferenceRateIndexTenor(this.alternativeCashSettlementReferenceRateIndexTenor);
		return builder;
	}

	public static class Builder extends
			VanillaOptionTrade.Builder<SingleCurrencyIRSwapTrade, IRSwapOptionTrade, Builder> {
		protected BigDecimal cashSettlementAmount;
		protected Index alternativeCashSettlementReferenceRateIndex;
		protected Tenor alternativeCashSettlementReferenceRateIndexTenor;

		public Builder cashSettlementAmount(BigDecimal cashSettlementAmount) {
			this.cashSettlementAmount = cashSettlementAmount;
			return this;
		}

		public Builder alternativeCashSettlementReferenceRateIndex(
				Index alternativeCashSettlementReferenceRateIndex) {
			this.alternativeCashSettlementReferenceRateIndex = alternativeCashSettlementReferenceRateIndex;
			return this;
		}

		public Builder alternativeCashSettlementReferenceRateIndexTenor(
				Tenor alternativeCashSettlementReferenceRateIndexTenor) {
			this.alternativeCashSettlementReferenceRateIndexTenor = alternativeCashSettlementReferenceRateIndexTenor;
			return this;
		}

		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public IRSwapOptionTrade build() {
			return new IRSwapOptionTrade(this);
		}
	}

}