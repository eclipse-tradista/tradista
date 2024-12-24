package org.eclipse.tradista.security.equityoption.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;
import org.eclipse.tradista.core.pricing.pricer.PricingParameterModule;
import org.eclipse.tradista.security.equity.model.Equity;

/********************************************************************************
 * Copyright (c) 2019 Olivier Asuncion
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

public class PricingParameterDividendYieldCurveModule extends PricingParameterModule {

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
		pricingParameterDividendYieldCurveModule.dividendYieldCurves = (Map<Equity, InterestRateCurve>) TradistaModelUtil
				.deepCopy(dividendYieldCurves);
		return pricingParameterDividendYieldCurveModule;
	}

}