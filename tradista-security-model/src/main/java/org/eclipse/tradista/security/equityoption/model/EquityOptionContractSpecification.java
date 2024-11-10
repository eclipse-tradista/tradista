package org.eclipse.tradista.security.equityoption.model;

import java.math.BigDecimal;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.daterule.model.DateRule;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.trade.model.OptionTrade.SettlementType;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;

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

public class EquityOptionContractSpecification extends TradistaObject {

	private static final long serialVersionUID = -8579292038343826059L;

	private DateRule maturityDatesDateRule;

	@Id
	private String name;

	private SettlementType settlementType;

	private short settlementDateOffset;

	private VanillaOptionTrade.Style style;

	private BigDecimal quantity;

	private Exchange exchange;

	private BigDecimal multiplier;

	private Currency premiumCurrency;

	public EquityOptionContractSpecification(String name) {
		super();
		this.name = name;
	}

	public DateRule getMaturityDatesDateRule() {
		return TradistaModelUtil.clone(maturityDatesDateRule);
	}

	public void setMaturityDatesDateRule(DateRule maturityDatesDateRule) {
		this.maturityDatesDateRule = maturityDatesDateRule;
	}

	public String getName() {
		return name;
	}

	public SettlementType getSettlementType() {
		return settlementType;
	}

	public void setSettlementType(SettlementType settlementType) {
		this.settlementType = settlementType;
	}

	public short getSettlementDateOffset() {
		return settlementDateOffset;
	}

	public void setSettlementDateOffset(short settlementDateOffset) {
		this.settlementDateOffset = settlementDateOffset;
	}

	public VanillaOptionTrade.Style getStyle() {
		return style;
	}

	public void setStyle(VanillaOptionTrade.Style style) {
		this.style = style;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public Exchange getExchange() {
		return TradistaModelUtil.clone(exchange);
	}

	public void setExchange(Exchange exchange) {
		this.exchange = exchange;
	}

	public BigDecimal getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(BigDecimal multiplier) {
		this.multiplier = multiplier;
	}

	public Currency getPremiumCurrency() {
		return TradistaModelUtil.clone(premiumCurrency);
	}

	public void setPremiumCurrency(Currency premiumCurrency) {
		this.premiumCurrency = premiumCurrency;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public EquityOptionContractSpecification clone() {
		EquityOptionContractSpecification equityOptionContractSpecification = (EquityOptionContractSpecification) super.clone();
		equityOptionContractSpecification.maturityDatesDateRule = TradistaModelUtil.clone(maturityDatesDateRule);
		equityOptionContractSpecification.exchange = TradistaModelUtil.clone(exchange);
		equityOptionContractSpecification.premiumCurrency = TradistaModelUtil.clone(premiumCurrency);

		return equityOptionContractSpecification;
	}

}