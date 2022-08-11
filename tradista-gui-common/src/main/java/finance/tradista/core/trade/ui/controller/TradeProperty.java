package finance.tradista.core.trade.ui.controller;

import java.time.format.DateTimeFormatter;

import finance.tradista.core.product.model.Product;
import finance.tradista.core.trade.model.Trade;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/*
 * Copyright 2022 Olivier Asuncion
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.    */

public class TradeProperty {

	private LongProperty id = new SimpleLongProperty();
	private StringProperty tradeDate = new SimpleStringProperty();
	private LongProperty productId = new SimpleLongProperty();
	private StringProperty productType = new SimpleStringProperty();
	private StringProperty creationDate = new SimpleStringProperty();
	private StringProperty counterparty = new SimpleStringProperty();

	public TradeProperty(Trade<? extends Product> trade) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		this.id.set(trade.getId());
		this.tradeDate.set(trade.getTradeDate().format(dtf));
		this.productId.set(trade.getProductId());
		this.productType.set(trade.getProductType());
		this.creationDate.set(trade.getCreationDate().format(dtf));
		this.counterparty.set(trade.getCounterparty().toString());
	}

	public LongProperty getId() {
		return id;
	}

	public StringProperty getTradeDate() {
		return tradeDate;
	}

	public LongProperty getProductId() {
		return productId;
	}

	public StringProperty getProductType() {
		return productType;
	}

	public StringProperty getCreationDate() {
		return creationDate;
	}

	public StringProperty getCounterparty() {
		return counterparty;
	}

}