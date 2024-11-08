package org.eclipse.tradista.fx.fx.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.fx.fx.model.FXTrade;
import org.eclipse.tradista.fx.fx.model.FXTrade.Type;
import org.eclipse.tradista.fx.fx.service.FXTradeService;
import org.eclipse.tradista.fx.fx.validator.FXTradeValidator;

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

public class FXTradeBusinessDelegate extends AbstractFXTradeBusinessDelegate<FXTrade> {

	private FXTradeService fxTradeService;

	private FXTradeValidator validator;

	private ConfigurationBusinessDelegate configurationBusinessDelegate;

	public FXTradeBusinessDelegate() {
		fxTradeService = TradistaServiceLocator.getInstance().getFXTradeService();
		configurationBusinessDelegate = new ConfigurationBusinessDelegate();
		validator = new FXTradeValidator();
	}

	public long saveFXTrade(FXTrade trade) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> fxTradeService.saveFXTrade(trade));
	}

	@Override
	public boolean isBusinessDay(FXTrade fxTrade, LocalDate date) throws TradistaBusinessException {
		Currency currencyOne = fxTrade.getCurrencyOne();
		Calendar currencyOneCalendar;
		if (currencyOne == null) {
			throw new TradistaBusinessException("The FX trade currency one cannot be null");
		}
		currencyOneCalendar = currencyOne.getCalendar();
		if (currencyOneCalendar == null) {
			// TODO Add a warning log
		}
		boolean isBusinessDay = super.isBusinessDay(fxTrade, date);
		if (currencyOneCalendar != null) {
			return isBusinessDay && currencyOneCalendar.isBusinessDay(date);
		} else {
			return isBusinessDay;
		}
	}

	public Type determinateType(FXTrade trade) throws TradistaBusinessException {
		if (trade.getType() == null) {
			// FX Spot are generally settled in T+2.
			// there are some exceptions, see
			// https://en.wikipedia.org/wiki/Foreign_exchange_spot#Settlement_date
			// The settlement day must be open for both currencies.
			Type type = null;
			LocalDate maxSettleDate = DateUtil.addBusinessDay(trade.getTradeDate(), getFXExchange().getCalendar(), 2);
			boolean dateFound = false;
			while (!dateFound) {
				if (isBusinessDay(trade, maxSettleDate)) {
					dateFound = true;
				} else {
					maxSettleDate = DateUtil.nextBusinessDay(trade.getTradeDate(), getFXExchange().getCalendar());
				}
			}
			if (trade.getSettlementDate().isAfter(maxSettleDate)) {
				type = Type.FX_FORWARD;
			} else {
				type = Type.FX_SPOT;
			}
			trade.setType(type);
		}
		return trade.getType();
	}

	public BigDecimal getExchangeRate(FXTrade trade) throws TradistaBusinessException {
		if (trade == null) {
			throw new TradistaBusinessException("The trade is mandatory.");
		}
		return SecurityUtil.run(() -> trade.getAmount().divide(trade.getAmountOne(),
				configurationBusinessDelegate.getScale(), configurationBusinessDelegate.getRoundingMode()));
	}

	public FXTrade getFXTradeById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The trade id must be positive.");
		}
		return SecurityUtil.run(() -> fxTradeService.getFXTradeById(id));
	}

}