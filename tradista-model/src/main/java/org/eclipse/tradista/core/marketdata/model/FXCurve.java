package org.eclipse.tradista.core.marketdata.model;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;

/********************************************************************************
 * Copyright (c) 2016 Olivier Asuncion
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

public class FXCurve extends GenerableCurve {

	private static final long serialVersionUID = -6365357379109869222L;

	public static final String FX_CURVE = "FXCurve";

	private Currency primaryCurrency;

	private Currency quoteCurrency;

	private InterestRateCurve primaryCurrencyIRCurve;

	private InterestRateCurve quoteCurrencyIRCurve;

	public FXCurve(String name, LegalEntity po) {
		super(name, po);
	}

	public Currency getPrimaryCurrency() {
		return TradistaModelUtil.clone(primaryCurrency);
	}

	public void setPrimaryCurrency(Currency primaryCurrency) {
		this.primaryCurrency = primaryCurrency;
	}

	public Currency getQuoteCurrency() {
		return TradistaModelUtil.clone(quoteCurrency);
	}

	public void setQuoteCurrency(Currency quoteCurrency) {
		this.quoteCurrency = quoteCurrency;
	}

	public InterestRateCurve getPrimaryCurrencyIRCurve() {
		return TradistaModelUtil.clone(primaryCurrencyIRCurve);
	}

	public void setPrimaryCurrencyIRCurve(InterestRateCurve primaryCurrencyIRCurve) {
		this.primaryCurrencyIRCurve = primaryCurrencyIRCurve;
	}

	public InterestRateCurve getQuoteCurrencyIRCurve() {
		return TradistaModelUtil.clone(quoteCurrencyIRCurve);
	}

	public void setQuoteCurrencyIRCurve(InterestRateCurve quoteCurrencyIRCurve) {
		this.quoteCurrencyIRCurve = quoteCurrencyIRCurve;
	}

	public String toString() {
		return this.getName();
	}

	@Override
	public FXCurve clone() {
		FXCurve curve = (FXCurve) super.clone();
		curve.primaryCurrency = TradistaModelUtil.clone(primaryCurrency);
		curve.quoteCurrency = TradistaModelUtil.clone(quoteCurrency);
		curve.primaryCurrencyIRCurve = TradistaModelUtil.clone(primaryCurrencyIRCurve);
		curve.quoteCurrencyIRCurve = TradistaModelUtil.clone(quoteCurrencyIRCurve);
		return curve;
	}

}