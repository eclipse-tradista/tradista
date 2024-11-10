package org.eclipse.tradista.core.pricing.ui.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.TradistaGUIUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.service.CurrencyBusinessDelegate;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;
import org.eclipse.tradista.core.pricing.pricer.Pricer;
import org.eclipse.tradista.core.pricing.pricer.PricerMeasure;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.service.PricerBusinessDelegate;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.workflow.service.WorkflowBusinessDelegate;

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
public class PricingController implements Serializable {

	private static final long serialVersionUID = 1681707647567789611L;

	protected static final String PRICING_MSG = "pricingMsg";

	private Pricer pricer;

	private PricingParameter pricingParameter;

	private Set<PricingParameter> allPricingParameters;

	private PricerMeasure pricerMeasure;

	private List<PricerMeasure> allPricerMeasures;

	private QuoteSet quoteSet;

	private LocalDate pricingDate;

	private Set<QuoteSet> allQuoteSets;

	private PricerBusinessDelegate pricerBusinessDelegate;

	private QuoteBusinessDelegate quoteBusinessDelegate;

	private CurrencyBusinessDelegate currencyBusinessDelegate;

	private WorkflowBusinessDelegate workflowBusinessDelegate;

	private String pricingMethod;

	private List<String> allPricingMethods;

	private Currency pricingCurrency;

	private Set<Currency> allCurrencies;

	private String pricerResult;

	@PostConstruct
	public void init() {
		pricerBusinessDelegate = new PricerBusinessDelegate();
		quoteBusinessDelegate = new QuoteBusinessDelegate();
		currencyBusinessDelegate = new CurrencyBusinessDelegate();
		workflowBusinessDelegate = new WorkflowBusinessDelegate();
		allPricingParameters = pricerBusinessDelegate.getAllPricingParameters();
		allQuoteSets = quoteBusinessDelegate.getAllQuoteSets();
		allCurrencies = currencyBusinessDelegate.getAllCurrencies();
		pricingDate = LocalDate.now();
		if (allPricingParameters != null && !allPricingParameters.isEmpty()) {
			pricingParameter = allPricingParameters.stream().findFirst().get();
		}
	}

	public Pricer getPricer() {
		return pricer;
	}

	public void setPricer(Pricer pricer) {
		pricer = this.pricer;
	}

	public PricingParameter getPricingParameter() {
		return pricingParameter;
	}

	public void setPricingParameter(PricingParameter pricingParameter) {
		this.pricingParameter = pricingParameter;
	}

	public Set<PricingParameter> getAllPricingParameters() {
		return allPricingParameters;
	}

	public void setAllPricingParameters(Set<PricingParameter> allPricingParameters) {
		this.allPricingParameters = allPricingParameters;
	}

	public PricerMeasure getPricerMeasure() {
		return pricerMeasure;
	}

	public void setPricerMeasure(PricerMeasure pricerMeasure) {
		this.pricerMeasure = pricerMeasure;
	}

	public List<PricerMeasure> getAllPricerMeasures() {
		return allPricerMeasures;
	}

	public void setAllPricerMeasures(List<PricerMeasure> allPricerMeasures) {
		this.allPricerMeasures = allPricerMeasures;
	}

	public String getPricingMethod() {
		return pricingMethod;
	}

	public void setPricingMethod(String pricingMethod) {
		this.pricingMethod = pricingMethod;
	}

	public List<String> getAllPricingMethods() {
		return allPricingMethods;
	}

	public void setAllPricingMethods(List<String> allPricingMethods) {
		this.allPricingMethods = allPricingMethods;
	}

	public QuoteSet getQuoteSet() {
		return quoteSet;
	}

	public void setQuoteSet(QuoteSet quoteSet) {
		this.quoteSet = quoteSet;
	}

	public LocalDate getPricingDate() {
		return pricingDate;
	}

	public void setPricingDate(LocalDate pricingDate) {
		this.pricingDate = pricingDate;
	}

	public Set<QuoteSet> getAllQuoteSets() {
		return allQuoteSets;
	}

	public void setAllQuoteSets(Set<QuoteSet> allQuoteSets) {
		this.allQuoteSets = allQuoteSets;
	}

	public Currency getPricingCurrency() {
		return pricingCurrency;
	}

	public void setPricingCurrency(Currency pricingCurrency) {
		this.pricingCurrency = pricingCurrency;
	}

	public Set<Currency> getAllCurrencies() {
		return allCurrencies;
	}

	public void setAllCurrencies(Set<Currency> allCurrencies) {
		this.allCurrencies = allCurrencies;
	}

	public String getPricerResult() {
		return pricerResult;
	}

	public void setPricerResult(String pricerResult) {
		this.pricerResult = pricerResult;
	}

	public void price(Trade<?> trade) {
		try {
			Trade<?> tradeToBePriced = trade.clone();
			if (tradeToBePriced.getId() == 0) {
				tradeToBePriced.setStatus(workflowBusinessDelegate.getInitialStatus(tradeToBePriced.getWorkflow()));

			}
			pricerResult = TradistaGUIUtil.formatAmount(pricerBusinessDelegate.calculate(tradeToBePriced,
					pricingParameter, pricingCurrency, pricingDate, pricerMeasure, pricingMethod));
		} catch (TradistaBusinessException tbe) {
			String[] msgs = tbe.getMessage().split(System.lineSeparator());
			for (String msg : msgs) {
				FacesContext.getCurrentInstance().addMessage(PRICING_MSG,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", msg));
			}
		}
	}

	public void updatePricerMeasures() {
		if (pricer == null) {
			allPricerMeasures = null;
		} else {
			allPricerMeasures = pricer.getPricerMeasures();
		}
	}

	public void updatePricingMethods() {
		if (pricerMeasure == null) {
			allPricingMethods = null;
		} else {
			allPricingMethods = pricerBusinessDelegate.getAllPricingMethods(pricerMeasure);
		}
	}

	public void updatePricer(String productType) {
		if (pricingParameter == null) {
			pricer = null;
		} else {
			try {
				pricer = pricerBusinessDelegate.getPricer(productType, pricingParameter);
			} catch (TradistaBusinessException tbe) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", tbe.getMessage()));
			}
		}
	}

	public void initPricer(String productType) {
		if (pricer == null) {
			updatePricer(productType);
			updatePricerMeasures();
			if (allPricerMeasures != null && !allPricerMeasures.isEmpty()) {
				pricerMeasure = allPricerMeasures.stream().findFirst().get();
			}
			updatePricingMethods();
			if (allPricingMethods != null && !allPricingMethods.isEmpty()) {
				pricingMethod = allPricingMethods.stream().findFirst().get();
			}
		}
	}

}