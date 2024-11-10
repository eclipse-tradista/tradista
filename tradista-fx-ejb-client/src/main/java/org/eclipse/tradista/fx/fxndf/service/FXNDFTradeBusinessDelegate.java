package org.eclipse.tradista.fx.fxndf.service;

import java.time.LocalDate;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.fx.fx.service.AbstractFXTradeBusinessDelegate;
import org.eclipse.tradista.fx.fxndf.model.FXNDFTrade;
import org.eclipse.tradista.fx.fxndf.validator.FXNDFTradeValidator;

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

public class FXNDFTradeBusinessDelegate extends AbstractFXTradeBusinessDelegate<FXNDFTrade> {

	private FXNDFTradeService fxNDFTradeService;

	private FXNDFTradeValidator validator;

	public FXNDFTradeBusinessDelegate() {
		fxNDFTradeService = TradistaServiceLocator.getInstance().getFXNDFTradeService();
		validator = new FXNDFTradeValidator();
	}

	public long saveFXNDFTrade(FXNDFTrade trade) throws TradistaBusinessException {

		validator.validateTrade(trade);

		return SecurityUtil.runEx(() -> fxNDFTradeService.saveFXNDFTrade(trade));
	}

	@Override
	public boolean isBusinessDay(FXNDFTrade fxTrade, LocalDate date) throws TradistaBusinessException {
		Currency nonDeliverableCurrency = fxTrade.getNonDeliverableCurrency();
		Calendar nonDeliverableCurrencyCalendar;
		if (nonDeliverableCurrency == null) {
			throw new TradistaBusinessException("The FX NDF trade non deliverable currency cannot be null");
		}
		nonDeliverableCurrencyCalendar = nonDeliverableCurrency.getCalendar();
		if (nonDeliverableCurrencyCalendar == null) {
			// TODO Add a warning log
		}
		boolean isBusinessDay = super.isBusinessDay(fxTrade, date);
		if (nonDeliverableCurrencyCalendar != null) {
			return isBusinessDay && nonDeliverableCurrencyCalendar.isBusinessDay(date);
		} else {
			return isBusinessDay;
		}
	}

	public LocalDate getFixingDate(FXNDFTrade trade) throws TradistaBusinessException {
		if (trade.getSettlementDate() == null) {
			throw new TradistaBusinessException("The settlement date must exist to calculate the fixing date.");
		}
		if (trade.getNonDeliverableCurrency() == null) {
			throw new TradistaBusinessException(
					"The non deliverable currency must exist to calculate the fixing date.");
		}
		if (trade.getCurrency() == null) {
			throw new TradistaBusinessException("The deliverable currency must exist to calculate the fixing date.");
		}
		boolean fixingDateFound = false;
		LocalDate fixingDate = DateUtil.addBusinessDay(trade.getSettlementDate(), getFXExchange().getCalendar(),
				trade.getNonDeliverableCurrency().getFixingDateOffset());
		while (!fixingDateFound) {
			if ((trade.getCurrency().getCalendar() != null ? trade.getCurrency().getCalendar().isBusinessDay(fixingDate)
					: true)
					&& (trade.getNonDeliverableCurrency().getCalendar() != null
							? trade.getNonDeliverableCurrency().getCalendar().isBusinessDay(fixingDate)
							: true)) {
				fixingDateFound = true;
			} else {
				fixingDate = DateUtil.addBusinessDay(trade.getSettlementDate(), getFXExchange().getCalendar(), -1);
			}
		}
		return fixingDate;
	}

	public FXNDFTrade getFXNDFTradeById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The trade id must be positive.");
		}
		return SecurityUtil.run(() -> fxNDFTradeService.getFXNDFTradeById(id));
	}

}