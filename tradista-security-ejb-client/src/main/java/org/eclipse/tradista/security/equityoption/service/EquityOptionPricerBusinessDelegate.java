package org.eclipse.tradista.security.equityoption.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.security.equityoption.model.EquityOption;
import org.eclipse.tradista.security.equityoption.model.EquityOptionTrade;
import org.eclipse.tradista.security.equityoption.validator.EquityOptionTradeValidator;
import org.eclipse.tradista.security.equityoption.validator.EquityOptionValidator;

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

public class EquityOptionPricerBusinessDelegate implements Serializable {

	private static final long serialVersionUID = -9015095965433017363L;

	private EquityOptionValidator equityOptionValidator;

	private EquityOptionTradeValidator equityOptionTradeValidator;

	private EquityOptionPricerService equityOptionPricerService;

	public EquityOptionPricerBusinessDelegate() {
		equityOptionPricerService = TradistaServiceLocator.getInstance().getEquityOptionPricerService();
		equityOptionValidator = new EquityOptionValidator();
		equityOptionTradeValidator = new EquityOptionTradeValidator();
	}

	public BigDecimal pvCoxRossRubinstein(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		equityOptionTradeValidator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> equityOptionPricerService.pvCoxRossRubinstein(params, trade, currency, pricingDate));
	}

	public BigDecimal npvBlackAndScholes(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		equityOptionTradeValidator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> equityOptionPricerService.npvBlackAndScholes(params, trade, currency, pricingDate));
	}

	public BigDecimal expectedReturnCapm(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		equityOptionTradeValidator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> equityOptionPricerService.expectedReturnCapm(params, trade, currency, pricingDate));
	}

	public BigDecimal pvBlackAndScholes(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		equityOptionTradeValidator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> equityOptionPricerService.pvBlackAndScholes(params, trade, currency, pricingDate));
	}

	public BigDecimal npvCoxRossRubinstein(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		equityOptionTradeValidator.validateTrade(trade);
		return SecurityUtil.runEx(() -> npvCoxRossRubinstein(params, trade, currency, pricingDate));
	}

	public BigDecimal realizedPnlOptionExercise(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		equityOptionTradeValidator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> equityOptionPricerService.realizedPnlOptionExercise(params, trade, currency, pricingDate));
	}

	public BigDecimal unrealizedPnlBlackAndScholes(PricingParameter params, EquityOption equityOption, Book book,
			Currency currency, LocalDate pricingDate) throws TradistaBusinessException {
		equityOptionValidator.validateProduct(equityOption);
		return SecurityUtil.runEx(() -> equityOptionPricerService.unrealizedPnlBlackAndScholes(params, equityOption,
				book, currency, pricingDate));
	}

	public BigDecimal pnlDefault(PricingParameter params, EquityOption equityOption, Book book, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		equityOptionValidator.validateProduct(equityOption);
		return SecurityUtil
				.runEx(() -> equityOptionPricerService.pnlDefault(params, equityOption, book, currency, pricingDate));
	}

	public BigDecimal realizedPnlDefault(PricingParameter params, EquityOption equityOption, Book book,
			Currency currency, LocalDate pricingDate) throws TradistaBusinessException {
		equityOptionValidator.validateProduct(equityOption);
		return SecurityUtil.runEx(
				() -> equityOptionPricerService.realizedPnlDefault(params, equityOption, book, currency, pricingDate));
	}

	public BigDecimal unrealizedPnlBlackAndScholes(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		equityOptionTradeValidator.validateTrade(trade);
		return SecurityUtil.runEx(
				() -> equityOptionPricerService.unrealizedPnlBlackAndScholes(params, trade, currency, pricingDate));
	}

	public BigDecimal pnlDefault(PricingParameter params, EquityOptionTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		equityOptionTradeValidator.validateTrade(trade);
		return SecurityUtil.runEx(() -> equityOptionPricerService.pnlDefault(params, trade, currency, pricingDate));
	}

	public List<CashFlow> generateCashFlows(EquityOptionTrade trade, PricingParameter pp, LocalDate pricingDate)
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
		equityOptionTradeValidator.validateTrade(trade);
		// Dummy method for the moment, cashflows generation of optional products is a
		// pending topic.
		return new ArrayList<CashFlow>();
	}

}