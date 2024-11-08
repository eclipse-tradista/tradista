package org.eclipse.tradista.ai.agent.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.currency.model.Currency;

/********************************************************************************
 * Copyright (c) 2017 Olivier Asuncion
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

public class Mandate extends TradistaObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3997175280523902567L;

	public static enum RiskLevel {
		VERY_LOW, LOW, AVERAGE, HIGH, VERY_HIGH;

		public String toString() {
			switch (this) {
			case VERY_LOW:
				return "Very Low";
			case LOW:
				return "Low";
			case AVERAGE:
				return "Average";
			case HIGH:
				return "High";
			case VERY_HIGH:
				return "Very High";
			}
			return super.toString();
		}
	};

	private RiskLevel acceptedRiskLevel;

	private LocalDateTime creationDateTime;

	private LocalDate startDate;

	private LocalDate endDate;

	@Id
	private String name;

	private Map<String, Allocation> productTypeAllocations;

	private Map<String, Allocation> currencyAllocations;

	private BigDecimal initialCashAmount;

	private Currency initialCashCurrency;

	private Book book;

	public class Allocation extends TradistaObject {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8335133920081352601L;

		private short minAllocation;

		private short maxAllocation;

		public short getMinAllocation() {
			return minAllocation;
		}

		public void setMinAllocation(short minAllocation) {
			this.minAllocation = minAllocation;
		}

		public short getMaxAllocation() {
			return maxAllocation;
		}

		public void setMaxAllocation(short maxAllocation) {
			this.maxAllocation = maxAllocation;
		}
	}

	public Mandate(String name) {
		this.name = name;
	}

	public RiskLevel getAcceptedRiskLevel() {
		return acceptedRiskLevel;
	}

	public void setAcceptedRiskLevel(RiskLevel acceptedRiskLevel) {
		this.acceptedRiskLevel = acceptedRiskLevel;
	}

	public LocalDateTime getCreationDateTime() {
		return creationDateTime;
	}

	public void setCreationDateTime(LocalDateTime creationDateTime) {
		this.creationDateTime = creationDateTime;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public String getName() {
		return name;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Allocation> getProductTypeAllocations() {
		return (Map<String, Allocation>) TradistaModelUtil.deepCopy(productTypeAllocations);
	}

	public void setProductTypeAllocations(Map<String, Allocation> productTypeAllocations) {
		this.productTypeAllocations = productTypeAllocations;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Allocation> getCurrencyAllocations() {
		return (Map<String, Allocation>) TradistaModelUtil.deepCopy(currencyAllocations);
	}

	public void setCurrencyAllocations(Map<String, Allocation> currencyAllocations) {
		this.currencyAllocations = currencyAllocations;
	}

	public BigDecimal getInitialCashAmount() {
		return initialCashAmount;
	}

	public void setInitialCashAmount(BigDecimal initialCashAmount) {
		this.initialCashAmount = initialCashAmount;
	}

	public Currency getInitialCashCurrency() {
		return TradistaModelUtil.clone(initialCashCurrency);
	}

	public void setInitialCashCurrency(Currency initialCashCurrency) {
		this.initialCashCurrency = initialCashCurrency;
	}

	public Book getBook() {
		return TradistaModelUtil.clone(book);
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Mandate clone() {
		Mandate mandate = (Mandate) super.clone();
		mandate.currencyAllocations = (Map<String, Allocation>) TradistaModelUtil.deepCopy(currencyAllocations);
		mandate.productTypeAllocations = (Map<String, Allocation>) TradistaModelUtil.deepCopy(productTypeAllocations);
		mandate.book = TradistaModelUtil.clone(book);
		mandate.initialCashCurrency = TradistaModelUtil.clone(initialCashCurrency);
		return mandate;
	}

}