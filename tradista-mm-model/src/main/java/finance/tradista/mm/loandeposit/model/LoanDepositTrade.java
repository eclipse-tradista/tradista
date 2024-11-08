package org.eclipse.tradista.mm.loandeposit.model;

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
 * Copyright (c) 2015 Olivier Asuncion
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
 * Amount is nominal. Settle date is the Loan start date.
 * 
 * @author OA
 * 
 * @param <P>
 */
public abstract class LoanDepositTrade extends Trade<Product> {

	public static String LOAN_DEPOSIT = "LoanDeposit";

	/**
	 * 
	 */
	private static final long serialVersionUID = 3579586407106122139L;

	public static enum Direction {
		LOAN, DEPOSIT;

		public String toString() {
			switch (this) {
			case LOAN:
				return LoanTrade.LOAN;
			case DEPOSIT:
				return DepositTrade.DEPOSIT;
			}
			return super.toString();
		}
	};

	public static enum InterestType {
		SIMPLE, COMPOUND;

		public String toString() {
			switch (this) {
			case SIMPLE:
				return "Simple";
			case COMPOUND:
				return "Compound";
			}
			return super.toString();
		}
	};

	private BigDecimal fixedRate;
	private Index floatingRateIndex;
	private Tenor floatingRateIndexTenor;
	private DayCountConvention dayCountConvention;
	private Tenor paymentFrequency;
	private LocalDate endDate;
	private Tenor fixingPeriod;
	private BigDecimal spread;
	private InterestType interestType;
	private Tenor compoundPeriod;
	private Tenor maturity;
	private InterestPayment interestPayment;
	private InterestPayment interestFixing;

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

	public Tenor getFixingPeriod() {
		return fixingPeriod;
	}

	public void setFixingPeriod(Tenor fixingPeriod) {
		this.fixingPeriod = fixingPeriod;
	}

	public BigDecimal getSpread() {
		return spread;
	}

	public void setSpread(BigDecimal spread) {
		this.spread = spread;
	}

	public BigDecimal getFixedRate() {
		return fixedRate;
	}

	public void setFixedRate(BigDecimal fixedRate) {
		this.fixedRate = fixedRate;
	}

	public Index getFloatingRateIndex() {
		return TradistaModelUtil.clone(floatingRateIndex);
	}

	public void setFloatingRateIndex(Index floatingRateIndex) {
		this.floatingRateIndex = floatingRateIndex;
	}

	public Tenor getFloatingRateIndexTenor() {
		return floatingRateIndexTenor;
	}

	public void setFloatingRateIndexTenor(Tenor floatingRateIndexTenor) {
		this.floatingRateIndexTenor = floatingRateIndexTenor;
	}

	public DayCountConvention getDayCountConvention() {
		return dayCountConvention;
	}

	public void setDayCountConvention(DayCountConvention dayCountConvention) {
		this.dayCountConvention = dayCountConvention;
	}

	public Tenor getPaymentFrequency() {
		return paymentFrequency;
	}

	public void setPaymentFrequency(Tenor paymentFrequency) {
		this.paymentFrequency = paymentFrequency;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public boolean isFixed() {
		return (floatingRateIndex == null);
	}

	public InterestType getInterestType() {
		return interestType;
	}

	public void setInterestType(InterestType interestType) {
		this.interestType = interestType;
	}

	public Tenor getCompoundPeriod() {
		return compoundPeriod;
	}

	public void setCompoundPeriod(Tenor compoundPeriod) {
		this.compoundPeriod = compoundPeriod;
	}

	public Tenor getMaturity() {
		return maturity;
	}

	public void setMaturity(Tenor maturity) {
		this.maturity = maturity;
	}

	public boolean isSimpleInterest() {
		return (interestType != null && interestType.equals(InterestType.SIMPLE));
	}

	public boolean isCompoundInterest() {
		return (interestType != null && interestType.equals(InterestType.COMPOUND));
	}

	@Override
	public LoanDepositTrade clone() {
		LoanDepositTrade loanDepositTrade = (LoanDepositTrade) super.clone();
		loanDepositTrade.floatingRateIndex = TradistaModelUtil.clone(floatingRateIndex);
		return loanDepositTrade;
	}

}