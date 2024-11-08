package org.eclipse.tradista.ir.irswap.service;

import java.time.LocalDate;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.ir.irswap.model.SingleCurrencyIRSwapTrade;
import org.eclipse.tradista.ir.irswap.service.IRSwapTradeService;
import org.eclipse.tradista.ir.irswap.validator.IRSwapTradeValidator;

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

public class IRSwapTradeBusinessDelegate {

	private IRSwapTradeService irSwapTradeService;

	private IRSwapTradeValidator validator;

	public IRSwapTradeBusinessDelegate() {
		irSwapTradeService = TradistaServiceLocator.getInstance().getIRSwapTradeService();
		validator = new IRSwapTradeValidator();
	}

	public long saveIRSwapTrade(SingleCurrencyIRSwapTrade trade) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> irSwapTradeService.saveIRSwapTrade(trade));
	}

	public boolean isBusinessDay(SingleCurrencyIRSwapTrade irSwapTrade, LocalDate date)
			throws TradistaBusinessException {
		Currency tradeCurrency;
		Calendar currencyCalendar;
		if (irSwapTrade == null) {
			throw new TradistaBusinessException("The IR Swap trade cannot be null");
		}
		tradeCurrency = irSwapTrade.getCurrency();
		if (tradeCurrency == null) {
			throw new TradistaBusinessException("The IR Swap trade currency cannot be null");
		}
		currencyCalendar = tradeCurrency.getCalendar();
		if (currencyCalendar == null) {
			// TODO Add warning log
		}
		if (currencyCalendar != null) {
			return currencyCalendar.isBusinessDay(date);
		} else {
			return true;
		}
	}

	public SingleCurrencyIRSwapTrade getIRSwapTradeById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The trade id must be positive.");
		}
		return SecurityUtil.run(() -> irSwapTradeService.getIRSwapTradeById(id));
	}

}