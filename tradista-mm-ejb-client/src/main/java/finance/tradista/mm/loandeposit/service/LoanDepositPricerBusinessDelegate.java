package org.eclipse.tradista.mm.loandeposit.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.mm.loandeposit.model.LoanDepositTrade;
import org.eclipse.tradista.mm.loandeposit.validator.LoanDepositTradeValidator;

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

public class LoanDepositPricerBusinessDelegate implements Serializable {

	private static final long serialVersionUID = 5575888221370103740L;

	private LoanDepositPricerService loanDepositPricerService;

	private LoanDepositTradeValidator validator;

	public LoanDepositPricerBusinessDelegate() {
		loanDepositPricerService = TradistaServiceLocator.getInstance().getLoanDepositPricerService();
		validator = new LoanDepositTradeValidator();
	}

	public BigDecimal npvDiscountedCashFlow(PricingParameter params, LoanDepositTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> loanDepositPricerService.npvDiscountedCashFlow(params, trade, currency, pricingDate));
	}

	public BigDecimal pnlDefault(PricingParameter params, LoanDepositTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> loanDepositPricerService.pnlDefault(params, trade, currency, pricingDate));
	}

	public BigDecimal realizedPnlCashFlows(PricingParameter params, LoanDepositTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> loanDepositPricerService.realizedPnlCashFlows(params, trade, currency, pricingDate));
	}

	public BigDecimal unrealizedPnlDefault(PricingParameter params, LoanDepositTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(
				() -> loanDepositPricerService.unrealizedPnlDiscountedCashFlow(params, trade, currency, pricingDate));
	}

	public List<CashFlow> generateCashFlows(LoanDepositTrade trade, PricingParameter pp, LocalDate pricingDate)
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
		return SecurityUtil.runEx(() -> loanDepositPricerService.generateCashFlows(pp, trade, pricingDate));
	}

}