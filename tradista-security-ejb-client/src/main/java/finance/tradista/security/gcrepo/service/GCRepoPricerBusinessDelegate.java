package org.eclipse.tradista.security.gcrepo.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.security.common.model.Security;
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;
import org.eclipse.tradista.security.gcrepo.validator.GCRepoTradeValidator;

/********************************************************************************
 * Copyright (c) 2023 Olivier Asuncion
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

public class GCRepoPricerBusinessDelegate implements Serializable {

	private static final long serialVersionUID = 6702432232737370768L;

	private GCRepoPricerService gcRepoPricerService;

	private GCRepoTradeValidator validator;

	private static final String PRICING_DATE_IS_MANDATORY = "The pricing date is mandatory.%n";

	private static final String TRADE_IS_MANDATORY = "The trade is mandatory.%n";

	private static final String PRICING_PARAMETERS_SET_IS_MANDATORY = "The Pricing Parameters Set is mandatory.%n";

	public GCRepoPricerBusinessDelegate() {
		gcRepoPricerService = TradistaServiceLocator.getInstance().getGCRepoPricerService();
		validator = new GCRepoTradeValidator();
	}

	public List<CashFlow> generateCashFlows(GCRepoTrade trade, PricingParameter pp, LocalDate pricingDate)
			throws TradistaBusinessException {
		StringBuilder errorMsg = new StringBuilder();
		if (trade == null) {
			errorMsg.append(String.format(TRADE_IS_MANDATORY));
		}
		if (pp == null) {
			errorMsg.append(String.format(PRICING_PARAMETERS_SET_IS_MANDATORY));
		}
		if (pricingDate == null) {
			errorMsg.append(String.format(PRICING_DATE_IS_MANDATORY));
		}
		if (errorMsg.length() > 0) {
			throw new TradistaBusinessException(errorMsg.toString());
		}
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> gcRepoPricerService.generateCashFlows(pp, trade, pricingDate));
	}

	public BigDecimal getCollateralMarketToMarket(GCRepoTrade trade, Currency currency, LocalDate pricingDate,
			PricingParameter params) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (trade == null) {
			errMsg.append(String.format(TRADE_IS_MANDATORY));
		} else {
			if (trade.getId() == 0) {
				errMsg.append(String.format("The trade must be an existing one.%n"));
			}
			try {
				validator.validateTrade(trade);
			} catch (TradistaBusinessException tbe) {
				errMsg.append(tbe.getMessage());
			}
		}
		if (params == null) {
			errMsg.append(String.format(PRICING_PARAMETERS_SET_IS_MANDATORY));
		}
		if (pricingDate == null) {
			errMsg.append(String.format(PRICING_DATE_IS_MANDATORY));
		} else {
			if (pricingDate.isAfter(LocalDate.now())) {
				errMsg.append(String.format(
						"Pricing date (%tD) cannot be in the future to calculate Collateral Mark To Market",
						pricingDate));
			}
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil
				.runEx(() -> gcRepoPricerService.getCollateralMarketToMarket(trade, currency, pricingDate, params));
	}

	public BigDecimal getCurrentCollateralMarketToMarket(GCRepoTrade trade) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (trade == null) {
			errMsg.append(String.format(TRADE_IS_MANDATORY));
		} else {
			if (trade.getId() == 0) {
				errMsg.append(String.format("The trade must be an existing one.%n"));
			}
			try {
				validator.validateTrade(trade);
			} catch (TradistaBusinessException tbe) {
				errMsg.append(tbe.getMessage());
			}
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil.runEx(() -> gcRepoPricerService.getCurrentCollateralMarketToMarket(trade));
	}

	public BigDecimal getCollateralMarketToMarket(Map<Security, Map<Book, BigDecimal>> securities, LegalEntity po,
			LocalDate pricingDate) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (po == null) {
			errMsg.append(String.format("The Processing Org is mandatory.%n"));
		}
		if (securities == null || securities.isEmpty()) {
			errMsg.append(String.format("Securities are mandatory.%n"));
		}
		if (pricingDate == null) {
			errMsg.append(String.format(PRICING_DATE_IS_MANDATORY));
		} else {
			if (pricingDate.isAfter(LocalDate.now())) {
				errMsg.append(String.format(
						"Pricing date (%tD) cannot be in the future to calculate the collateral Mark To Market",
						pricingDate));
			}
		}
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil.runEx(() -> gcRepoPricerService.getCollateralMarketToMarket(securities, po, pricingDate));
	}

	public BigDecimal getExposure(GCRepoTrade trade, Currency currency, LocalDate pricingDate, PricingParameter params)
			throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (trade == null) {
			throw new TradistaBusinessException(String.format(TRADE_IS_MANDATORY));
		} else {
			try {
				validator.validateTrade(trade);
			} catch (TradistaBusinessException tbe) {
				errMsg.append(tbe.getMessage());
			}
		}
		if (params == null) {
			errMsg.append(String.format(PRICING_PARAMETERS_SET_IS_MANDATORY));
		}
		if (currency == null) {
			errMsg.append(String.format("The currency is mandatory.%n"));
		}
		if (pricingDate == null) {
			errMsg.append(String.format(PRICING_DATE_IS_MANDATORY));
		} else {
			if (pricingDate.isBefore(trade.getSettlementDate())) {
				errMsg.append(String.format(
						"Pricing date (%tD) cannot be before the trade settlement date (%tD) to calculate the exposure",
						pricingDate, trade.getSettlementDate()));
			}
			if (pricingDate.isAfter(trade.getEndDate())) {
				return BigDecimal.ZERO;
			}
			if (pricingDate.isAfter(LocalDate.now())) {
				if (!trade.isFixedRepoRate()) {
					InterestRateCurve indexCurve = params.getIndexCurves().get(trade.getIndex());
					if (indexCurve == null) {
						throw new TradistaBusinessException(String.format(
								"Pricing Parameters Set '%s' doesn't contain an Index Curve for %s. please add it or change the Pricing Parameters Set.",
								params.getName(), trade.getIndex()));
					}
				}
			}
		}
		return SecurityUtil.runEx(() -> gcRepoPricerService.getExposure(trade, currency, pricingDate, params));
	}

	public BigDecimal getCurrentExposure(GCRepoTrade trade) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> gcRepoPricerService.getCurrentExposure(trade));
	}

	public BigDecimal getCurrentCollateralValue(GCRepoTrade trade) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> gcRepoPricerService.getCurrentExposure(trade));
	}

	public BigDecimal pnlDefault(GCRepoTrade trade, Currency currency, LocalDate pricingDate, PricingParameter params)
			throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> gcRepoPricerService.pnlDefault(trade, currency, pricingDate, params));
	}

	public BigDecimal realizedPayments(GCRepoTrade trade, Currency currency, LocalDate pricingDate,
			PricingParameter params) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> gcRepoPricerService.realizedPayments(trade, currency, pricingDate, params));
	}

	public BigDecimal discountedPayments(GCRepoTrade trade, Currency currency, LocalDate pricingDate,
			PricingParameter params) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> gcRepoPricerService.discountedPayments(trade, currency, pricingDate, params));
	}

	public BigDecimal getDelta(GCRepoTrade trade, Currency currency, LocalDate pricingDate, PricingParameter params)
			throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> gcRepoPricerService.getDelta(trade, currency, pricingDate, params));
	}

	public BigDecimal getPendingCollateralValue(GCRepoTrade trade, Map<Security, Map<Book, BigDecimal>> addedSecurities,
			Map<Security, Map<Book, BigDecimal>> removedSecurities) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> gcRepoPricerService.getPendingCollateralValue(trade, addedSecurities, removedSecurities));
	}

	public BigDecimal getCurrentCashValue(GCRepoTrade trade) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> gcRepoPricerService.getCurrentCashValue(trade));
	}

	public BigDecimal getApproximatedConvexity(GCRepoTrade trade, Currency currency, LocalDate pricingDate,
			PricingParameter params) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil
				.runEx(() -> gcRepoPricerService.getApproximatedConvexity(trade, currency, pricingDate, params));
	}
}