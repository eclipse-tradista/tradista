package org.eclipse.tradista.fx.fxswap.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.fx.fxswap.model.FXSwapTrade;
import org.eclipse.tradista.fx.fxswap.service.FXSwapPricerService;
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

public class FXSwapPricerBusinessDelegate implements Serializable {

	private static final long serialVersionUID = -7934812925515293266L;
	private FXSwapPricerService fxSwapPricerService;
	private FXSwapTradeValidator validator;

	public FXSwapPricerBusinessDelegate() {
		fxSwapPricerService = TradistaServiceLocator.getInstance().getFXSwapPricerService();
		validator = new FXSwapTradeValidator();
	}

	public BigDecimal fwdLegNpvDiscountedLegsDiff(PricingParameter params, FXSwapTrade trade, Currency currency,
			LocalDate pricingDate) throws PricerException, TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> fxSwapPricerService.fwdLegNpvDiscountedLegsDiff(params, trade, currency, pricingDate));
	}

	public BigDecimal npvDiscountedLegsDiff(PricingParameter params, FXSwapTrade trade, Currency currency,
			LocalDate pricingDate) throws PricerException, TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> fxSwapPricerService.npvDiscountedLegsDiff(params, trade, currency, pricingDate));
	}

	public BigDecimal spotLegNpvDiscountedLegsDiff(PricingParameter params, FXSwapTrade trade, Currency currency,
			LocalDate pricingDate) throws PricerException, TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> fxSwapPricerService.spotLegNpvDiscountedLegsDiff(params, trade, currency, pricingDate));
	}

	public BigDecimal realizedPnlMarkToMarket(PricingParameter params, FXSwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException, PricerException {
		validator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> fxSwapPricerService.realizedPnlMarkToMarket(params, trade, currency, pricingDate));
	}

	public BigDecimal unrealizedPnlLegsDiff(PricingParameter params, FXSwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException, PricerException {
		validator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> fxSwapPricerService.unrealizedPnlLegsDiff(params, trade, currency, pricingDate));
	}

	public BigDecimal defaultPNL(PricingParameter params, FXSwapTrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException, PricerException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> fxSwapPricerService.defaultPNL(params, trade, currency, pricingDate));
	}

	public List<CashFlow> generateCashFlows(FXSwapTrade trade, PricingParameter pp, LocalDate pricingDate)
			throws TradistaBusinessException {
		StringBuffer errorMsg = new StringBuffer();
		if (trade == null) {
			errorMsg.append(String.format("The trade cannot be null.%n"));
		}
		if (pp == null) {
			errorMsg.append(String.format("The pricing parameters cannot be null.%n"));
		}
		if (pricingDate == null) {
			errorMsg.append(String.format("The pricing date cannot be null.%n"));
		}
		if (errorMsg.length() > 0) {
			throw new TradistaBusinessException(errorMsg.toString());
		}
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> fxSwapPricerService.generateCashFlows(pp, trade, pricingDate));
	}

	public BigDecimal unrealizedPnlMarkToMarket(PricingParameter params, FXSwapTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException, PricerException {
		validator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> fxSwapPricerService.unrealizedPnlMarkToMarket(params, trade, currency, pricingDate));
	}

}