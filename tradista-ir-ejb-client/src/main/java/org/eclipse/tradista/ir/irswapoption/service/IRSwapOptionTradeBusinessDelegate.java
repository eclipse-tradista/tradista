package org.eclipse.tradista.ir.irswapoption.service;

import java.time.LocalDate;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.ir.irswapoption.model.IRSwapOptionTrade;
import org.eclipse.tradista.ir.irswapoption.validator.IRSwapOptionTradeValidator;

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

public class IRSwapOptionTradeBusinessDelegate {

	private IRSwapOptionTradeService irSwapOptionTradeService;

	private IRSwapOptionTradeValidator validator;

	public IRSwapOptionTradeBusinessDelegate() {
		irSwapOptionTradeService = TradistaServiceLocator.getInstance().getIRSwapOptionTradeService();
		validator = new IRSwapOptionTradeValidator();
	}

	public long saveIRSwapOptionTrade(IRSwapOptionTrade trade) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> irSwapOptionTradeService.saveIRSwapOptionTrade(trade));
	}

	public boolean isBusinessDay(IRSwapOptionTrade trade, LocalDate date) throws TradistaBusinessException {
		Currency underlyingCurrency;
		Trade<Product> underlying;
		Calendar currencyCalendar;
		if (trade == null) {
			throw new TradistaBusinessException("The IR Swap Option trade cannot be null");
		}
		underlying = trade.getUnderlying();
		if (underlying == null) {
			throw new TradistaBusinessException("The IR Swap Option trade underlying cannot be null");
		}
		underlyingCurrency = underlying.getCurrency();
		if (underlyingCurrency == null) {
			throw new TradistaBusinessException("The IR Swap Option trade underlying currency cannot be null");
		}
		currencyCalendar = underlyingCurrency.getCalendar();
		if (currencyCalendar == null) {
			// TODO Add warning log
		}
		if (currencyCalendar != null) {
			return currencyCalendar.isBusinessDay(date);
		} else {
			return true;
		}
	}

	public IRSwapOptionTrade getIRSwapOptionTradeById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The trade id must be positive.");
		}
		return SecurityUtil.run(() -> irSwapOptionTradeService.getIRSwapOptionTradeById(id));
	}

}