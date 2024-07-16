package finance.tradista.security.equityoption.surfacegenerationhandler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import finance.tradista.core.common.util.DateUtil;
import finance.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import finance.tradista.core.marketdata.model.Quote;
import finance.tradista.core.marketdata.model.QuoteSet;
import finance.tradista.core.marketdata.model.QuoteType;
import finance.tradista.core.marketdata.model.QuoteValue;
import finance.tradista.core.marketdata.model.SurfacePoint;
import finance.tradista.core.marketdata.service.QuoteBusinessDelegate;
import finance.tradista.core.marketdata.surfacegenerationhandler.SurfaceGenerationHandler;
import finance.tradista.security.equityoption.model.EquityOption;

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

public class EquityOptionSurfaceGenerationHandler implements SurfaceGenerationHandler {

	private ConfigurationBusinessDelegate configurationBusinessDelegate;

	public EquityOptionSurfaceGenerationHandler() {
		configurationBusinessDelegate = new ConfigurationBusinessDelegate();
	}

	/**
	 * FXOption quotes : FXOption.CurrencyPair.OptionExpiry.Strike.CALL/PUT
	 */
	@Override
	public List<SurfacePoint<Integer, BigDecimal, BigDecimal>> buildSurfacePoints2(List<Quote> quotes,
			LocalDate quoteDate, String instance, QuoteSet quoteSet) {
		List<SurfacePoint<Integer, BigDecimal, BigDecimal>> surfacePoints = new ArrayList<SurfacePoint<Integer, BigDecimal, BigDecimal>>();
		for (Quote quote : quotes) {
			String key = quote.getName().substring(EquityOption.EQUITY_OPTION.length() + 1);
			String[] prop = key.split("\\.");
			String optionExpiry = prop[1];
			String strike = prop[2];
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
			BigDecimal price = null;
			List<QuoteValue> quoteValues = new QuoteBusinessDelegate()
					.getQuoteValuesByQuoteSetIdQuoteNameAndDate(quoteSet.getId(), quote.getName(), quoteDate);
			for (QuoteValue quoteValue : quoteValues) {
				if (quoteValue.getQuote().getType().equals(QuoteType.VOLATILITY)) {
					volatility = getPrice(quoteValue, instance);
				}
				if (quoteValue.getQuote().getType().equals(QuoteType.EQUITY_OPTION_PRICE)) {
					price = getPrice(quoteValue, instance);
				}
				if (volatility != null && price != null) {
					break;
				}
			}
			// In Equity Option surfaces, the Y axis is Strike/Spot price).
			surfacePoints.add(new SurfacePoint<Integer, BigDecimal, BigDecimal>(optionExpiryDecimal,
					new BigDecimal(strike).divide(price, configurationBusinessDelegate.getRoundingMode()), volatility));

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
