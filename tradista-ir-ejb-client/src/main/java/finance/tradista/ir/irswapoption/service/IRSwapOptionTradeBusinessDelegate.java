package finance.tradista.ir.irswapoption.service;

import java.time.LocalDate;

import finance.tradista.core.calendar.model.Calendar;
import finance.tradista.core.common.exception.TradistaBusinessException;
import finance.tradista.core.common.servicelocator.TradistaServiceLocator;
import finance.tradista.core.common.util.SecurityUtil;
import finance.tradista.core.currency.model.Currency;
import finance.tradista.core.product.model.Product;
import finance.tradista.core.trade.model.Trade;
import finance.tradista.ir.irswapoption.model.IRSwapOptionTrade;
import finance.tradista.ir.irswapoption.validator.IRSwapOptionTradeValidator;

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