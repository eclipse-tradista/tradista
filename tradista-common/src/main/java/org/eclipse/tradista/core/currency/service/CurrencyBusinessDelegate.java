package org.eclipse.tradista.core.currency.service;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.service.CurrencyService;

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

public class CurrencyBusinessDelegate {

	private CurrencyService currencyService;

	public CurrencyBusinessDelegate() {
		currencyService = TradistaServiceLocator.getInstance().getCurrencyService();
	}

	public Set<Currency> getAllCurrencies() {
		return SecurityUtil.run(() -> currencyService.getAllCurrencies());
	}

	public Set<Currency> getDeliverableCurrencies() {
		return SecurityUtil.run(() -> currencyService.getDeliverableCurrencies());
	}

	public Set<Currency> getNonDeliverableCurrencies() {
		return SecurityUtil.run(() -> currencyService.getNonDeliverableCurrencies());
	}

	public long saveCurrency(Currency currency) throws TradistaBusinessException {
		if (currency == null) {
			throw new TradistaBusinessException("The currency cannot be null.");
		}
		if (StringUtils.isBlank(currency.getIsoCode())) {
			throw new TradistaBusinessException("The ISO code cannot be empty.");
		} else if (currency.getIsoCode().length() > 3) {
			throw new TradistaBusinessException("The ISO code cannot contain more than 3 characters.");
		}
		// Fixing date offset cannot be positive: we never fix rates after the
		// settlement dates!
		if (currency.getFixingDateOffset() > 0) {
			throw new TradistaBusinessException("The Fixing date offset must be negative or equals to 0.");
		}
		return SecurityUtil.runEx(() -> currencyService.saveCurrency(currency));
	}

	public Currency getCurrencyByIsoCode(String isoCode) throws TradistaBusinessException {
		if (StringUtils.isBlank(isoCode)) {
			throw new TradistaBusinessException("The ISO code cannot be empty.");
		}
		return SecurityUtil.run(() -> currencyService.getCurrencyByIsoCode(isoCode));
	}

	public Currency getCurrencyById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The id must be positive.");
		}
		return SecurityUtil.run(() -> currencyService.getCurrencyById(id));
	}
}
