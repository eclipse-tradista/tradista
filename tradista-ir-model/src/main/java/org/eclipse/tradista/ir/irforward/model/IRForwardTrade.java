package org.eclipse.tradista.ir.irforward.model;

import java.time.LocalDate;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.daycountconvention.model.DayCountConvention;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.model.Trade;

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

public class IRForwardTrade<P extends Product> extends Trade<P> {

	private static final long serialVersionUID = 429312965675171534L;

	/**
	 * @deprecated use {@link Builder} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public IRForwardTrade() {
	}

	protected IRForwardTrade(Builder<P, ?, ?> builder) {
		super(builder);
		this.maturityDate = builder.maturityDate;
		this.frequency = builder.frequency;
		this.interestPayment = builder.interestPayment;
		this.interestFixing = builder.interestFixing;
		this.referenceRateIndex = builder.referenceRateIndex;
		this.referenceRateIndexTenor = builder.referenceRateIndexTenor;
		this.dayCountConvention = builder.dayCountConvention;
	}

	private LocalDate maturityDate;

	private Tenor frequency;

	private InterestPayment interestPayment;

	private InterestPayment interestFixing;

	private Index referenceRateIndex;

	private Tenor referenceRateIndexTenor;

	private DayCountConvention dayCountConvention;

	public static final String IR_FORWARD = "IRForward";

	public InterestPayment getInterestPayment() {
		return interestPayment;
	}

	public void setInterestPayment(InterestPayment interestPayment) {
		this.interestPayment = interestPayment;
	}

	public InterestPayment getInterestFixing() {
		return interestFixing;
	}

	public void setInterestFixing(InterestPayment interestFixing) {
		this.interestFixing = interestFixing;
	}

	public DayCountConvention getDayCountConvention() {
		return dayCountConvention;
	}

	public void setDayCountConvention(DayCountConvention dayCountConvention) {
		this.dayCountConvention = dayCountConvention;
	}

	public Tenor getFrequency() {
		return frequency;
	}

	public void setFrequency(Tenor frequency) {
		this.frequency = frequency;
	}

	public Index getReferenceRateIndex() {
		return TradistaModelUtil.clone(referenceRateIndex);
	}

	public Tenor getReferenceRateIndexTenor() {
		return referenceRateIndexTenor;
	}

	public LocalDate getMaturityDate() {
		return maturityDate;
	}

	public void setMaturityDate(LocalDate maturityDate) {
		this.maturityDate = maturityDate;
	}

	public void setReferenceRateIndex(Index referenceRateIndex) {
		this.referenceRateIndex = referenceRateIndex;
	}

	public void setReferenceRateIndexTenor(Tenor referenceRateIndexTenor) {
		this.referenceRateIndexTenor = referenceRateIndexTenor;
	}

	public String getProductType() {
		return IR_FORWARD;
	}

	@Override
	public IRForwardTrade<P> clone() {
		IRForwardTrade<P> irForwardTrade = (IRForwardTrade<P>) super.clone();
		irForwardTrade.referenceRateIndex = TradistaModelUtil.clone(referenceRateIndex);
		return irForwardTrade;
	}

	@Override
	public Builder<P, ?, ?> toBuilder() {
		ConcreteBuilder<P> builder = new ConcreteBuilder<>();
		builder.id(getId()).creationTime(getCreationTime()).lastUpdateTime(getLastUpdateTime())
				.product(getProduct()).tradeDate(getTradeDate()).settlementDate(getSettlementDate())
				.amount(getAmount()).currency(getCurrency()).counterparty(getCounterparty())
				.book(getBook()).status(getStatus()).workflow(getWorkflow()).buySell(isBuy())
				.maturityDate(this.maturityDate).frequency(this.frequency)
				.interestPayment(this.interestPayment).interestFixing(this.interestFixing)
				.referenceRateIndex(this.referenceRateIndex)
				.referenceRateIndexTenor(this.referenceRateIndexTenor)
				.dayCountConvention(this.dayCountConvention);
		return builder;
	}

	public abstract static class Builder<P extends Product, T extends IRForwardTrade<P>, B extends Builder<P, T, B>>
			extends Trade.Builder<P, T, B> {
		protected LocalDate maturityDate;
		protected Tenor frequency;
		protected InterestPayment interestPayment;
		protected InterestPayment interestFixing;
		protected Index referenceRateIndex;
		protected Tenor referenceRateIndexTenor;
		protected DayCountConvention dayCountConvention;

		public B maturityDate(LocalDate maturityDate) {
			this.maturityDate = maturityDate;
			return self();
		}

		public B frequency(Tenor frequency) {
			this.frequency = frequency;
			return self();
		}

		public B interestPayment(InterestPayment interestPayment) {
			this.interestPayment = interestPayment;
			return self();
		}

		public B interestFixing(InterestPayment interestFixing) {
			this.interestFixing = interestFixing;
			return self();
		}

		public B referenceRateIndex(Index referenceRateIndex) {
			this.referenceRateIndex = referenceRateIndex;
			return self();
		}

		public B referenceRateIndexTenor(Tenor referenceRateIndexTenor) {
			this.referenceRateIndexTenor = referenceRateIndexTenor;
			return self();
		}

		public B dayCountConvention(DayCountConvention dayCountConvention) {
			this.dayCountConvention = dayCountConvention;
			return self();
		}
	}

	public static class ConcreteBuilder<P extends Product>
			extends Builder<P, IRForwardTrade<P>, ConcreteBuilder<P>> {
		@Override
		protected ConcreteBuilder<P> self() {
			return this;
		}

		@Override
		public IRForwardTrade<P> build() {
			return new IRForwardTrade<>(this);
		}
	}

}