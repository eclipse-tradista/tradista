package org.eclipse.tradista.fx.fxndf.service;

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

public class FXNDFPricerBusinessDelegate implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7137084591554434226L;

	private FXNDFPricerService fxNdfPricerService;

	private FXNDFTradeValidator validator;

	public FXNDFPricerBusinessDelegate() {
		fxNdfPricerService = TradistaServiceLocator.getInstance().getFXNDFPricerService();
		validator = new FXNDFTradeValidator();
	}

	public BigDecimal npvDiscountedLegsDiff(PricingParameter params, FXNDFTrade trade, Currency currency,
			LocalDate pricingDate) throws PricerException, TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> fxNdfPricerService.npvDiscountedLegsDiff(params, trade, currency, pricingDate));
	}

	public BigDecimal realizedPnlMarkToMarket(PricingParameter params, FXNDFTrade trade, Currency currency,
			LocalDate pricingDate) throws PricerException, TradistaBusinessException {
		validator.validateTrade(trade);
		return fxNdfPricerService.realizedPnlMarkToMarket(params, trade, currency, pricingDate);
	}

	public BigDecimal unrealizedPnlLegsDiff(PricingParameter params, FXNDFTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException, PricerException {
		validator.validateTrade(trade);
		return fxNdfPricerService.unrealizedPnlLegsDiff(params, trade, currency, pricingDate);
	}

	public BigDecimal pnlDefault(PricingParameter params, FXNDFTrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException, PricerException {
		validator.validateTrade(trade);
		return fxNdfPricerService.pnlDefault(params, trade, currency, pricingDate);
	}

	public List<CashFlow> generateCashFlows(FXNDFTrade trade, PricingParameter pp, LocalDate pricingDate)
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
		return fxNdfPricerService.generateCashFlows(pp, trade, pricingDate);
	}

	public BigDecimal unrealizedPnlMarkToMarket(PricingParameter params, FXNDFTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException, PricerException {
		validator.validateTrade(trade);
		return fxNdfPricerService.unrealizedPnlMarkToMarket(params, trade, currency, pricingDate);
	}

}