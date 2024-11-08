package org.eclipse.tradista.fx.fxswap.service;

import java.time.LocalDate;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.fx.fx.service.AbstractFXTradeBusinessDelegate;
import org.eclipse.tradista.fx.fx.service.FXTradeBusinessDelegate;
import org.eclipse.tradista.fx.fxswap.model.FXSwapTrade;
import org.eclipse.tradista.fx.fxswap.service.FXSwapTradeService;
import org.eclipse.tradista.fx.fxswap.validator.FXSwapTradeValidator;

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

public class FXSwapTradeBusinessDelegate extends AbstractFXTradeBusinessDelegate<FXSwapTrade> {

	private FXSwapTradeService fxSwapTradeService;

	private FXSwapTradeValidator validator;

	public FXSwapTradeBusinessDelegate() {
		validator = new FXSwapTradeValidator();
		fxSwapTradeService = TradistaServiceLocator.getInstance().getFXSwapTradeService();
	}

	public long saveFXSwapTrade(FXSwapTrade trade) throws TradistaBusinessException {

		validator.validateTrade(trade);

		return SecurityUtil.runEx(() -> fxSwapTradeService.saveFXSwapTrade(trade));

	}

	@Override
	public boolean isBusinessDay(FXSwapTrade fxSwapTrade, LocalDate date) throws TradistaBusinessException {
		boolean isBusinessDay = super.isBusinessDay(fxSwapTrade, date);
		Currency currencyOne = fxSwapTrade.getCurrencyOne();
		Calendar currencyOneCalendar;
		if (currencyOne == null) {
			throw new TradistaBusinessException("The FX Swap trade currency one cannot be null");
		}
		currencyOneCalendar = currencyOne.getCalendar();
		if (currencyOneCalendar == null) {
			// TODO Add a warning log
		}
		if (currencyOneCalendar != null) {
			return isBusinessDay && currencyOneCalendar.isBusinessDay(date);
		} else {
			return isBusinessDay;
		}
	}

	public Exchange getFXExchange() {
		return new FXTradeBusinessDelegate().getFXExchange();
	}

	public FXSwapTrade getFXSwapTradeById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The trade id must be positive.");
		}
		return SecurityUtil.run(() -> fxSwapTradeService.getFXSwapTradeById(id));
	}

}