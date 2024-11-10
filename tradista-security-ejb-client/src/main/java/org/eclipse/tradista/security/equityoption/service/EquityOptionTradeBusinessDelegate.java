package org.eclipse.tradista.security.equityoption.service;

import java.time.LocalDate;
import java.util.List;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.security.equity.model.EquityTrade;
import org.eclipse.tradista.security.equityoption.model.EquityOption;
import org.eclipse.tradista.security.equityoption.model.EquityOptionTrade;
import org.eclipse.tradista.security.equityoption.validator.EquityOptionTradeValidator;

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

public class EquityOptionTradeBusinessDelegate {

	private EquityOptionTradeService equityTradeOptionService;

	private EquityOptionTradeValidator validator;

	public EquityOptionTradeBusinessDelegate() {
		equityTradeOptionService = TradistaServiceLocator.getInstance().getEquityOptionTradeService();
		validator = new EquityOptionTradeValidator();
	}

	public long saveEquityOptionTrade(EquityOptionTrade trade) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> equityTradeOptionService.saveEquityOptionTrade(trade));
	}

	public boolean isBusinessDay(EquityOptionTrade trade, LocalDate date) throws TradistaBusinessException {
		EquityOption equityOption;
		EquityTrade underlying;
		if (trade == null) {
			throw new TradistaBusinessException("The Equity Option trade cannot be null");
		}
		underlying = trade.getUnderlying();
		equityOption = trade.getEquityOption();

		if (equityOption == null) {
			if (underlying == null) {
				return true;
			} else {
				Exchange underlyingExchange = underlying.getExchange();
				if (underlyingExchange == null) {
					throw new TradistaBusinessException("The Equity Option trade underlying exchange cannot be null");
				} else {
					Calendar underlyingCalendar = underlyingExchange.getCalendar();
					if (underlyingCalendar != null) {
						return underlyingCalendar.isBusinessDay(date);
					} else {
						return true;
					}
				}
			}
		} else {
			Exchange eqoExchange = equityOption.getExchange();
			if (eqoExchange == null) {
				throw new TradistaBusinessException("The Equity Option exchange cannot be null");
			} else {
				Calendar eqoCalendar = eqoExchange.getCalendar();
				if (eqoCalendar != null) {
					return eqoCalendar.isBusinessDay(date);
				} else {
					return true;
				}
			}
		}

	}

	public List<EquityOptionTrade> getEquityOptionTradesBeforeTradeDateByEquityOptionAndBookIds(LocalDate tradeDate,
			long equityOptionId, long bookId) throws TradistaBusinessException {
		if (tradeDate == null) {
			throw new TradistaBusinessException("The trade date is mandatory.");
		}

		return SecurityUtil.run(() -> equityTradeOptionService
				.getEquityOptionTradesBeforeTradeDateByEquityOptionAndBookIds(tradeDate, equityOptionId, bookId));
	}

	public EquityOptionTrade getEquityOptionTradeById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The trade id must be positive.");
		}
		return SecurityUtil.run(() -> equityTradeOptionService.getEquityOptionTradeById(id));
	}

}