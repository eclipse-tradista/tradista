package org.eclipse.tradista.security.equityoption.ui.controller;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.security.equityoption.model.EquityOption;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/********************************************************************************
 * Copyright (c) 2022 Olivier Asuncion
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

public class EquityOptionProperty {

	private LongProperty id = new SimpleLongProperty();
	private StringProperty code = new SimpleStringProperty();
	private StringProperty equity = new SimpleStringProperty();
	private StringProperty quantity = new SimpleStringProperty();
	private StringProperty style = new SimpleStringProperty();
	private StringProperty exchange = new SimpleStringProperty();

	public EquityOptionProperty(EquityOption equityOption) {
		this.id.set(equityOption.getId());
		this.code.set(equityOption.getCode());
		this.equity
				.set(equityOption.getUnderlying() == null ? StringUtils.EMPTY : equityOption.getUnderlying().getIsin());
		this.quantity.set(TradistaGUIUtil.formatAmount(equityOption.getQuantity()));
		this.style.set(equityOption.getStyle().name());
		this.exchange.set(equityOption.getExchange().getCode());
	}

	public LongProperty getId() {
		return id;
	}

	public StringProperty getCode() {
		return code;
	}

	public StringProperty getEquity() {
		return equity;
	}

	public StringProperty getQuantity() {
		return quantity;
	}

	public StringProperty getStyle() {
		return style;
	}

	public StringProperty getExchange() {
		return exchange;
	}

}