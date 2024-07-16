package finance.tradista.security.equity.pricer;

import java.math.BigDecimal;
import java.time.LocalDate;

import finance.tradista.core.book.model.Book;
import finance.tradista.core.common.exception.TradistaBusinessException;
import finance.tradista.core.currency.model.Currency;
import finance.tradista.core.pricing.exception.PricerException;
import finance.tradista.core.pricing.pricer.PricerMeasure;
import finance.tradista.core.pricing.pricer.Pricing;
import finance.tradista.core.pricing.pricer.PricingParameter;
import finance.tradista.security.equity.model.Equity;
import finance.tradista.security.equity.service.EquityPricerBusinessDelegate;

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

public class PricerMeasurePNL extends PricerMeasure {

	private static final long serialVersionUID = 6686801429056963228L;
	private EquityPricerBusinessDelegate equityPricerBusinessDelegate;

	public PricerMeasurePNL() {
		super();
		equityPricerBusinessDelegate = new EquityPricerBusinessDelegate();
	}

	@Pricing(defaultPNL = true)
	public BigDecimal defaultPNL(PricingParameter params, Equity product, Book book, Currency currency,
			LocalDate pricingDate) throws PricerException, TradistaBusinessException {

		return equityPricerBusinessDelegate.pnlDefault(params, product, book, currency, pricingDate);
	}

	public String toString() {
		return "PNL";
	}

}