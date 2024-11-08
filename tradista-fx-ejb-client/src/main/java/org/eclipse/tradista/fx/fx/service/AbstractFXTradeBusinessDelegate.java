package org.eclipse.tradista.fx.fx.service;

import java.time.LocalDate;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.exchange.service.ExchangeBusinessDelegate;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.fx.common.model.AbstractFXTrade;

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

public class AbstractFXTradeBusinessDelegate<X extends AbstractFXTrade<Product>> {

	protected static Exchange fxExchange;

	public boolean isBusinessDay(X fxTrade, LocalDate date) throws TradistaBusinessException {
		Currency tradeCurrency;
		Exchange tradeExchange;
		Calendar exchangeCalendar;
		Calendar currencyCalendar;
		if (fxTrade == null) {
			throw new TradistaBusinessException("The FX trade cannot be null");
		}
		if (date == null) {
			throw new TradistaBusinessException("The date cannot be null");
		}
		tradeCurrency = fxTrade.getCurrency();
		if (tradeCurrency == null) {
			throw new TradistaBusinessException("The FX trade currency cannot be null");
		}
		currencyCalendar = tradeCurrency.getCalendar();
		if (currencyCalendar == null) {
			// TODO Add a warning log
		}
		tradeExchange = getFXExchange();
		if (tradeExchange == null) {
			throw new TradistaBusinessException("The FX trade exchange cannot be null");
		}
		exchangeCalendar = tradeExchange.getCalendar();
		if (tradeExchange.getCalendar() == null) {
			throw new TradistaBusinessException("The FX exchange calendar cannot be null");
		}
		if (currencyCalendar != null) {
			return (currencyCalendar.isBusinessDay(date) && exchangeCalendar.isBusinessDay(date));
		} else {
			return exchangeCalendar.isBusinessDay(date);
		}
	}

	public Exchange getFXExchange() {
		if (fxExchange == null) {
			fxExchange = new ExchangeBusinessDelegate().getExchangeByCode("FX");
		}
		return fxExchange;
	}

}