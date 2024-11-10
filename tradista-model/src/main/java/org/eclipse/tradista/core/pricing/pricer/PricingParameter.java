package org.eclipse.tradista.core.pricing.pricer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.model.CurrencyPair;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;

/********************************************************************************
 * Copyright (c) 2014 Olivier Asuncion
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

public class PricingParameter extends TradistaObject {

	private static final long serialVersionUID = -5219479844754106015L;

	public PricingParameter(String name, LegalEntity po) {
		super();
		params = new HashMap<String, String>();
		indexCurves = new HashMap<Index, InterestRateCurve>();
		discountCurves = new HashMap<Currency, InterestRateCurve>();
		fxCurves = new HashMap<CurrencyPair, FXCurve>();
		customPricers = new HashMap<String, String>();
		modules = new ArrayList<PricingParameterModule>();
		this.name = name;
		this.processingOrg = po;
	}

	public PricingParameter(String name, QuoteSet quoteSet, LegalEntity po) {
		this(name, po);
		this.quoteSet = quoteSet;
	}

	@Id
	private String name;

	private QuoteSet quoteSet;

	@Id
	private LegalEntity processingOrg;

	private List<PricingParameterModule> modules;

	private Map<Currency, InterestRateCurve> discountCurves;

	private Map<Index, InterestRateCurve> indexCurves;

	private Map<CurrencyPair, FXCurve> fxCurves;

	private Map<String, String> params;

	private Map<String, String> customPricers;

	public QuoteSet getQuoteSet() {
		return TradistaModelUtil.clone(quoteSet);
	}

	public void setQuoteSet(QuoteSet quoteSet) {
		this.quoteSet = quoteSet;
	}

	public String getName() {
		return name;
	}

	public Map<String, String> getParams() {
		if (params == null) {
			return null;
		}
		return new HashMap<String, String>(params);
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public LegalEntity getProcessingOrg() {
		return TradistaModelUtil.clone(processingOrg);
	}

	@SuppressWarnings("unchecked")
	public Map<Currency, InterestRateCurve> getDiscountCurves() {
		return (Map<Currency, InterestRateCurve>) TradistaModelUtil.deepCopy(discountCurves);
	}

	public void setDiscountCurves(Map<Currency, InterestRateCurve> discountCurves) {
		this.discountCurves = discountCurves;
	}

	@SuppressWarnings("unchecked")
	public Map<Index, InterestRateCurve> getIndexCurves() {
		return (Map<Index, InterestRateCurve>) TradistaModelUtil.deepCopy(indexCurves);
	}

	public void setIndexCurves(Map<Index, InterestRateCurve> indexCurves) {
		this.indexCurves = indexCurves;
	}

	@SuppressWarnings("unchecked")
	public Map<CurrencyPair, FXCurve> getFxCurves() {
		return (Map<CurrencyPair, FXCurve>) TradistaModelUtil.deepCopy(fxCurves);
	}

	public void setFxCurves(Map<CurrencyPair, FXCurve> fxCurves) {
		this.fxCurves = fxCurves;
	}

	public Map<String, String> getCustomPricers() {
		if (customPricers == null) {
			return null;
		}
		return new HashMap<String, String>(customPricers);
	}

	public void setCustomPricers(Map<String, String> customPricers) {
		this.customPricers = customPricers;
	}

	public String toString() {
		return name;
	}

	@SuppressWarnings("unchecked")
	public List<PricingParameterModule> getModules() {
		return (List<PricingParameterModule>) TradistaModelUtil.deepCopy(modules);
	}

	public void setModules(List<PricingParameterModule> modules) {
		this.modules = modules;
	}

	public InterestRateCurve getDiscountCurve(Currency currency) {
		if (currency == null) {
			return null;
		}
		return TradistaModelUtil.clone(discountCurves.get(currency));
	}

	@SuppressWarnings("unchecked")
	@Override
	public PricingParameter clone() {
		PricingParameter pricingParameter = (PricingParameter) super.clone();
		pricingParameter.quoteSet = TradistaModelUtil.clone(quoteSet);
		pricingParameter.processingOrg = TradistaModelUtil.clone(processingOrg);
		pricingParameter.modules = (List<PricingParameterModule>) TradistaModelUtil.deepCopy(modules);
		pricingParameter.discountCurves = (Map<Currency, InterestRateCurve>) TradistaModelUtil.deepCopy(discountCurves);
		pricingParameter.indexCurves = (Map<Index, InterestRateCurve>) TradistaModelUtil.deepCopy(indexCurves);
		pricingParameter.fxCurves = (Map<CurrencyPair, FXCurve>) TradistaModelUtil.deepCopy(fxCurves);
		if (params != null) {
			pricingParameter.params = new HashMap<String, String>(params);
		}
		if (customPricers != null) {
			pricingParameter.customPricers = new HashMap<String, String>(customPricers);
		}
		return pricingParameter;
	}

}