package org.eclipse.tradista.security.equity.model;

import java.time.LocalDate;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.security.common.model.Security;

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
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

public class Equity extends Security {

	private static final long serialVersionUID = 4659470425942478262L;

	public static final String EQUITY = "Equity";

	private long tradingSize;

	private long totalIssued;

	private boolean payDividend;

	private Currency dividendCurrency;

	private Tenor dividendFrequency;

	private LocalDate activeFrom;

	private LocalDate activeTo;

	public Equity(Exchange exchange, String isin) {
		super(exchange, isin);
	}

	public long getTradingSize() {
		return tradingSize;
	}

	public void setTradingSize(long tradingSize) {
		this.tradingSize = tradingSize;
	}

	public long getTotalIssued() {
		return totalIssued;
	}

	public void setTotalIssued(long totalIssued) {
		this.totalIssued = totalIssued;
	}

	public boolean isPayDividend() {
		return payDividend;
	}

	public void setPayDividend(boolean payDividend) {
		this.payDividend = payDividend;
	}

	public Currency getDividendCurrency() {
		return TradistaModelUtil.clone(dividendCurrency);
	}

	public void setDividendCurrency(Currency dividendCurrency) {
		this.dividendCurrency = dividendCurrency;
	}

	public Tenor getDividendFrequency() {
		return dividendFrequency;
	}

	public void setDividendFrequency(Tenor dividendFrequency) {
		this.dividendFrequency = dividendFrequency;
	}

	public LocalDate getActiveFrom() {
		return activeFrom;
	}

	public void setActiveFrom(LocalDate activeFrom) {
		this.activeFrom = activeFrom;
	}

	public LocalDate getActiveTo() {
		return activeTo;
	}

	public void setActiveTo(LocalDate activeTo) {
		this.activeTo = activeTo;
	}

	@Override
	public String getProductType() {
		return EQUITY;
	}

	@Override
	public Equity clone() {
		Equity equity = (Equity) super.clone();
		equity.dividendCurrency = TradistaModelUtil.clone(dividendCurrency);
		return equity;
	}

}