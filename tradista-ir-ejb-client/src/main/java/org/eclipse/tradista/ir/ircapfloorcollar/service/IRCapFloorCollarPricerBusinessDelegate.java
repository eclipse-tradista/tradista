package org.eclipse.tradista.ir.ircapfloorcollar.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.ir.ircapfloorcollar.model.IRCapFloorCollarTrade;
import org.eclipse.tradista.ir.ircapfloorcollar.validator.IRCapFloorCollarTradeValidator;

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

public class IRCapFloorCollarPricerBusinessDelegate implements Serializable {

	private static final long serialVersionUID = -7137084591554434226L;

	private IRCapFloorCollarPricerService irCapFloorCollarPricerService;

	private IRCapFloorCollarTradeValidator validator;

	public IRCapFloorCollarPricerBusinessDelegate() {
		irCapFloorCollarPricerService = TradistaServiceLocator.getInstance().getIRCapFloorCollarPricerService();
		validator = new IRCapFloorCollarTradeValidator();
	}

	public BigDecimal npvBlack(PricingParameter params, IRCapFloorCollarTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		validator.validateTrade(trade);

		return SecurityUtil.runEx(() -> irCapFloorCollarPricerService.npvBlack(params, trade, currency, pricingDate));
	}

	public BigDecimal pvBlack(PricingParameter params, IRCapFloorCollarTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		validator.validateTrade(trade);

		return SecurityUtil.runEx(() -> irCapFloorCollarPricerService.pvBlack(params, trade, currency, pricingDate));
	}

	public BigDecimal pnlDefault(PricingParameter params, IRCapFloorCollarTrade trade, Currency currency,
			LocalDate pricingDate) throws TradistaBusinessException {

		validator.validateTrade(trade);

		return SecurityUtil.runEx(() -> irCapFloorCollarPricerService.pnlDefault(params, trade, currency, pricingDate));
	}

	public BigDecimal realizedPnlPaymentTriggers(PricingParameter params, IRCapFloorCollarTrade trade,
			Currency currency, LocalDate pricingDate) throws TradistaBusinessException {

		validator.validateTrade(trade);
		return SecurityUtil.runEx(
				() -> irCapFloorCollarPricerService.realizedPnlPaymentTriggers(params, trade, currency, pricingDate));
	}

	public BigDecimal unrealizedPnlDefault(PricingParameter params, IRCapFloorCollarTrade trade, Currency currency,
			LocalDate pricingDate) throws PricerException, TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> irCapFloorCollarPricerService.unrealizedPnlBlack(params, trade, currency, pricingDate));
	}

	public List<CashFlow> generateCashFlows(IRCapFloorCollarTrade trade, PricingParameter pp, LocalDate pricingDate)
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
		// Dummy method for the moment, cashflows generation of optional products is a
		// pending topic.
		return new ArrayList<CashFlow>();
	}
}