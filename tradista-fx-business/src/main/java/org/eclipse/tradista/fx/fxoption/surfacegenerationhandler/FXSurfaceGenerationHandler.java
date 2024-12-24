package org.eclipse.tradista.fx.fxoption.surfacegenerationhandler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.marketdata.model.Quote;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.marketdata.model.SurfacePoint;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;
import org.eclipse.tradista.core.marketdata.surfacegenerationhandler.SurfaceGenerationHandler;
import org.eclipse.tradista.fx.fxoption.model.FXOptionTrade;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
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

public class FXSurfaceGenerationHandler implements SurfaceGenerationHandler {

	/**
	 * FXOption quotes : FXOption.CurrencyPair.OptionExpiry.Strike.CALL/PUT
	 */
	@Override
	public List<SurfacePoint<Integer, BigDecimal, BigDecimal>> buildSurfacePoints2(List<Quote> quotes,
			LocalDate quoteDate, String instance, QuoteSet quoteSet) {
		List<SurfacePoint<Integer, BigDecimal, BigDecimal>> surfacePoints = new ArrayList<SurfacePoint<Integer, BigDecimal, BigDecimal>>();
		for (Quote quote : quotes) {
			String key = quote.getName().substring(FXOptionTrade.FX_OPTION.length() + 1);
			String[] prop = key.split("\\.");
			String optionExpiry = prop[1];
			LocalDate optionExpiryDate = null;

			switch (optionExpiry) {
			case "3M": {
				optionExpiryDate = quoteDate.plus(3, ChronoUnit.MONTHS);
				break;
			}
			case "6M": {
				optionExpiryDate = quoteDate.plus(6, ChronoUnit.MONTHS);
				break;
			}
			case "1Y": {
				optionExpiryDate = quoteDate.plus(1, ChronoUnit.YEARS);
				break;
			}
			}

			int optionExpiryDecimal = DateUtil.difference(quoteDate, optionExpiryDate);
			BigDecimal volatility = null;
			BigDecimal delta = null;
			QuoteBusinessDelegate quoteBusinessDelegate = new QuoteBusinessDelegate();
			List<QuoteValue> quoteValues = quoteBusinessDelegate
					.getQuoteValuesByQuoteSetIdQuoteNameAndDate(quoteSet.getId(), quote.getName(), quoteDate);
			for (QuoteValue quoteValue : quoteValues) {
				if (quoteValue.getQuote().getType().equals(QuoteType.VOLATILITY)) {
					volatility = getPrice(quoteValue, instance);
				}
				if (quoteValue.getQuote().getType().equals(QuoteType.DELTA)) {
					delta = getPrice(quoteValue, instance);
				}
				if (volatility != null && delta != null) {
					break;
				}
			}
			surfacePoints
					.add(new SurfacePoint<Integer, BigDecimal, BigDecimal>(optionExpiryDecimal, delta, volatility));

		}

		return surfacePoints;
	}

	private BigDecimal getPrice(QuoteValue quoteValue, String instance) {
		switch (instance) {
		case "ASK":
			return quoteValue.getAsk();
		case "BID":
			return quoteValue.getBid();
		case "MID":
			return (quoteValue.getAsk().add(quoteValue.getBid())).divide(BigDecimal.valueOf(2));
		case "CLOSE":
			return quoteValue.getClose();
		case "OPEN":
			return quoteValue.getOpen();
		}
		return null;
	}

	@Override
	public List<SurfacePoint<Number, Number, Number>> buildSurfacePoints(List<Quote> quotes, LocalDate quoteDate,
			String instance, QuoteSet quoteSet) {
		throw new UnsupportedOperationException("Method not used.");
	}

}
