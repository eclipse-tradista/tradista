package org.eclipse.tradista.core.marketdata.ui.controller;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.core.marketdata.model.Quote;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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

public class QuoteProperty {

	private final StringProperty date;
	private final StringProperty name;
	private final StringProperty type;
	private final StringProperty bid;
	private final StringProperty ask;
	private final StringProperty open;
	private final StringProperty close;
	private final StringProperty high;
	private final StringProperty low;
	private final StringProperty last;
	private final StringProperty enteredDate;
	private final StringProperty sourceName;

	public QuoteProperty(String date, String name, String type, String bid, String ask, String open, String close,
			String high, String low, String last, String enteredDate, String sourceName) {
		this.date = new SimpleStringProperty(date);
		this.name = new SimpleStringProperty(name);
		this.type = new SimpleStringProperty(type);
		this.bid = new SimpleStringProperty(bid);
		this.ask = new SimpleStringProperty(ask);
		this.open = new SimpleStringProperty(open);
		this.close = new SimpleStringProperty(close);
		this.high = new SimpleStringProperty(high);
		this.low = new SimpleStringProperty(low);
		this.last = new SimpleStringProperty(last);
		this.enteredDate = new SimpleStringProperty(enteredDate);
		this.sourceName = new SimpleStringProperty(sourceName);
	}

	public static ObservableList<QuoteProperty> buildTableContent(List<QuoteValue> data, Month month, Year year,
			String quoteName, QuoteType quoteType) {

		if (data == null) {
			data = new ArrayList<>();
		}

		// Get the number of days in that month
		int daysInMonth = month.length(year.isLeap());

		for (int i = 1; i <= daysInMonth; i++) {
			LocalDate cal = LocalDate.of(year.getValue(), month, i);
			if (quoteType == null) {
				List<QuoteType> quoteTypes = new QuoteBusinessDelegate().getQuoteTypesByQuoteName(quoteName);
				for (QuoteType type : quoteTypes) {
					QuoteValue quoteValue = new QuoteValue(cal,
							new QuoteBusinessDelegate().getQuoteByNameAndType(quoteName, type));
					if (!data.contains(quoteValue)) {
						data.add(quoteValue);
					}
				}
			} else {
				Quote quote = new QuoteBusinessDelegate().getQuoteByNameAndType(quoteName, quoteType);
				if (quote != null) {
					QuoteValue quoteValue = new QuoteValue(cal, quote);
					if (!data.contains(quoteValue)) {
						data.add(quoteValue);
					}
				}
			}

		}

		Collections.sort(data);

		return FXCollections.observableArrayList(toQuotePropertyList(data));

	}

	public static List<QuoteProperty> toQuotePropertyList(Collection<QuoteValue> data) {
		List<QuoteProperty> quotePropertyList = new ArrayList<>();
		for (QuoteValue quoteValue : data) {
			quotePropertyList
					.add(new QuoteProperty(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(quoteValue.getDate()),
							quoteValue.getQuote().getName(), quoteValue.getQuote().getType().toString(),
							quoteValue.getBid() == null ? "" : TradistaGUIUtil.formatAmount(quoteValue.getBid()),
							quoteValue.getAsk() == null ? "" : TradistaGUIUtil.formatAmount(quoteValue.getAsk()),
							quoteValue.getOpen() == null ? "" : TradistaGUIUtil.formatAmount(quoteValue.getOpen()),
							quoteValue.getClose() == null ? "" : TradistaGUIUtil.formatAmount(quoteValue.getClose()),
							quoteValue.getHigh() == null ? "" : TradistaGUIUtil.formatAmount(quoteValue.getHigh()),
							quoteValue.getLow() == null ? "" : TradistaGUIUtil.formatAmount(quoteValue.getLow()),
							quoteValue.getLast() == null ? "" : TradistaGUIUtil.formatAmount(quoteValue.getLast()),
							quoteValue.getEnteredDate() == null ? "" : quoteValue.getEnteredDate().toString(),
							quoteValue.getSourceName() == null ? "" : quoteValue.getSourceName()));
		}

		return quotePropertyList;
	}

	public StringProperty getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date.set(date);
	}

	public StringProperty getName() {
		return name;
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public StringProperty getType() {
		return type;
	}

	public void setType(String type) {
		this.type.set(type);
	}

	public StringProperty getBid() {
		return bid;
	}

	public void setBid(String bid) {
		this.bid.set(bid);
	}

	public StringProperty getAsk() {
		return ask;
	}

	public void setAsk(String ask) {
		this.ask.set(ask);
	}

	public StringProperty getOpen() {
		return open;
	}

	public void setOpen(String open) {
		this.open.set(open);
	}

	public StringProperty getClose() {
		return close;
	}

	public void setClose(String close) {
		this.close.set(close);
	}

	public StringProperty getHigh() {
		return high;
	}

	public void setHigh(String high) {
		this.high.set(high);
	}

	public StringProperty getLow() {
		return low;
	}

	public void setLow(String low) {
		this.low.set(low);
	}

	public StringProperty getLast() {
		return last;
	}

	public void setLast(String last) {
		this.last.set(last);
	}

	public StringProperty getEnteredDate() {
		return enteredDate;
	}

	public void setEnteredDate(String enteredDate) {
		this.enteredDate.set(enteredDate);
	}

	public StringProperty getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName.set(sourceName);
	}

}