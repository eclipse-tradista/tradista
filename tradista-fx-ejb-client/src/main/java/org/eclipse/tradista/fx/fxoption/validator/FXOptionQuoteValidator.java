package org.eclipse.tradista.fx.fxoption.validator;

import java.math.BigDecimal;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.service.CurrencyBusinessDelegate;
import org.eclipse.tradista.core.marketdata.validator.DefaultQuoteValidator;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.fx.fxoption.model.FXOptionTrade;
import org.eclipse.tradista.fx.fxoption.service.FXVolatilitySurfaceBusinessDelegate;

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

public class FXOptionQuoteValidator extends DefaultQuoteValidator {

	@Override
	public void validateQuoteName(String quoteName) throws TradistaBusinessException {
		validateQuoteBasics(quoteName);
		StringBuilder errMsg = new StringBuilder();
		String[] data = quoteName.split("\\.");

		if (data.length < 5) {
			throw new TradistaBusinessException(String.format(
					"The quote name (%s) must be as follows: FXOption.Currency.optionExpiry.strike.CALL(or PUT)%n",
					quoteName));
		}

		if (!data[0].equals(FXOptionTrade.FX_OPTION)) {
			errMsg.append(
					String.format("The quote name (%s) must start with %s.%n", quoteName, FXOptionTrade.FX_OPTION));
		}
		Set<Currency> currencies = new CurrencyBusinessDelegate().getAllCurrencies();
		if (currencies != null && !currencies.isEmpty()) {
			Currency curr = new Currency(data[1]);
			if (!currencies.contains(curr)) {
				errMsg.append(String.format("The currency (%s) must exist in the system: %s%n.", data[1], currencies));
			}
		} else {
			errMsg.append(String.format("The currency (%s) must exist in the system. %n.", data[1]));
		}
		Set<String> expiries = new FXVolatilitySurfaceBusinessDelegate().getAllOptionExpiriesAsString();
		if (!expiries.contains(data[2])) {
			errMsg.append(String.format("The option expiry (%s) must be a valid one: %s%n.", data[2], expiries));
		}
		try {
			new BigDecimal(data[3]);
		} catch (NumberFormatException nfe) {
			errMsg.append(String.format("The strike (%s) must be a valid one.%n", data[3]));
		}
		if (!data[4].equals(OptionTrade.Type.CALL.toString()) && !data[4].equals(OptionTrade.Type.PUT.toString())) {
			errMsg.append(String.format("The option type (%s) must be %s or %s.%n", data[4], OptionTrade.Type.CALL,
					OptionTrade.Type.PUT));
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}
