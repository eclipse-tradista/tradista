package org.eclipse.tradista.security.equity.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.model.EquityTrade;
import org.eclipse.tradista.security.equity.validator.EquityTradeValidator;
import org.eclipse.tradista.security.equity.validator.EquityValidator;

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

public class EquityPricerBusinessDelegate implements Serializable {

	private static final long serialVersionUID = -9015095965433017363L;

	private EquityPricerService equityPricerService;

	private EquityTradeValidator tradeValidator;

	private EquityValidator equityValidator;

	public EquityPricerBusinessDelegate() {
		equityPricerService = TradistaServiceLocator.getInstance().getEquityPricerService();
		tradeValidator = new EquityTradeValidator();
		equityValidator = new EquityValidator();
	}

	public BigDecimal pvMonteCarloSimulation(PricingParameter params, EquityTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		tradeValidator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> equityPricerService.pvMonteCarloSimulation(params, trade, currency, pricingDate));
	}

	public BigDecimal npvMontecarloSimulation(PricingParameter params, EquityTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		tradeValidator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> equityPricerService.npvMontecarloSimulation(params, trade, currency, pricingDate));
	}

	public BigDecimal expectedReturnCapm(PricingParameter params, EquityTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		tradeValidator.validateTrade(trade);
		return SecurityUtil.runEx(() -> equityPricerService.expectedReturnCapm(params, trade, currency, pricingDate));
	}

	public BigDecimal pnlDefault(PricingParameter params, Equity equity, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		equityValidator.validateProduct(equity);
		return equityPricerService.pnlDefault(params, equity, book, currency, pricingDate);
	}

	public BigDecimal realizedPnlDefault(PricingParameter params, Equity equity, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		equityValidator.validateProduct(equity);
		return SecurityUtil
				.runEx(() -> equityPricerService.realizedPnlDefault(params, equity, book, currency, pricingDate));
	}

	public BigDecimal unrealizedPnlMarkToMarket(PricingParameter params, Equity equity, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		equityValidator.validateProduct(equity);
		return SecurityUtil.runEx(
				() -> equityPricerService.unrealizedPnlMarkToMarket(params, equity, book, currency, pricingDate));
	}

	public BigDecimal unrealizedPnlMarkToModel(PricingParameter params, Equity equity, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		equityValidator.validateProduct(equity);
		return SecurityUtil
				.runEx(() -> equityPricerService.unrealizedPnlMarkToModel(params, equity, book, currency, pricingDate));
	}

	public List<CashFlow> generateCashFlows(EquityTrade trade, PricingParameter pp, LocalDate pricingDate)
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
		tradeValidator.validateTrade(trade);
		return SecurityUtil.runEx(() -> equityPricerService.generateCashFlows(pp, trade, pricingDate));
	}

}