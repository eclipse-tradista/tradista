package org.eclipse.tradista.security.bond.service;

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
import org.eclipse.tradista.security.bond.model.Bond;
import org.eclipse.tradista.security.bond.model.BondTrade;
import org.eclipse.tradista.security.bond.validator.BondTradeValidator;
import org.eclipse.tradista.security.bond.validator.BondValidator;

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

public class BondPricerBusinessDelegate implements Serializable {

	private static final long serialVersionUID = -9015095965433017363L;

	private BondPricerService bondPricerService;

	private BondTradeValidator tradeValidator;

	private BondValidator bondValidator;

	public BondPricerBusinessDelegate() {
		bondPricerService = TradistaServiceLocator.getInstance().getBondPricerService();
		tradeValidator = new BondTradeValidator();
		bondValidator = new BondValidator();
	}

	public BigDecimal ytmNewtonRaphson(PricingParameter params, BondTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		tradeValidator.validateTrade(trade);
		return SecurityUtil.runEx(() -> bondPricerService.ytmNewtonRaphson(params, trade, currency, pricingDate));
	}

	public BigDecimal parYieldParYield(PricingParameter params, BondTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		tradeValidator.validateTrade(trade);
		return SecurityUtil.runEx(() -> bondPricerService.parYieldParYield(params, trade, currency, pricingDate));
	}

	public BigDecimal npvDiscountedCashFlow(PricingParameter params, BondTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		tradeValidator.validateTrade(trade);
		return SecurityUtil.runEx(() -> bondPricerService.npvDiscountedCashFlow(params, trade, currency, pricingDate));
	}

	public BigDecimal pvDiscountedCashFlow(PricingParameter params, BondTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		tradeValidator.validateTrade(trade);
		return SecurityUtil.runEx(() -> bondPricerService.npvDiscountedCashFlow(params, trade, currency, pricingDate));
	}

	public BigDecimal cleanPriceDiscountedCashFlow(PricingParameter params, BondTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		tradeValidator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> bondPricerService.cleanPriceDiscountedCashFlow(params, trade, currency, pricingDate));
	}

	public BigDecimal dirtyPriceDiscountedCashFlow(PricingParameter params, BondTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		tradeValidator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> bondPricerService.dirtyPriceDiscountedCashFlow(params, trade, currency, pricingDate));
	}

	public BigDecimal pnlDefault(PricingParameter params, Bond bond, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		bondValidator.validateProduct(bond);
		return SecurityUtil.runEx(() -> bondPricerService.pnlDefault(params, bond, book, currency, pricingDate));
	}

	public BigDecimal realizedPnlDefault(PricingParameter params, Bond bond, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		bondValidator.validateProduct(bond);
		return SecurityUtil
				.runEx(() -> bondPricerService.realizedPnlDefault(params, bond, book, currency, pricingDate));
	}

	public BigDecimal unrealizedPnlDefault(PricingParameter params, Bond bond, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		bondValidator.validateProduct(bond);
		return SecurityUtil
				.runEx(() -> bondPricerService.unrealizedPnlDefault(params, bond, book, currency, pricingDate));
	}

	public List<CashFlow> generateCashFlows(BondTrade trade, PricingParameter pp, LocalDate pricingDate)
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
		return SecurityUtil.runEx(() -> bondPricerService.generateCashFlows(pp, trade, pricingDate));
	}

}