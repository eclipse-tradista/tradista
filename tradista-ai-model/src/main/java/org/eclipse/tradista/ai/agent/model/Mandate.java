package org.eclipse.tradista.ai.agent.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TimestampedObject;
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

public class Mandate extends TimestampedObject {

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

	/**
	 * @deprecated use {@link Builder} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public Mandate(String name) {
		this.name = name;
	}

	protected Mandate(Builder builder) {
		super(builder);
		this.name = builder.name;
		this.acceptedRiskLevel = builder.acceptedRiskLevel;
		this.startDate = builder.startDate;
		this.endDate = builder.endDate;
		this.productTypeAllocations = builder.productTypeAllocations;
		this.currencyAllocations = builder.currencyAllocations;
		this.initialCashAmount = builder.initialCashAmount;
		this.initialCashCurrency = builder.initialCashCurrency;
		this.book = builder.book;
	}

	/**
	 * @deprecated use {@link #getCreationTime()} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public LocalDateTime getCreationDateTime() {
		if (getCreationTime() == null) {
			return null;
		}
		return LocalDateTime.ofInstant(getCreationTime(), ZoneId.systemDefault());
	}

	/**
	 * @deprecated creation time is automatically managed and immutable. Use
	 *             {@link Builder#creationTime(java.time.Instant)} if needed during
	 *             construction.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public void setCreationDateTime(LocalDateTime creationDateTime) {
		// No-op: creationTime is immutable and managed at construction/builder level.
	}

	public RiskLevel getAcceptedRiskLevel() {
		return acceptedRiskLevel;
	}

	public void setAcceptedRiskLevel(RiskLevel acceptedRiskLevel) {
		this.acceptedRiskLevel = acceptedRiskLevel;
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

	@Override
	public Builder toBuilder() {
		return new Builder(this.name).id(this.getId()).acceptedRiskLevel(this.acceptedRiskLevel)
				.startDate(this.startDate).endDate(this.endDate).productTypeAllocations(this.productTypeAllocations)
				.currencyAllocations(this.currencyAllocations).initialCashAmount(this.initialCashAmount)
				.initialCashCurrency(this.initialCashCurrency).book(this.book).creationTime(this.getCreationTime())
				.lastUpdateTime(this.getLastUpdateTime());
	}

	public static class Builder extends TimestampedObject.Builder<Mandate, Builder> {
		protected String name;
		protected RiskLevel acceptedRiskLevel;
		protected LocalDate startDate;
		protected LocalDate endDate;
		protected Map<String, Allocation> productTypeAllocations;
		protected Map<String, Allocation> currencyAllocations;
		protected BigDecimal initialCashAmount;
		protected Currency initialCashCurrency;
		protected Book book;

		public Builder(String name) {
			this.name = name;
		}

		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public Mandate build() {
			return new Mandate(this);
		}

		public Builder acceptedRiskLevel(RiskLevel acceptedRiskLevel) {
			this.acceptedRiskLevel = acceptedRiskLevel;
			return this;
		}

		public Builder startDate(LocalDate startDate) {
			this.startDate = startDate;
			return this;
		}

		public Builder endDate(LocalDate endDate) {
			this.endDate = endDate;
			return this;
		}

		public Builder productTypeAllocations(Map<String, Allocation> productTypeAllocations) {
			this.productTypeAllocations = productTypeAllocations;
			return this;
		}

		public Builder currencyAllocations(Map<String, Allocation> currencyAllocations) {
			this.currencyAllocations = currencyAllocations;
			return this;
		}

		public Builder initialCashAmount(BigDecimal initialCashAmount) {
			this.initialCashAmount = initialCashAmount;
			return this;
		}

		public Builder initialCashCurrency(Currency initialCashCurrency) {
			this.initialCashCurrency = initialCashCurrency;
			return this;
		}

		public Builder book(Book book) {
			this.book = book;
			return this;
		}
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