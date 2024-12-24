package org.eclipse.tradista.ir.irswapoption.surfacegenerationhandler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.marketdata.model.Quote;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.marketdata.model.SurfacePoint;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;
import org.eclipse.tradista.core.marketdata.surfacegenerationhandler.SurfaceGenerationHandler;
import org.eclipse.tradista.ir.irswapoption.model.IRSwapOptionTrade;

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

public class IRSurfaceGenerationHandler implements SurfaceGenerationHandler {

	@Override
	public List<SurfacePoint<Number, Number, Number>> buildSurfacePoints(List<Quote> quotes, LocalDate quoteDate,
			String instance, QuoteSet quoteSet) {
		List<SurfacePoint<Number, Number, Number>> surfacePoints = new ArrayList<SurfacePoint<Number, Number, Number>>();
		for (Quote quote : quotes) {
			String key = quote.getName().substring(IRSwapOptionTrade.IR_SWAP_OPTION.length() + 1);
			String[] prop = key.split("\\.");
			String frequency = prop[1];
			String swapMaturity = prop[2];
			String optionExpiry = prop[3];
			LocalDate maturityDate = null;
			LocalDate optionExpiryDate = null;

			// Volatilities are only quoted for the swap having a frequency of 6
			// months
			if (frequency.equals("6M")) {
				switch (swapMaturity) {
				case "1M": {
					maturityDate = quoteDate.plus(1, ChronoUnit.MONTHS);
					break;
				}
				case "3M": {
					maturityDate = quoteDate.plus(3, ChronoUnit.MONTHS);
					break;
				}
				case "6M": {
					maturityDate = quoteDate.plus(6, ChronoUnit.MONTHS);
					break;
				}
				case "1Y": {
					maturityDate = quoteDate.plus(1, ChronoUnit.YEARS);
					break;
				}
				}
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
				long maturityDecimal = DateUtil.difference(quoteDate, maturityDate);
				long optionExpiryDecimal = DateUtil.difference(quoteDate, optionExpiryDate);
				BigDecimal volatility = null;
				QuoteValue quoteValue = new QuoteBusinessDelegate().getQuoteValueByQuoteSetIdQuoteNameTypeAndDate(
						quoteSet.getId(), quote.getName(), quote.getType(), quoteDate);

				if (quoteValue != null) {
					volatility = getPrice(quoteValue, instance);
				}

				surfacePoints.add(
						new SurfacePoint<Number, Number, Number>(optionExpiryDecimal, maturityDecimal, volatility));

			}
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
	public List<SurfacePoint<Integer, BigDecimal, BigDecimal>> buildSurfacePoints2(List<Quote> quotes,
			LocalDate quoteDate, String instance, QuoteSet quoteSet) {
		// TODO Auto-generated method stub
		throw new RuntimeException("method Not used");
	}

}
