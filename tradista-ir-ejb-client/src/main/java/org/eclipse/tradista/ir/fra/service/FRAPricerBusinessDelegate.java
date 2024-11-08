package org.eclipse.tradista.ir.fra.service;

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
import org.eclipse.tradista.ir.fra.model.FRATrade;
import org.eclipse.tradista.ir.fra.service.FRAPricerService;
import org.eclipse.tradista.ir.fra.validator.FRATradeValidator;

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

public class FRAPricerBusinessDelegate implements Serializable {

	private static final long serialVersionUID = -7137084591554434226L;

	private FRAPricerService fraPricerService;

	private FRATradeValidator validator;

	public FRAPricerBusinessDelegate() {
		fraPricerService = TradistaServiceLocator.getInstance().getFRAPricerService();
		validator = new FRATradeValidator();
	}

	public BigDecimal npvValuation(PricingParameter params, FRATrade trade, Currency currency, LocalDate pricingDate)
			throws PricerException, TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> fraPricerService.npvValuation(params, trade, currency, pricingDate));
	}

	public BigDecimal pnlDefault(PricingParameter params, FRATrade trade, Currency currency, LocalDate pricingDate)
			throws PricerException, TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> fraPricerService.pnlDefault(params, trade, currency, pricingDate));
	}

	public BigDecimal realizedPnlDefault(PricingParameter params, FRATrade trade, Currency currency,
			LocalDate pricingDate) throws PricerException, TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> fraPricerService.realizedPnlDefault(params, trade, currency, pricingDate));
	}

	public BigDecimal unrealizedPnlDefault(PricingParameter params, FRATrade trade, Currency currency,
			LocalDate pricingDate) throws PricerException, TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> fraPricerService.unrealizedPnlDefault(params, trade, currency, pricingDate));
	}

	public List<CashFlow> generateCashFlows(FRATrade trade, PricingParameter pp, LocalDate pricingDate)
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
		return SecurityUtil.runEx(() -> fraPricerService.generateCashFlows(pp, trade, pricingDate));
	}

}