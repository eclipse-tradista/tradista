package org.eclipse.tradista.ir.irswap.model;

import java.math.BigDecimal;
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

public abstract class IRSwapTrade extends Trade<Product> {

	private static final long serialVersionUID = -6188291608649255466L;

	/**
	 * @deprecated use {@link Builder} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public IRSwapTrade() {
	}

	protected IRSwapTrade(Builder<?, ?> builder) {
		super(builder);
		this.maturityDate = builder.maturityDate;
		this.maturityTenor = builder.maturityTenor;
		this.paymentFrequency = builder.paymentFrequency;
		this.receptionFrequency = builder.receptionFrequency;
		this.paymentInterestPayment = builder.paymentInterestPayment;
		this.receptionInterestPayment = builder.receptionInterestPayment;
		this.paymentInterestFixing = builder.paymentInterestFixing;
		this.receptionInterestFixing = builder.receptionInterestFixing;
		this.paymentReferenceRateIndexTenor = builder.paymentReferenceRateIndexTenor;
		this.receptionReferenceRateIndexTenor = builder.receptionReferenceRateIndexTenor;
		this.receptionReferenceRateIndex = builder.receptionReferenceRateIndex;
		this.paymentReferenceRateIndex = builder.paymentReferenceRateIndex;
		this.paymentSpread = builder.paymentSpread;
		this.receptionSpread = builder.receptionSpread;
		this.paymentFixedInterestRate = builder.paymentFixedInterestRate;
		this.interestsToPayFixed = builder.interestsToPayFixed;
		this.paymentDayCountConvention = builder.paymentDayCountConvention;
		this.receptionDayCountConvention = builder.receptionDayCountConvention;
	}

	public static final String IR_SWAP = "IRSwap";

	protected LocalDate maturityDate;

	protected Tenor maturityTenor;

	protected Tenor paymentFrequency;

	protected Tenor receptionFrequency;

	protected InterestPayment paymentInterestPayment;

	protected InterestPayment receptionInterestPayment;

	protected InterestPayment paymentInterestFixing;

	protected InterestPayment receptionInterestFixing;

	protected Tenor paymentReferenceRateIndexTenor;

	protected Tenor receptionReferenceRateIndexTenor;

	protected Index receptionReferenceRateIndex;

	protected Index paymentReferenceRateIndex;

	protected BigDecimal paymentSpread;

	protected BigDecimal receptionSpread;

	protected BigDecimal paymentFixedInterestRate;

	protected boolean interestsToPayFixed;

	protected DayCountConvention paymentDayCountConvention;

	protected DayCountConvention receptionDayCountConvention;

	public InterestPayment getPaymentInterestPayment() {
		return paymentInterestPayment;
	}

	public void setPaymentInterestPayment(InterestPayment paymentInterestPayment) {
		this.paymentInterestPayment = paymentInterestPayment;
	}

	public InterestPayment getReceptionInterestPayment() {
		return receptionInterestPayment;
	}

	public void setReceptionInterestPayment(InterestPayment receptionInterestPayment) {
		this.receptionInterestPayment = receptionInterestPayment;
	}

	public DayCountConvention getPaymentDayCountConvention() {
		return paymentDayCountConvention;
	}

	public void setPaymentDayCountConvention(DayCountConvention paymentDayCountConvention) {
		this.paymentDayCountConvention = paymentDayCountConvention;
	}

	public DayCountConvention getReceptionDayCountConvention() {
		return receptionDayCountConvention;
	}

	public void setReceptionDayCountConvention(DayCountConvention receptionDayCountConvention) {
		this.receptionDayCountConvention = receptionDayCountConvention;
	}

	public boolean isInterestsToPayFixed() {
		return interestsToPayFixed;
	}

	public void setInterestsToPayFixed(boolean interestsToPayFixed) {
		this.interestsToPayFixed = interestsToPayFixed;
	}

	public LocalDate getMaturityDate() {
		return maturityDate;
	}

	public void setMaturityDate(LocalDate maturityDate) {
		this.maturityDate = maturityDate;
	}

	public Tenor getMaturityTenor() {
		return maturityTenor;
	}

	public void setMaturityTenor(Tenor maturityTenor) {
		this.maturityTenor = maturityTenor;
	}

	public BigDecimal getPaymentFixedInterestRate() {
		return paymentFixedInterestRate;
	}

	public Tenor getPaymentFrequency() {
		return paymentFrequency;
	}

	public void setPaymentFrequency(Tenor paymentFrequency) {
		this.paymentFrequency = paymentFrequency;
	}

	public Tenor getReceptionFrequency() {
		return receptionFrequency;
	}

	public void setReceptionFrequency(Tenor receptionFrequency) {
		this.receptionFrequency = receptionFrequency;
	}

	public Tenor getPaymentReferenceRateIndexTenor() {
		return paymentReferenceRateIndexTenor;
	}

	public void setPaymentReferenceRateIndexTenor(Tenor paymentReferenceRateIndexTenor) {
		this.paymentReferenceRateIndexTenor = paymentReferenceRateIndexTenor;
	}

	public Tenor getReceptionReferenceRateIndexTenor() {
		return receptionReferenceRateIndexTenor;
	}

	public void setReceptionReferenceRateIndexTenor(Tenor receptionReferenceRateIndexTenor) {
		this.receptionReferenceRateIndexTenor = receptionReferenceRateIndexTenor;
	}

	public Index getReceptionReferenceRateIndex() {
		return TradistaModelUtil.clone(receptionReferenceRateIndex);
	}

	public void setReceptionReferenceRateIndex(Index receptionReferenceRateIndex) {
		this.receptionReferenceRateIndex = receptionReferenceRateIndex;
	}

	public Index getPaymentReferenceRateIndex() {
		return TradistaModelUtil.clone(paymentReferenceRateIndex);
	}

	public void setPaymentReferenceRateIndex(Index paymentReferenceRateIndex) {
		this.paymentReferenceRateIndex = paymentReferenceRateIndex;
	}

	public BigDecimal getPaymentSpread() {
		return paymentSpread;
	}

	public void setPaymentSpread(BigDecimal paymentSpread) {
		this.paymentSpread = paymentSpread;
	}

	public BigDecimal getReceptionSpread() {
		return receptionSpread;
	}

	public void setReceptionSpread(BigDecimal receptionSpread) {
		this.receptionSpread = receptionSpread;
	}

	public void setPaymentFixedInterestRate(BigDecimal paymentFixedInterestRate) {
		this.paymentFixedInterestRate = paymentFixedInterestRate;
	}

	public InterestPayment getPaymentInterestFixing() {
		return paymentInterestFixing;
	}

	public void setPaymentInterestFixing(InterestPayment paymentInterestFixing) {
		this.paymentInterestFixing = paymentInterestFixing;
	}

	public InterestPayment getReceptionInterestFixing() {
		return receptionInterestFixing;
	}

	public void setReceptionInterestFixing(InterestPayment receptionInterestFixing) {
		this.receptionInterestFixing = receptionInterestFixing;
	}

	@Override
	public IRSwapTrade clone() {
		IRSwapTrade irSwapTrade = (IRSwapTrade) super.clone();
		irSwapTrade.receptionReferenceRateIndex = TradistaModelUtil.clone(receptionReferenceRateIndex);
		irSwapTrade.paymentReferenceRateIndex = TradistaModelUtil.clone(paymentReferenceRateIndex);
		return irSwapTrade;
	}

	public abstract static class Builder<T extends IRSwapTrade, B extends Builder<T, B>>
			extends Trade.Builder<Product, T, B> {
		protected LocalDate maturityDate;
		protected Tenor maturityTenor;
		protected Tenor paymentFrequency;
		protected Tenor receptionFrequency;
		protected InterestPayment paymentInterestPayment;
		protected InterestPayment receptionInterestPayment;
		protected InterestPayment paymentInterestFixing;
		protected InterestPayment receptionInterestFixing;
		protected Tenor paymentReferenceRateIndexTenor;
		protected Tenor receptionReferenceRateIndexTenor;
		protected Index receptionReferenceRateIndex;
		protected Index paymentReferenceRateIndex;
		protected BigDecimal paymentSpread;
		protected BigDecimal receptionSpread;
		protected BigDecimal paymentFixedInterestRate;
		protected boolean interestsToPayFixed;
		protected DayCountConvention paymentDayCountConvention;
		protected DayCountConvention receptionDayCountConvention;

		public B maturityDate(LocalDate maturityDate) {
			this.maturityDate = maturityDate;
			return self();
		}

		public B maturityTenor(Tenor maturityTenor) {
			this.maturityTenor = maturityTenor;
			return self();
		}

		public B paymentFrequency(Tenor paymentFrequency) {
			this.paymentFrequency = paymentFrequency;
			return self();
		}

		public B receptionFrequency(Tenor receptionFrequency) {
			this.receptionFrequency = receptionFrequency;
			return self();
		}

		public B paymentInterestPayment(InterestPayment paymentInterestPayment) {
			this.paymentInterestPayment = paymentInterestPayment;
			return self();
		}

		public B receptionInterestPayment(InterestPayment receptionInterestPayment) {
			this.receptionInterestPayment = receptionInterestPayment;
			return self();
		}

		public B paymentInterestFixing(InterestPayment paymentInterestFixing) {
			this.paymentInterestFixing = paymentInterestFixing;
			return self();
		}

		public B receptionInterestFixing(InterestPayment receptionInterestFixing) {
			this.receptionInterestFixing = receptionInterestFixing;
			return self();
		}

		public B paymentReferenceRateIndexTenor(Tenor paymentReferenceRateIndexTenor) {
			this.paymentReferenceRateIndexTenor = paymentReferenceRateIndexTenor;
			return self();
		}

		public B receptionReferenceRateIndexTenor(Tenor receptionReferenceRateIndexTenor) {
			this.receptionReferenceRateIndexTenor = receptionReferenceRateIndexTenor;
			return self();
		}

		public B receptionReferenceRateIndex(Index receptionReferenceRateIndex) {
			this.receptionReferenceRateIndex = receptionReferenceRateIndex;
			return self();
		}

		public B paymentReferenceRateIndex(Index paymentReferenceRateIndex) {
			this.paymentReferenceRateIndex = paymentReferenceRateIndex;
			return self();
		}

		public B paymentSpread(BigDecimal paymentSpread) {
			this.paymentSpread = paymentSpread;
			return self();
		}

		public B receptionSpread(BigDecimal receptionSpread) {
			this.receptionSpread = receptionSpread;
			return self();
		}

		public B paymentFixedInterestRate(BigDecimal paymentFixedInterestRate) {
			this.paymentFixedInterestRate = paymentFixedInterestRate;
			return self();
		}

		public B interestsToPayFixed(boolean interestsToPayFixed) {
			this.interestsToPayFixed = interestsToPayFixed;
			return self();
		}

		public B paymentDayCountConvention(DayCountConvention paymentDayCountConvention) {
			this.paymentDayCountConvention = paymentDayCountConvention;
			return self();
		}

		public B receptionDayCountConvention(DayCountConvention receptionDayCountConvention) {
			this.receptionDayCountConvention = receptionDayCountConvention;
			return self();
		}
	}

}