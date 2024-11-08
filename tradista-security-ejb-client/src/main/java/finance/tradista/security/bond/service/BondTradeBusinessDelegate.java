package org.eclipse.tradista.security.bond.service;

import java.time.LocalDate;
import java.util.List;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.security.bond.model.Bond;
import org.eclipse.tradista.security.bond.model.BondTrade;
import org.eclipse.tradista.security.bond.validator.BondTradeValidator;

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

public class BondTradeBusinessDelegate {

	private BondTradeService bondTradeService;

	private BondTradeValidator validator;

	public BondTradeBusinessDelegate() {
		bondTradeService = TradistaServiceLocator.getInstance().getBondTradeService();
		validator = new BondTradeValidator();
	}

	public long saveBondTrade(BondTrade trade) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.run(() -> bondTradeService.saveBondTrade(trade));
	}

	public boolean isBusinessDay(BondTrade bondTrade, LocalDate date) throws TradistaBusinessException {
		Bond bond;
		Exchange bondExchange;
		Calendar exchangeCalendar;
		if (bondTrade == null) {
			throw new TradistaBusinessException("The Bond trade cannot be null");
		}
		bond = (Bond) bondTrade.getProduct();
		if (bond == null) {
			throw new TradistaBusinessException("The Bond product cannot be null");
		}
		bondExchange = bond.getExchange();
		if (bondExchange == null) {
			throw new TradistaBusinessException("The Bond exchange cannot be null");
		}

		exchangeCalendar = bondExchange.getCalendar();
		if (exchangeCalendar == null) {
			// TODO add a warning log.
		}

		return exchangeCalendar.isBusinessDay(date);
	}

	public List<BondTrade> getBondTradesBeforeTradeDateByBondAndBookIds(LocalDate date, long bondId, long bookId)
			throws TradistaBusinessException {
		if (date == null) {
			throw new TradistaBusinessException("The date is mandatory.");
		}

		return SecurityUtil
				.run(() -> bondTradeService.getBondTradesBeforeTradeDateByBondAndBookIds(date, bondId, bookId));
	}

	public BondTrade getBondTradeById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The trade id must be positive.");
		}
		return SecurityUtil.run(() -> bondTradeService.getBondTradeById(id));
	}

}