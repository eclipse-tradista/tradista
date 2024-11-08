package org.eclipse.tradista.core.cashflow.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;

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

public class CashFlow extends TradistaObject implements Comparable<CashFlow> {

	private static final long serialVersionUID = 7017846262097274047L;

	private BigDecimal amount;

	private BigDecimal discountedAmount;

	private BigDecimal discountFactor;

	@Id
	private LocalDate date;

	@Id
	private Currency currency;

	@Id
	private TransferPurpose purpose;

	private Direction direction;

	public static enum Direction {
		PAY, RECEIVE;

		public String toString() {
			switch (this) {
			case PAY:
				return "Pay";
			case RECEIVE:
				return "Receive";
			}
			return super.toString();
		}
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public Currency getCurrency() {
		return TradistaModelUtil.clone(currency);
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public TransferPurpose getPurpose() {
		return purpose;
	}

	public void setPurpose(TransferPurpose purpose) {
		this.purpose = purpose;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public BigDecimal getDiscountedAmount() {
		return discountedAmount;
	}

	public void setDiscountedAmount(BigDecimal discountedAmount) {
		this.discountedAmount = discountedAmount;
	}

	public BigDecimal getDiscountFactor() {
		return discountFactor;
	}

	public void setDiscountFactor(BigDecimal discountFactor) {
		this.discountFactor = discountFactor;
	}

	@Override
	public int compareTo(CashFlow cf) {
		if (cf == null) {
			return 1;
		}
		return date.compareTo(cf.getDate());
	}

	@Override
	public CashFlow clone() {
		CashFlow cf = (CashFlow) super.clone();
		cf.currency = TradistaModelUtil.clone(currency);
		return cf;
	}

}