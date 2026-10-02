package org.eclipse.tradista.security.repo.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.security.common.model.Security;

/********************************************************************************
 * Copyright (c) 2024 Olivier Asuncion
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
 * Abstract class representing a trade on Repo.
 * 
 *
 */
public abstract class RepoTrade extends Trade<Security> {

	private static final long serialVersionUID = -1883330290840134887L;

	/**
	 * @deprecated use {@link Builder} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public RepoTrade() {
	}

	protected RepoTrade(Builder<?, ?> builder) {
		super(builder);
		this.repoRate = builder.repoRate;
		this.marginRate = builder.marginRate;
		this.index = builder.index;
		this.indexTenor = builder.indexTenor;
		this.indexOffset = builder.indexOffset;
		this.endDate = builder.endDate;
		this.rightOfSubstitution = builder.rightOfSubstitution;
		this.rightOfReuse = builder.rightOfReuse;
		this.crossCurrencyCollateral = builder.crossCurrencyCollateral;
		this.terminableOnDemand = builder.terminableOnDemand;
		this.noticePeriod = builder.noticePeriod;
		this.collateralToAdd = builder.collateralToAdd;
		this.collateralToRemove = builder.collateralToRemove;
		this.partialTerminations = builder.partialTerminations;
	}

	private BigDecimal repoRate;

	private BigDecimal marginRate;

	private Index index;

	private Tenor indexTenor;

	private BigDecimal indexOffset;

	private LocalDate endDate;

	private boolean rightOfSubstitution;

	private boolean rightOfReuse;

	private boolean crossCurrencyCollateral;

	private boolean terminableOnDemand;

	private short noticePeriod;

	private Map<Security, Map<Book, BigDecimal>> collateralToAdd;

	private Map<Security, Map<Book, BigDecimal>> collateralToRemove;

	private Map<LocalDate, BigDecimal> partialTerminations;

	public BigDecimal getRepoRate() {
		return repoRate;
	}

	public void setRepoRate(BigDecimal repoRate) {
		this.repoRate = repoRate;
	}

	/**
	 * By convention, expressed relatively to 100. Ex: 105 for a margin rate of 5%
	 * 
	 * @return
	 */
	public BigDecimal getMarginRate() {
		return marginRate;
	}

	public void setMarginRate(BigDecimal marginRate) {
		this.marginRate = marginRate;
	}

	public Index getIndex() {
		return index;
	}

	public void setIndex(Index index) {
		this.index = index;
	}

	public Tenor getIndexTenor() {
		return indexTenor;
	}

	public void setIndexTenor(Tenor indexTenor) {
		this.indexTenor = indexTenor;
	}

	public BigDecimal getIndexOffset() {
		return indexOffset;
	}

