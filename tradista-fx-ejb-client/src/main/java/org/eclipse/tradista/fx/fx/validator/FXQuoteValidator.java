package org.eclipse.tradista.fx.fx.validator;

import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.service.CurrencyBusinessDelegate;
import org.eclipse.tradista.core.marketdata.validator.DefaultQuoteValidator;
import org.eclipse.tradista.fx.fx.model.FXTrade;

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

public class FXQuoteValidator extends DefaultQuoteValidator {

	@Override
	public void validateQuoteName(String quoteName) throws TradistaBusinessException {
		validateQuoteBasics(quoteName);
		StringBuilder errMsg = new StringBuilder();
		String[] data = quoteName.split("\\.");

		if (data.length < 3) {
			throw new TradistaBusinessException(String.format(
					"The quote name (%s) must be as follows: %s.CurrencyOne.CurrencyTwo%n", quoteName, FXTrade.FX));
		}

		if (!data[0].equals(FXTrade.FX)) {
			errMsg.append(String.format("The quote name (%s) must start with %s.%n", quoteName, FXTrade.FX));
		}
		Set<Currency> currencies = new CurrencyBusinessDelegate().getAllCurrencies();
		if (currencies != null && !currencies.isEmpty()) {
			Currency curr = new Currency(data[1]);
			if (!currencies.contains(curr)) {
				errMsg.append(
						String.format("The currency one (%s) must exist in the system: %s%n.", data[1], currencies));
			}
			curr = new Currency(data[2]);
			if (!currencies.contains(curr)) {
				errMsg.append(
						String.format("The currency two (%s) must exist in the system: %s%n.", data[2], currencies));
			}
		} else {
			errMsg.append(String.format("The currencies (%s and %s) must exist in the system. %n.", data[1], data[2]));
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}
