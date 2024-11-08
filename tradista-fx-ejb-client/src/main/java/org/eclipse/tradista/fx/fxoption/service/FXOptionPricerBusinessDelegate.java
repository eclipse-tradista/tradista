package org.eclipse.tradista.fx.fxoption.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.fx.fxoption.model.FXOptionTrade;
import org.eclipse.tradista.fx.fxoption.service.FXOptionPricerService;
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

public class FXOptionPricerBusinessDelegate implements Serializable {

	private static final long serialVersionUID = -7137084591554434226L;

	private FXOptionPricerService fxOptionPricerService;

	private FXOptionTradeValidator fxOptionTradeValidator;

	public FXOptionPricerBusinessDelegate() {
		fxOptionPricerService = TradistaServiceLocator.getInstance().getFXOptionPricerService();
		fxOptionTradeValidator = new FXOptionTradeValidator();
	}

	public BigDecimal npvBlacBkAndScholes(PricingParameter params, FXOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		fxOptionTradeValidator.validateTrade(trade);
		return SecurityUtil.runEx(() -> fxOptionPricerService.npvBlackAndScholes(params, trade, currency, pricingDate));
	}

	public BigDecimal npvCoxRossRubinstein(PricingParameter params, FXOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		fxOptionTradeValidator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> fxOptionPricerService.npvCoxRossRubinstein(params, trade, currency, pricingDate));
	}

	public BigDecimal pvBlacBkAndScholes(PricingParameter params, FXOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		fxOptionTradeValidator.validateTrade(trade);
		return SecurityUtil.runEx(() -> fxOptionPricerService.pvBlackAndScholes(params, trade, currency, pricingDate));
	}

	public BigDecimal pvCoxRossRubinstein(PricingParameter params, FXOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		fxOptionTradeValidator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> fxOptionPricerService.pvCoxRossRubinstein(params, trade, currency, pricingDate));
	}

	public BigDecimal realizedPnlMarkToMarket(PricingParameter params, FXOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		fxOptionTradeValidator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> fxOptionPricerService.realizedPnlMarkToMarket(params, trade, currency, pricingDate));
	}

	public BigDecimal unrealizedPnlBlackAndScholes(PricingParameter params, FXOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		fxOptionTradeValidator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> fxOptionPricerService.unrealizedPnlBlackAndScholes(params, trade, currency, pricingDate));
	}

	public BigDecimal pnlDefault(PricingParameter params, FXOptionTrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException {
		fxOptionTradeValidator.validateTrade(trade);
		return SecurityUtil.runEx(() -> fxOptionPricerService.pnlDefault(params, trade, currency, pricingDate));
	}

	public BigDecimal unrealizedPnlMarkToMarket(PricingParameter params, FXOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		fxOptionTradeValidator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> fxOptionPricerService.unrealizedPnlMarkToMarket(params, trade, currency, pricingDate));
	}

}