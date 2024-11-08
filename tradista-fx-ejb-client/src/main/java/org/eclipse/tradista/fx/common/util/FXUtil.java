package org.eclipse.tradista.fx.common.util;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.book.model.BlankBook;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.model.CurrencyPair;
import org.eclipse.tradista.core.legalentity.model.BlankLegalEntity;
import org.eclipse.tradista.core.marketdata.model.FXCurve;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.fx.fx.model.FXTrade;
import org.eclipse.tradista.fx.fx.service.FXPricerBusinessDelegate;

/********************************************************************************
 * Copyright (c) 2021 Olivier Asuncion
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

public final class FXUtil {

	/**
	 * Gets the NPV of a FX Spot/FX Fwd for one unit of each currency. This method
	 * uses a synthetic trade and then calls the NPV pricer measure ("NPV discounted
	 * legs diff" algorithm).
	 * 
	 * @param primaryCurrency the primary currency
	 * @param quoteCurrency   the quote currency
	 * @param valueCurrency   the value currency (currency of the returned npv)
	 * @param date            the value date
	 * @param pp              the pricing parameters
	 * @return the NPV of a FX Spot/FX Fwd for one unit of each currency.
	 * @throws TradistaBusinessException
	 */
	public static BigDecimal getNPV(Currency primaryCurrency, Currency quoteCurrency, Currency valueCurrency,
			LocalDate date, PricingParameter pp) throws TradistaBusinessException {
		FXTrade trade = new FXTrade();
		trade.setCurrency(quoteCurrency);
		trade.setCurrencyOne(primaryCurrency);
		trade.setTradeDate(LocalDate.now());
		trade.setSettlementDate(date);
		trade.setBook(BlankBook.getInstance());
		trade.setCounterparty(BlankLegalEntity.getInstance());
		// Calculate amounts equal to 1 unit of the mandate currency
		CurrencyPair pair = new CurrencyPair(valueCurrency, quoteCurrency);
		FXCurve ppQuoteFXCurve = pp.getFxCurves().get(pair);
		if (ppQuoteFXCurve == null) {
			// TODO add log warn
		}
		pair = new CurrencyPair(valueCurrency, primaryCurrency);
		FXCurve ppPrimaryFXCurve = pp.getFxCurves().get(pair);
		if (ppPrimaryFXCurve == null) {
			// TODO add log warn
		}
		BigDecimal convertedQuoteCurrency = PricerUtil.convertAmount(BigDecimal.ONE, quoteCurrency, valueCurrency,
				LocalDate.now(), pp.getQuoteSet().getId(), ppQuoteFXCurve != null ? ppQuoteFXCurve.getId() : 0);
		BigDecimal convertedPrimaryCurrency = PricerUtil.convertAmount(BigDecimal.ONE, primaryCurrency, valueCurrency,
				LocalDate.now(), pp.getQuoteSet().getId(), ppPrimaryFXCurve != null ? ppPrimaryFXCurve.getId() : 0);
		trade.setAmount(convertedQuoteCurrency);
		trade.setAmountOne(convertedPrimaryCurrency);

		return new FXPricerBusinessDelegate().npvDiscountedLegsDiff(pp, trade, valueCurrency, LocalDate.now());
	}

}