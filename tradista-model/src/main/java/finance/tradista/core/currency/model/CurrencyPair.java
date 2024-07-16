package finance.tradista.core.currency.model;

import finance.tradista.core.common.model.Id;
import finance.tradista.core.common.model.TradistaModelUtil;
import finance.tradista.core.common.model.TradistaObject;

/********************************************************************************
 * Copyright (c) 2019 Olivier Asuncion
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

public class CurrencyPair extends TradistaObject {

	private static final long serialVersionUID = -1429614201466990666L;

	@Id
	private Currency primaryCurrency;

	@Id
	private Currency quoteCurrency;

	public CurrencyPair(Currency primaryCurrency, Currency quoteCurrency) {
		this.primaryCurrency = primaryCurrency;
		this.quoteCurrency = quoteCurrency;
	}

	public Currency getPrimaryCurrency() {
		return TradistaModelUtil.clone(primaryCurrency);
	}

	public Currency getQuoteCurrency() {
		return TradistaModelUtil.clone(quoteCurrency);
	}

	@Override
	public CurrencyPair clone() {
		CurrencyPair currencyPair = (CurrencyPair) super.clone();
		currencyPair.primaryCurrency = TradistaModelUtil.clone(primaryCurrency);
		currencyPair.quoteCurrency = TradistaModelUtil.clone(quoteCurrency);
		return currencyPair;
	}

}