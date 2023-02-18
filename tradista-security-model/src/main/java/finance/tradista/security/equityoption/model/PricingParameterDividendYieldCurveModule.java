package finance.tradista.security.equityoption.model;

import java.util.HashMap;
import java.util.Map;

import finance.tradista.core.common.model.TradistaModelUtil;
import finance.tradista.core.marketdata.model.InterestRateCurve;
import finance.tradista.core.pricing.pricer.PricingParameterModule;
import finance.tradista.security.equity.model.Equity;

/*
 * Copyright 2019 Olivier Asuncion
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.    */

public class PricingParameterDividendYieldCurveModule extends PricingParameterModule {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8492065287869688434L;

	private Map<Equity, InterestRateCurve> dividendYieldCurves;

	public PricingParameterDividendYieldCurveModule() {
		super();
		dividendYieldCurves = new HashMap<Equity, InterestRateCurve>();
	}

	@Override
	public String getProductFamily() {
		return "security";
	}

	@Override
	public String getProductType() {
		return EquityOption.EQUITY_OPTION;
	}

	@SuppressWarnings("unchecked")
	public Map<Equity, InterestRateCurve> getDividendYieldCurves() {
		return (Map<Equity, InterestRateCurve>) TradistaModelUtil.deepCopy(dividendYieldCurves);
	}

	public void setDividendYieldCurves(Map<Equity, InterestRateCurve> dividendYieldCurves) {
		this.dividendYieldCurves = dividendYieldCurves;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PricingParameterDividendYieldCurveModule clone() {
		PricingParameterDividendYieldCurveModule pricingParameterDividendYieldCurveModule = (PricingParameterDividendYieldCurveModule) super.clone();
		pricingParameterDividendYieldCurveModule.dividendYieldCurves = (Map<Equity, InterestRateCurve>) TradistaModelUtil.deepCopy(dividendYieldCurves);
		return pricingParameterDividendYieldCurveModule;
	}

}