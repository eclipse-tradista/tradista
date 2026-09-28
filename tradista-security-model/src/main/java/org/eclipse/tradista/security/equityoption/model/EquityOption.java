package org.eclipse.tradista.security.equityoption.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.OptionTrade.SettlementType;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.security.equity.model.Equity;

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
 * Class representing Listed Equity Options. Example of listed equity option:
 * http://www.cboe.com/products/equityoptionspecs.aspx
 * 
 * Note: Identifier is code + type + strike because: In CBOE, a symbol
 * identifies an equity option. In Euronext, a symbol can be either a call or a
 * put, and with different strikes.
 * 
 * @author OA
 *
 */
public class EquityOption extends Product {

	private static final long serialVersionUID = 2072515902166014837L;

	public static final String EQUITY_OPTION = "EquityOption";

	@Id
	private String code;

	private Equity underlying;

	@Id
	private BigDecimal strike;

	@Id
	private LocalDate maturityDate;

	@Id
	private OptionTrade.Type type;

	@Id
	private EquityOptionContractSpecification equityOptionContractSpecification;

	/**
	 * @deprecated use {@link Builder} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public EquityOption(String code, OptionTrade.Type type, BigDecimal strike, LocalDate maturityDate,
			EquityOptionContractSpecification equityOptionContractSpecification) {
		super(equityOptionContractSpecification != null ? equityOptionContractSpecification.getExchange() : null);
		this.code = code;
		this.type = type;
		this.strike = strike;
		this.maturityDate = maturityDate;
		this.equityOptionContractSpecification = equityOptionContractSpecification;
	}

	protected EquityOption(Builder builder) {
		super(builder);
		this.code = builder.code;
		this.type = builder.type;
		this.strike = builder.strike;
		this.maturityDate = builder.maturityDate;
		this.equityOptionContractSpecification = builder.equityOptionContractSpecification;
		this.underlying = builder.underlying;
	}

	@Override
	public Builder toBuilder() {
		Builder builder = new Builder(this.code, this.type, this.strike, this.maturityDate,
				this.equityOptionContractSpecification);
		builder.id(getId()).exchange(getExchange()).creationTime(getCreationTime())
				.lastUpdateTime(getLastUpdateTime()).underlying(this.underlying);
		return builder;
	}

	public static class Builder extends Product.Builder<EquityOption, Builder> {
		protected String code;
		protected OptionTrade.Type type;
		protected BigDecimal strike;
		protected LocalDate maturityDate;
		protected EquityOptionContractSpecification equityOptionContractSpecification;
		protected Equity underlying;

		public Builder(String code, OptionTrade.Type type, BigDecimal strike, LocalDate maturityDate,
				EquityOptionContractSpecification equityOptionContractSpecification) {
			this.code = code;
			this.type = type;
			this.strike = strike;
			this.maturityDate = maturityDate;
			this.equityOptionContractSpecification = equityOptionContractSpecification;
			if (equityOptionContractSpecification != null) {
				this.exchange = equityOptionContractSpecification.getExchange();
			}
		}

		public Builder underlying(Equity underlying) {
			this.underlying = underlying;
			return this;
		}

		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public EquityOption build() {
			return new EquityOption(this);
		}
	}

	public EquityOptionContractSpecification getEquityOptionContractSpecification() {
		return TradistaModelUtil.clone(equityOptionContractSpecification);
	}

	public OptionTrade.Type getType() {
		return type;
	}

	public SettlementType getSettlementType() {
		if (equityOptionContractSpecification != null) {
			return equityOptionContractSpecification.getSettlementType();
		}
		return null;
	}

	public int getSettlementDateOffset() {
		if (equityOptionContractSpecification != null) {
			return equityOptionContractSpecification.getSettlementDateOffset();
		}
		return 0;
	}

	public String getCode() {
		return code;
	}

	public Equity getUnderlying() {
		return TradistaModelUtil.clone(underlying);
	}

	public void setUnderlying(Equity underlying) {
		this.underlying = underlying;
	}

	public BigDecimal getQuantity() {
		if (equityOptionContractSpecification != null) {
			return equityOptionContractSpecification.getQuantity();
		}
		return null;
	}

	public VanillaOptionTrade.Style getStyle() {
		if (equityOptionContractSpecification != null) {
			return equityOptionContractSpecification.getStyle();
		}
		return null;
	}

	public Currency getPremiumCurrency() {
		if (equityOptionContractSpecification != null) {
			return equityOptionContractSpecification.getPremiumCurrency();
		}
		return null;
	}

	public BigDecimal getMultiplier() {
		if (equityOptionContractSpecification != null) {
			return equityOptionContractSpecification.getMultiplier();
		}
		return null;
	}

	@Override
	public String getProductType() {
		return EQUITY_OPTION;
	}

	public BigDecimal getStrike() {
		return strike;
	}

	public LocalDate getMaturityDate() {
		return maturityDate;
	}

	@Override
	public Exchange getExchange() {
		if (equityOptionContractSpecification != null) {
			return equityOptionContractSpecification.getExchange();
		}
		return null;
	}

	public String toString() {
		String label = getCode() + " - " + TradistaModelUtil.formatNumber(getStrike()) + " - " + getMaturityDate()
				+ " - " + getType();
		if (getEquityOptionContractSpecification() != null) {
			label += " - " + TradistaModelUtil.formatObject(getEquityOptionContractSpecification());
		}
		label += " - " + TradistaModelUtil.formatObject(getExchange());
		return label;
	}

	@Override
	public EquityOption clone() {
		EquityOption equityOption = (EquityOption) super.clone();
		equityOption.equityOptionContractSpecification = TradistaModelUtil.clone(equityOptionContractSpecification);
		equityOption.underlying = TradistaModelUtil.clone(underlying);
		return equityOption;
	}

}