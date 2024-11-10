package org.eclipse.tradista.core.cashflow.ui.controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
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

public class CashFlowProperty {

	private final StringProperty date;
	private final StringProperty direction;
	private final StringProperty purpose;
	private final StringProperty amount;
	private final StringProperty currency;
	private final StringProperty discountedAmount;
	private final StringProperty discountFactor;

	public CashFlowProperty(String date, String direction, String purpose, String amount, String currency,
			String discountedAmount, String discountFactor) {
		this.date = new SimpleStringProperty(date);
		this.direction = new SimpleStringProperty(direction);
		this.purpose = new SimpleStringProperty(purpose);
		this.amount = new SimpleStringProperty(amount);
		this.currency = new SimpleStringProperty(currency);
		this.discountedAmount = new SimpleStringProperty(discountedAmount);
		this.discountFactor = new SimpleStringProperty(discountFactor);
	}

	public static List<CashFlowProperty> toCashFlowPropertyList(Collection<CashFlow> data) {
		List<CashFlowProperty> cfPropertyList = new ArrayList<CashFlowProperty>();
		for (CashFlow cf : data) {
			cfPropertyList.add(new CashFlowProperty(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(cf.getDate()),
					cf.getDirection().toString(),
					cf.getPurpose() == null ? StringUtils.EMPTY : cf.getPurpose().toString(),
					TradistaGUIUtil.formatAmount(cf.getAmount()), cf.getCurrency().toString(),
					cf.getDiscountedAmount() == null ? StringUtils.EMPTY
							: TradistaGUIUtil.formatAmount(cf.getDiscountedAmount()),
					cf.getDiscountFactor() == null ? StringUtils.EMPTY
							: TradistaGUIUtil.formatAmount(cf.getDiscountFactor())));
		}

		return cfPropertyList;
	}

	public StringProperty getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date.set(date);
	}

	public StringProperty getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction.set(direction);
	}

	public StringProperty getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose.set(purpose);
	}

	public StringProperty getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount.set(amount);
	}

	public StringProperty getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency.set(currency);
	}

	public StringProperty getDiscountedAmount() {
		return discountedAmount;
	}

	public void setDiscountedAmount(String discountedAmount) {
		this.discountedAmount.set(discountedAmount);
	}

	public StringProperty getDiscountFactor() {
		return discountFactor;
	}

	public void setDiscountFactor(String discountFactor) {
		this.discountFactor.set(discountFactor);
	}

}