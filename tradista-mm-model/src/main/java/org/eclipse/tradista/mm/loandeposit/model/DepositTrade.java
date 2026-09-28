package org.eclipse.tradista.mm.loandeposit.model;

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
public class DepositTrade extends LoanDepositTrade {

	public static final String DEPOSIT = "Deposit";

	private static final long serialVersionUID = 6139725225871550581L;

	/**
	 * @deprecated use {@link Builder} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public DepositTrade() {
	}

	protected DepositTrade(Builder builder) {
		super(builder);
	}

	public String getProductType() {
		return DEPOSIT;
	}

	@Override
	public Builder toBuilder() {
		Builder builder = new Builder();
		builder.id(getId()).creationTime(getCreationTime()).lastUpdateTime(getLastUpdateTime())
				.product(getProduct()).tradeDate(getTradeDate()).settlementDate(getSettlementDate())
				.amount(getAmount()).currency(getCurrency()).counterparty(getCounterparty())
				.book(getBook()).status(getStatus()).workflow(getWorkflow()).buySell(isBuy())
				.fixedRate(getFixedRate()).floatingRateIndex(getFloatingRateIndex())
				.floatingRateIndexTenor(getFloatingRateIndexTenor())
				.dayCountConvention(getDayCountConvention()).paymentFrequency(getPaymentFrequency())
				.endDate(getEndDate()).fixingPeriod(getFixingPeriod()).spread(getSpread())
				.interestType(getInterestType()).compoundPeriod(getCompoundPeriod())
				.maturity(getMaturity()).interestPayment(getInterestPayment())
				.interestFixing(getInterestFixing());
		return builder;
	}

	public static class Builder extends LoanDepositTrade.Builder<DepositTrade, Builder> {
		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public DepositTrade build() {
			return new DepositTrade(this);
		}
	}

}