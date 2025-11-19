package org.eclipse.tradista.security.gcrepo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.security.common.model.Security;
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;
import org.eclipse.tradista.security.repo.pricer.RepoPricerUtil;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
@Interceptors(GCRepoProductScopeFilteringInterceptor.class)
public class GCRepoPricerServiceBean implements GCRepoPricerService {

	@Override
	public BigDecimal getCollateralMarketToMarket(GCRepoTrade trade, Currency currency, LocalDate pricingDate,
			PricingParameter params) throws TradistaBusinessException {
		return RepoPricerUtil.getCollateralMarketToMarket(trade, currency, pricingDate, params);
	}

	@Override
	public BigDecimal getCurrentCollateralMarketToMarket(GCRepoTrade trade) throws TradistaBusinessException {
		return RepoPricerUtil.getCurrentCollateralMarketToMarket(trade);
	}

	@Override
	public BigDecimal getCollateralMarketToMarket(Map<Security, Map<Book, BigDecimal>> securities, LegalEntity po,
			LocalDate pricingDate) throws TradistaBusinessException {
		return RepoPricerUtil.getCollateralMarketToMarket(securities, po, pricingDate);
	}

	@Override
	public BigDecimal getExposure(GCRepoTrade trade, Currency currency, LocalDate pricingDate, PricingParameter params)
			throws TradistaBusinessException {
		return RepoPricerUtil.getExposure(trade, currency, pricingDate, params);
	}

	@Override
	public BigDecimal getCurrentExposure(GCRepoTrade trade) throws TradistaBusinessException {
		return RepoPricerUtil.getCurrentExposure(trade);
	}

	@Override
	public BigDecimal getCurrentCollateralValue(GCRepoTrade trade) throws TradistaBusinessException {
		return RepoPricerUtil.getCurrentCollateralValue(trade);
	}

	@Override
	public List<CashFlow> generateCashFlows(PricingParameter params, GCRepoTrade trade, LocalDate pricingDate)
			throws TradistaBusinessException {
		return RepoPricerUtil.generateCashFlows(params, trade, pricingDate);
	}

	@Override
	public BigDecimal pnlDefault(GCRepoTrade trade, Currency currency, LocalDate pricingDate, PricingParameter params)
			throws TradistaBusinessException {
		return RepoPricerUtil.pnlDefault(trade, currency, pricingDate, params);
	}

	@Override
	public BigDecimal realizedPayments(GCRepoTrade trade, Currency currency, LocalDate pricingDate,
			PricingParameter params) throws TradistaBusinessException {
		return RepoPricerUtil.realizedPayments(trade, currency, pricingDate, params);
	}

	@Override
	public BigDecimal discountedPayments(GCRepoTrade trade, Currency currency, LocalDate pricingDate,
			PricingParameter params) throws TradistaBusinessException {
		return RepoPricerUtil.discountedPayments(trade, currency, pricingDate, params);
	}

	@Override
	public BigDecimal getDelta(GCRepoTrade trade, Currency currency, LocalDate pricingDate, PricingParameter params)
			throws TradistaBusinessException {
		return RepoPricerUtil.getDelta(trade, currency, pricingDate, params);
	}

	@Override
	public BigDecimal getPendingCollateralValue(GCRepoTrade trade, Map<Security, Map<Book, BigDecimal>> addedSecurities,
			Map<Security, Map<Book, BigDecimal>> removedSecurities) throws TradistaBusinessException {
		return RepoPricerUtil.getPendingCollateralValue(trade, addedSecurities, removedSecurities);
	}

	@Override
	public BigDecimal getCurrentCashValue(GCRepoTrade trade) throws TradistaBusinessException {
		return RepoPricerUtil.getCurrentCashValue(trade);
	}

	@Override
	public BigDecimal getApproximatedConvexity(GCRepoTrade trade, Currency currency, LocalDate pricingDate,
			PricingParameter params) throws TradistaBusinessException {
		return RepoPricerUtil.getApproximatedConvexity(trade, pricingDate, params);
	}

}