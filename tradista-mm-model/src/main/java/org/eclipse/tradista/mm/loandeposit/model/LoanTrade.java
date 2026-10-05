package org.eclipse.tradista.mm.loandeposit.model;

import java.time.Instant;

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
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

public class LoanTrade extends LoanDepositTrade {

	public static final String LOAN = "Loan";

	private static final long serialVersionUID = -8199502969460190094L;

	/**
	 * @deprecated use {@link Builder} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public LoanTrade() {
	}

	protected LoanTrade(Builder builder) {
		super(builder);
	}

	@Override
	public String getProductType() {
		return LOAN;
	}

	@Override
	public Builder toBuilder() {
		Builder builder = new Builder();
		builder.id(getId()).creationTime(getCreationTime()).lastUpdateTime(getLastUpdateTime()).product(getProduct())
				.tradeDate(getTradeDate()).settlementDate(getSettlementDate()).amount(getAmount())
				.currency(getCurrency()).counterparty(getCounterparty()).book(getBook()).status(getStatus())
				.workflow(getWorkflow()).buySell(isBuy()).fixedRate(getFixedRate())
				.floatingRateIndex(getFloatingRateIndex()).floatingRateIndexTenor(getFloatingRateIndexTenor())
				.dayCountConvention(getDayCountConvention()).paymentFrequency(getPaymentFrequency())
				.endDate(getEndDate()).fixingPeriod(getFixingPeriod()).spread(getSpread())
				.interestType(getInterestType()).compoundPeriod(getCompoundPeriod()).maturity(getMaturity())
				.interestPayment(getInterestPayment()).interestFixing(getInterestFixing());
		return builder;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static LoanTrade create() {
		return builder().build();
	}

	public static LoanTrade of(Instant creationTime) {
		return builder().creationTime(creationTime).build();
	}

	public static class Builder extends LoanDepositTrade.Builder<LoanTrade, Builder> {
		private Builder() {
		}

		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public LoanTrade build() {
			return new LoanTrade(this);
		}
	}

}