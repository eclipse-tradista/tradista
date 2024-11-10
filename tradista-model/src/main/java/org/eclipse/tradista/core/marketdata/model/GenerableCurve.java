package org.eclipse.tradista.core.marketdata.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;

/********************************************************************************
 * Copyright (c) 2020 Olivier Asuncion
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

public abstract class GenerableCurve extends Curve<LocalDate, BigDecimal> implements Generable {

	private static final long serialVersionUID = -984285608945441710L;

	public GenerableCurve(String name, LegalEntity po) {
		super(name, po);
		points = new TreeMap<LocalDate, BigDecimal>();
	}

	private String algorithm;

	private String interpolator;

	private String instance;

	private QuoteSet quoteSet;

	private List<Quote> quotes;

	private LocalDate quoteDate;

	public QuoteSet getQuoteSet() {
		return TradistaModelUtil.clone(quoteSet);
	}

	public void setQuoteSet(QuoteSet quoteSet) {
		this.quoteSet = quoteSet;
	}

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getInterpolator() {
		return interpolator;
	}

	public void setInterpolator(String interpolator) {
		this.interpolator = interpolator;
	}

	public boolean isGenerated() {
		return (algorithm != null);
	}

	@SuppressWarnings("unchecked")
	public List<Quote> getQuotes() {
		return (List<Quote>) TradistaModelUtil.deepCopy(quotes);
	}

	public void setQuotes(List<Quote> quotes) {
		this.quotes = quotes;
	}

	public LocalDate getQuoteDate() {
		return quoteDate;
	}

	public void setQuoteDate(LocalDate quoteDate) {
		this.quoteDate = quoteDate;
	}

	@SuppressWarnings("unchecked")
	@Override
	public GenerableCurve clone() {
		GenerableCurve curve = (GenerableCurve) super.clone();
		curve.quoteSet = TradistaModelUtil.clone(quoteSet);
		curve.quotes = (List<Quote>) TradistaModelUtil.deepCopy(quotes);
		return curve;
	}

}