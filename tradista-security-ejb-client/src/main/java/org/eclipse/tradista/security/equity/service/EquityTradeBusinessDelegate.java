package org.eclipse.tradista.security.equity.service;

import java.time.LocalDate;
import java.util.List;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.model.EquityTrade;
import org.eclipse.tradista.security.equity.validator.EquityTradeValidator;

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

public class EquityTradeBusinessDelegate {

	private EquityTradeService equityTradeService;

	private EquityTradeValidator validator;

	public EquityTradeBusinessDelegate() {
		equityTradeService = TradistaServiceLocator.getInstance().getEquityTradeService();
		validator = new EquityTradeValidator();
	}

	public long saveEquityTrade(EquityTrade trade) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> equityTradeService.saveEquityTrade(trade));
	}

	public boolean isBusinessDay(EquityTrade equityTrade, LocalDate date) throws TradistaBusinessException {
		Equity equity;
		Exchange equityExchange;
		Calendar exchangeCalendar;
		if (equityTrade == null) {
			throw new TradistaBusinessException("The Equity trade cannot be null");
		}
		equity = (Equity) equityTrade.getProduct();
		if (equity == null) {
			throw new TradistaBusinessException("The Equity product cannot be null");
		}
		equityExchange = equity.getExchange();
		if (equityExchange == null) {
			throw new TradistaBusinessException("The Equity exchange cannot be null");
		}

		exchangeCalendar = equityExchange.getCalendar();
		if (exchangeCalendar == null) {
			// TODO add a warning log.
			return true; // no calendar, by default all days are business days.
		}

		return exchangeCalendar.isBusinessDay(date);
	}

	public List<EquityTrade> getEquityTradesBeforeTradeDateByEquityAndBookIds(LocalDate date, long equityId,
			long bookId) throws TradistaBusinessException {
		if (date == null) {
			throw new TradistaBusinessException("The date is mandatory.");
		}
		return SecurityUtil
				.run(() -> equityTradeService.getEquityTradesBeforeTradeDateByEquityAndBookIds(date, equityId, bookId));
	}

	public EquityTrade getEquityTradeById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The trade id must be positive.");
		}
		return SecurityUtil.run(() -> equityTradeService.getEquityTradeById(id));
	}

}