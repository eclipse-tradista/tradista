package org.eclipse.tradista.security.equity.ui.controller;

import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.security.equity.model.Equity;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

public class EquityProperty {

	private LongProperty id = new SimpleLongProperty();
	private LongProperty tradingSize = new SimpleLongProperty();
	private LongProperty totalIssued = new SimpleLongProperty();
	private BooleanProperty payDividend = new SimpleBooleanProperty();
	private StringProperty dividendCurrency = new SimpleStringProperty();
	private StringProperty activeFrom = new SimpleStringProperty();
	private StringProperty activeTo = new SimpleStringProperty();
	private StringProperty isin = new SimpleStringProperty();
	private StringProperty issuer = new SimpleStringProperty();

	public EquityProperty(Equity equity) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		this.id.set(equity.getId());
		this.tradingSize.set(equity.getTradingSize());
		this.totalIssued.set(equity.getTotalIssued());
		this.payDividend.set(equity.isPayDividend());
		this.dividendCurrency.set(
				equity.getDividendCurrency() == null ? StringUtils.EMPTY : equity.getDividendCurrency().toString());
		this.activeFrom.set(equity.getActiveFrom().format(dtf));
		this.activeTo.set(equity.getActiveTo().format(dtf));
		this.isin.set(equity.getIsin());
		this.issuer.set(equity.getIssuer().toString());
	}

	public LongProperty getId() {
		return id;
	}

	public LongProperty getTradingSize() {
		return tradingSize;
	}

	public LongProperty getTotalIssued() {
		return totalIssued;
	}

	public BooleanProperty getPayDividend() {
		return payDividend;
	}

	public StringProperty getDividendCurrency() {
		return dividendCurrency;
	}

	public StringProperty getActiveFrom() {
		return activeFrom;
	}

	public StringProperty getActiveTo() {
		return activeTo;
	}

	public StringProperty getIsin() {
		return isin;
	}

	public StringProperty getIssuer() {
		return issuer;
	}

}