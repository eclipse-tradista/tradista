package org.eclipse.tradista.core.marketdata.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;

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

public abstract class VolatilitySurface<X extends Number, Y extends Number, Z extends Number> extends TradistaObject
		implements MarketData, Generable {

	private static final long serialVersionUID = -8759394476672552715L;

	protected List<SurfacePoint<X, Y, Z>> points;

	private String name;

	private LegalEntity processingOrg;

	private String algorithm;

	private String instance;

	private String interpolator;

	private LocalDate quoteDate;

	private List<Quote> quotes;

	private QuoteSet quoteSet;

	public VolatilitySurface(String name, LegalEntity processingOrg) {
		this.name = name;
		this.processingOrg = processingOrg;
		points = new ArrayList<>();
	}

	public QuoteSet getQuoteSet() {
		return TradistaModelUtil.clone(quoteSet);
	}

	public void setQuoteSet(QuoteSet quoteSet) {
		this.quoteSet = quoteSet;
	}

	@SuppressWarnings("unchecked")
	public List<Quote> getQuotes() {
		return (List<Quote>) TradistaModelUtil.deepCopy(quotes);
	}

	public void setQuotes(List<Quote> quotes) {
		this.quotes = quotes;
	}

	public String getName() {
		return name;
	}

	public LegalEntity getProcessingOrg() {
		return TradistaModelUtil.clone(processingOrg);
	}

	@Override
	public String getAlgorithm() {
		return algorithm;
	}

	@Override
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	@Override
	public boolean isGenerated() {
		return algorithm != null;
	}

	@Override
	public String getInstance() {
		return instance;
	}

	@Override
	public void setInstance(String instance) {
		this.instance = instance;
	}

	@Override
	public String getInterpolator() {
		return interpolator;
	}

	@Override
	public void setInterpolator(String interpolator) {
		this.interpolator = interpolator;
	}

	@Override
	public LocalDate getQuoteDate() {
		return quoteDate;
	}

	@Override
	public void setQuoteDate(LocalDate quoteDate) {
		this.quoteDate = quoteDate;
	}

	public List<SurfacePoint<X, Y, Z>> getPoints() {
		if (points == null) {
			return null;
		}
		return new ArrayList<>(points);
	}

	public void setPoints(List<SurfacePoint<X, Y, Z>> points) {
		this.points = points;
	}

	@SuppressWarnings("unchecked")
	@Override
	public VolatilitySurface<X, Y, Z> clone() {
		VolatilitySurface<X, Y, Z> volatilitySurface = (VolatilitySurface<X, Y, Z>) super.clone();
		volatilitySurface.processingOrg = TradistaModelUtil.clone(processingOrg);
		volatilitySurface.quoteSet = TradistaModelUtil.clone(quoteSet);
		if (points != null) {
			volatilitySurface.points = new ArrayList<>(points);
		}
		return volatilitySurface;
	}

	@Override
	public String toString() {
		return name;
	}

}