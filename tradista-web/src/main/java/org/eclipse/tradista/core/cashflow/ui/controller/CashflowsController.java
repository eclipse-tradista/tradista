package org.eclipse.tradista.core.cashflow.ui.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.tradista.ai.analysis.service.CashflowsAnalysisBusinessDelegate;
import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.cashflow.model.CashFlow;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.service.PricerBusinessDelegate;
import org.eclipse.tradista.security.repo.model.RepoTrade;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

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

@Named
@ViewScoped
public class CashflowsController implements Serializable {

	private static final long serialVersionUID = 3525922991038977184L;

	private static final String CF_MSG = "cfMsg";

	private List<CashFlow> cashflows;

	private InterestRateCurve discountCurve;

	private PricerBusinessDelegate pricerBusinessDelegate;

	private CashflowsAnalysisBusinessDelegate cashflowsAnalysisBusinessDelegate;

	private String analysisResult;

	@PostConstruct
	public void init() {
		cashflows = Collections.synchronizedList(new ArrayList<CashFlow>());
		pricerBusinessDelegate = new PricerBusinessDelegate();
		cashflowsAnalysisBusinessDelegate = new CashflowsAnalysisBusinessDelegate();
	}

	public void setCashflows(List<CashFlow> cashflows) {
		this.cashflows = cashflows;
	}

	public List<CashFlow> getCashflows() {
		return cashflows;
	}

	public void generate(RepoTrade trade, PricingParameter pp, LocalDate pricingDate) {
		try {
			cashflows = pricerBusinessDelegate.generateCashFlows(trade, pp, pricingDate);
			analysisResult = null;
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			FacesContext.getCurrentInstance().addMessage(CF_MSG,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", te.getMessage()));
		}
	}

	public InterestRateCurve getDiscountCurve() {
		return discountCurve;
	}

	public void setDiscountCurve(InterestRateCurve discountCurve) {
		this.discountCurve = discountCurve;
	}

	public void updateDiscountCurve(PricingParameter pp, Currency currency) {
		if (pp != null && currency != null) {
			discountCurve = pp.getDiscountCurve(currency);
			if (discountCurve == null) {
				String errMsg = String.format(
						"Pricing Parameters Set '%s' doesn't contain a discount curve for currency %s.", pp, currency);
				FacesContext.getCurrentInstance().addMessage(CF_MSG,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", errMsg));
			}
		} else {
			discountCurve = null;
		}
	}

	public String getAnalysisResult() {
		return analysisResult;
	}

	public void setAnalysisResult(String analysisResult) {
		this.analysisResult = analysisResult;
	}

	public void analyseCashflows(RepoTrade trade, PricingParameter pp, LocalDate pricingDate) {
		try {
			if (!CollectionUtils.isEmpty(cashflows)) {
				Calendar currencyCalendar = trade.getCurrency().getCalendar();
				LocalDate tMinusOneDate = DateUtil.previousBusinessDay(pricingDate, currencyCalendar);
				List<CashFlow> tMinusOneCashflows = pricerBusinessDelegate.generateCashFlows(trade, pp, tMinusOneDate,
						true);
				analysisResult = cashflowsAnalysisBusinessDelegate.analyseCashflows(tMinusOneCashflows, cashflows);
			} else {
				analysisResult = "No cashflows generated.";
			}
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			FacesContext.getCurrentInstance().validationFailed();
			FacesContext.getCurrentInstance().addMessage(CF_MSG,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", te.getMessage()));
		}
	}

}