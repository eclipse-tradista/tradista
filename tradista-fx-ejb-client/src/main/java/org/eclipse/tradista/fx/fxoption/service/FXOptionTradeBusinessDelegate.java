package org.eclipse.tradista.fx.fxoption.service;

import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.fx.fx.service.FXTradeBusinessDelegate;
import org.eclipse.tradista.fx.fxoption.model.FXOptionTrade;
import org.eclipse.tradista.fx.fxoption.validator.FXOptionTradeValidator;

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

public class FXOptionTradeBusinessDelegate {

	private FXOptionTradeService fxOptionTradeService;

	private FXOptionTradeValidator validator;

	private FXTradeBusinessDelegate fxTradeBusinessDelegate;

	public FXOptionTradeBusinessDelegate() {
		fxOptionTradeService = TradistaServiceLocator.getInstance().getFXOptionTradeService();
		validator = new FXOptionTradeValidator();
		fxTradeBusinessDelegate = new FXTradeBusinessDelegate();
	}

	public long saveFXOptionTrade(FXOptionTrade trade) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> fxOptionTradeService.saveFXOptionTrade(trade));
	}

	public boolean isBusinessDay(FXOptionTrade fxOptionTrade, LocalDate date) throws TradistaBusinessException {
		return fxTradeBusinessDelegate.isBusinessDay(fxOptionTrade.getUnderlying(), date);
	}

	public Exchange getFXExchange() {
		return fxTradeBusinessDelegate.getFXExchange();
	}

	public FXOptionTrade getFXOptionTradeById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The trade id must be positive.");
		}
		return SecurityUtil.runEx(() -> fxOptionTradeService.getFXOptionTradeById(id));
	}

}