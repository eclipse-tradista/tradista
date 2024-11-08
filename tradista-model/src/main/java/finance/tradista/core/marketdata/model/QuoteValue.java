package org.eclipse.tradista.core.marketdata.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;

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

public class QuoteValue extends TradistaObject implements MarketData, Comparable<QuoteValue> {

	private static final long serialVersionUID = -8712317853305269754L;

	@Id
	private QuoteSet quoteSet;

	@Id
	private LocalDate date;

	private BigDecimal bid;

	private BigDecimal ask;

	private BigDecimal open;

	private BigDecimal close;

	private BigDecimal high;

	private BigDecimal low;

	private BigDecimal last;

	public static final String BID = "Bid";

	public static final String ASK = "Ask";

	public static final String OPEN = "Open";

	public static final String CLOSE = "Close";

	public static final String HIGH = "High";

	public static final String LOW = "Low";

	public static final String LAST = "Last";

	@Id
	private Quote quote;

	public Quote getQuote() {
		return TradistaModelUtil.clone(quote);
	}

	private String sourceName;

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	private LocalDate enteredDate;

	public BigDecimal getBid() {
		return bid;
	}

	public void setBid(BigDecimal bid) {
		this.bid = bid;
	}

	public BigDecimal getAsk() {
		return ask;
	}

	public void setAsk(BigDecimal ask) {
		this.ask = ask;
	}

	public BigDecimal getOpen() {
		return open;
	}

	public void setOpen(BigDecimal open) {
		this.open = open;
	}

	public BigDecimal getClose() {
		return close;
	}

	public void setClose(BigDecimal close) {
		this.close = close;
	}

	public BigDecimal getHigh() {
		return high;
	}

	public void setHigh(BigDecimal high) {
		this.high = high;
	}

	public BigDecimal getLow() {
		return low;
	}

	public void setLow(BigDecimal low) {
		this.low = low;
	}

	public BigDecimal getLast() {
		return last;
	}

	public void setLast(BigDecimal last) {
		this.last = last;
	}

	public LocalDate getEnteredDate() {
		return enteredDate;
	}

	public void setEnteredDate(LocalDate enteredDate) {
		this.enteredDate = enteredDate;
	}

	public LocalDate getDate() {
		return date;
	}

	public QuoteValue(LocalDate date, Quote quote) {
		this.quote = quote;
		this.date = date;
	}

	public QuoteValue(LocalDate date, BigDecimal bid, BigDecimal ask, BigDecimal open, BigDecimal close,
			BigDecimal high, BigDecimal low, BigDecimal last, String sourceName) {
		this.date = date;
		this.bid = bid;
		this.ask = ask;
		this.open = open;
		this.close = close;
		this.high = high;
		this.low = low;
		this.last = last;
		this.sourceName = sourceName;
	}

	public QuoteValue(LocalDate date, BigDecimal bid, BigDecimal ask, BigDecimal open, BigDecimal close,
			BigDecimal high, BigDecimal low, BigDecimal last, String sourceName, Quote quote, LocalDate enteredDate,
			QuoteSet quoteSet) {
		this.date = date;
		this.bid = bid;
		this.ask = ask;
		this.open = open;
		this.close = close;
		this.high = high;
		this.low = low;
		this.last = last;
		this.quote = quote;
		this.sourceName = sourceName;
		this.enteredDate = enteredDate;
		this.quoteSet = quoteSet;
	}

	public QuoteValue(LocalDate date, BigDecimal bid, BigDecimal ask, BigDecimal open, BigDecimal close,
			BigDecimal high, BigDecimal low, BigDecimal last, String sourceName, LocalDate enteredDate) {
		this.date = date;
		this.bid = bid;
		this.ask = ask;
		this.open = open;
		this.close = close;
		this.high = high;
		this.low = low;
		this.last = last;
		this.sourceName = sourceName;
		this.enteredDate = enteredDate;
	}

	public QuoteValue(QuoteValue quoteValue, QuoteSet quoteSet) {
		this.date = quoteValue.date;
		this.bid = quoteValue.bid;
		this.ask = quoteValue.ask;
		this.open = quoteValue.open;
		this.close = quoteValue.close;
		this.high = quoteValue.high;
		this.low = quoteValue.low;
		this.last = quoteValue.last;
		this.quote = quoteValue.quote;
		this.sourceName = quoteValue.sourceName;
		this.enteredDate = quoteValue.enteredDate;
		this.quote = TradistaModelUtil.clone(quoteValue.quote);
		this.quoteSet = quoteSet;
	}

	public QuoteSet getQuoteSet() {
		return TradistaModelUtil.clone(quoteSet);
	}

	/**
	 * Returns the value for this value type. default to Last
	 * 
	 * @param valueType the value type: BID, ASK, OPEN, CLOSE, LAST, HIGH or MID
	 * @return the value for this value type. default to Last.
	 */
	public BigDecimal getValue(String valueType) {
		if (valueType != null) {
			switch (valueType) {
			case QuoteValue.BID:
				return bid;
			case QuoteValue.ASK:
				return ask;
			case QuoteValue.OPEN:
				return open;
			case QuoteValue.CLOSE:
				return close;
			case QuoteValue.LAST:
				return last;
			case QuoteValue.HIGH:
				return high;
			case QuoteValue.LOW:
				return low;
			case "Mid":
				return (ask.add(bid)).divide(BigDecimal.valueOf(2));
			default:
				return last;
			}
		} else {
			return last;
		}
	}

	@Override
	public int compareTo(QuoteValue qv) {
		return date.compareTo(qv.getDate());
	}

	@Override
	public QuoteValue clone() {
		QuoteValue quoteValue = (QuoteValue) super.clone();
		quoteValue.quote = TradistaModelUtil.clone(quote);
		quoteValue.quoteSet = TradistaModelUtil.clone(quoteSet);
		return quoteValue;
	}

}