	public void setIndexOffset(BigDecimal indexOffset) {
		this.indexOffset = indexOffset;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public boolean isRightOfSubstitution() {
		return rightOfSubstitution;
	}

	public void setRightOfSubstitution(boolean rightOfSubstitution) {
		this.rightOfSubstitution = rightOfSubstitution;
	}

	public boolean isRightOfReuse() {
		return rightOfReuse;
	}

	public void setRightOfReuse(boolean rightOfReuse) {
		this.rightOfReuse = rightOfReuse;
	}

	public boolean isCrossCurrencyCollateral() {
		return crossCurrencyCollateral;
	}

	public void setCrossCurrencyCollateral(boolean crossCurrencyCollateral) {
		this.crossCurrencyCollateral = crossCurrencyCollateral;
	}

	public boolean isTerminableOnDemand() {
		return terminableOnDemand;
	}

	public void setTerminableOnDemand(boolean terminableOnDemand) {
		this.terminableOnDemand = terminableOnDemand;
	}

	public short getNoticePeriod() {
		return noticePeriod;
	}

	public void setNoticePeriod(short noticePeriod) {
		this.noticePeriod = noticePeriod;
	}

	public boolean isFixedRepoRate() {
		return index == null;
	}

	@SuppressWarnings("unchecked")
	public Map<Security, Map<Book, BigDecimal>> getCollateralToAdd() {
		return (Map<Security, Map<Book, BigDecimal>>) TradistaModelUtil.deepCopy(collateralToAdd);
	}

	public void setCollateralToAdd(Map<Security, Map<Book, BigDecimal>> collateralToAdd) {
		this.collateralToAdd = collateralToAdd;
	}

	@SuppressWarnings("unchecked")
	public Map<Security, Map<Book, BigDecimal>> getCollateralToRemove() {
		return (Map<Security, Map<Book, BigDecimal>>) TradistaModelUtil.deepCopy(collateralToRemove);
	}

	public void setCollateralToRemove(Map<Security, Map<Book, BigDecimal>> collateralToRemove) {
		this.collateralToRemove = collateralToRemove;
	}

	@SuppressWarnings("unchecked")
	public Map<LocalDate, BigDecimal> getPartialTerminations() {
		return (Map<LocalDate, BigDecimal>) TradistaModelUtil.deepCopy(partialTerminations);
	}

	public void setPartialTerminations(Map<LocalDate, BigDecimal> partialTerminations) {
		this.partialTerminations = partialTerminations;
	}

	public void addParTialTermination(LocalDate date, BigDecimal reduction) {
		if (partialTerminations == null) {
			partialTerminations = new HashMap<>();
		}
		partialTerminations.putIfAbsent(date, BigDecimal.ZERO);
		partialTerminations.put(date, partialTerminations.get(date).add(reduction));
	}

	public abstract static class Builder<T extends RepoTrade, B extends Builder<T, B>>
			extends Trade.Builder<Security, T, B> {
		protected BigDecimal repoRate;
		protected BigDecimal marginRate;
		protected Index index;
		protected Tenor indexTenor;
		protected BigDecimal indexOffset;
		protected LocalDate endDate;
		protected boolean rightOfSubstitution;
		protected boolean rightOfReuse;
		protected boolean crossCurrencyCollateral;
		protected boolean terminableOnDemand;
		protected short noticePeriod;
		protected Map<Security, Map<Book, BigDecimal>> collateralToAdd;
		protected Map<Security, Map<Book, BigDecimal>> collateralToRemove;
		protected Map<LocalDate, BigDecimal> partialTerminations;

		public B repoRate(BigDecimal repoRate) {
			this.repoRate = repoRate;
			return self();
		}

		public B marginRate(BigDecimal marginRate) {
			this.marginRate = marginRate;
			return self();
		}

		public B index(Index index) {
			this.index = index;
			return self();
		}

		public B indexTenor(Tenor indexTenor) {
			this.indexTenor = indexTenor;
			return self();
		}

		public B indexOffset(BigDecimal indexOffset) {
			this.indexOffset = indexOffset;
			return self();
		}

		public B endDate(LocalDate endDate) {
			this.endDate = endDate;
			return self();
		}

		public B rightOfSubstitution(boolean rightOfSubstitution) {
			this.rightOfSubstitution = rightOfSubstitution;
			return self();
		}

		public B rightOfReuse(boolean rightOfReuse) {
			this.rightOfReuse = rightOfReuse;
			return self();
		}

		public B crossCurrencyCollateral(boolean crossCurrencyCollateral) {
			this.crossCurrencyCollateral = crossCurrencyCollateral;
			return self();
		}

		public B terminableOnDemand(boolean terminableOnDemand) {
			this.terminableOnDemand = terminableOnDemand;
			return self();
		}

		public B noticePeriod(short noticePeriod) {
			this.noticePeriod = noticePeriod;
			return self();
		}

		public B collateralToAdd(Map<Security, Map<Book, BigDecimal>> collateralToAdd) {
			this.collateralToAdd = collateralToAdd;
			return self();
		}

		public B collateralToRemove(Map<Security, Map<Book, BigDecimal>> collateralToRemove) {
			this.collateralToRemove = collateralToRemove;
			return self();
		}

		public B partialTerminations(Map<LocalDate, BigDecimal> partialTerminations) {
			this.partialTerminations = partialTerminations;
			return self();
		}
	}

}