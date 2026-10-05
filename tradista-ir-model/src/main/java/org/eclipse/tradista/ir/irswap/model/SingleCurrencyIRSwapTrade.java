package org.eclipse.tradista.ir.irswap.model;

import java.time.Instant;

import org.eclipse.tradista.core.marketdata.model.Instrument;

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

public class SingleCurrencyIRSwapTrade extends IRSwapTrade implements Instrument {

	private static final long serialVersionUID = -6188291608649255466L;

	/**
	 * @deprecated use {@link Builder} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public SingleCurrencyIRSwapTrade() {
	}

	protected SingleCurrencyIRSwapTrade(Builder builder) {
		super(builder);
	}

	public static final String IR_SWAP = "IRSwap";

	public String getProductType() {
		return IR_SWAP;
	}

	@Override
	public String getInstrumentName() {
		return IR_SWAP;
	}

	@Override
	public Builder toBuilder() {
		Builder builder = new Builder();
		builder.id(getId()).creationTime(getCreationTime()).lastUpdateTime(getLastUpdateTime()).product(getProduct())
				.tradeDate(getTradeDate()).settlementDate(getSettlementDate()).amount(getAmount())
				.currency(getCurrency()).counterparty(getCounterparty()).book(getBook()).status(getStatus())
				.workflow(getWorkflow()).buySell(isBuy()).maturityDate(this.maturityDate)
				.maturityTenor(this.maturityTenor).paymentFrequency(this.paymentFrequency)
				.receptionFrequency(this.receptionFrequency).paymentInterestPayment(this.paymentInterestPayment)
				.receptionInterestPayment(this.receptionInterestPayment)
				.paymentInterestFixing(this.paymentInterestFixing).receptionInterestFixing(this.receptionInterestFixing)
				.paymentReferenceRateIndexTenor(this.paymentReferenceRateIndexTenor)
				.receptionReferenceRateIndexTenor(this.receptionReferenceRateIndexTenor)
				.receptionReferenceRateIndex(this.receptionReferenceRateIndex)
				.paymentReferenceRateIndex(this.paymentReferenceRateIndex).paymentSpread(this.paymentSpread)
				.receptionSpread(this.receptionSpread).paymentFixedInterestRate(this.paymentFixedInterestRate)
				.interestsToPayFixed(this.interestsToPayFixed).paymentDayCountConvention(this.paymentDayCountConvention)
				.receptionDayCountConvention(this.receptionDayCountConvention);
		return builder;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static SingleCurrencyIRSwapTrade create() {
		return builder().build();
	}

	public static SingleCurrencyIRSwapTrade of(Instant creationTime) {
		return builder().creationTime(creationTime).build();
	}

	public static class Builder extends IRSwapTrade.Builder<SingleCurrencyIRSwapTrade, Builder> {
		private Builder() {
		}

		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public SingleCurrencyIRSwapTrade build() {
			return new SingleCurrencyIRSwapTrade(this);
		}
	}

}