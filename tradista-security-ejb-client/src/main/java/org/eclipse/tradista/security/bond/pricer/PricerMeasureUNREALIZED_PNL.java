package org.eclipse.tradista.security.bond.pricer;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.exception.PricerException;
import org.eclipse.tradista.core.pricing.pricer.PricerMeasure;
import org.eclipse.tradista.core.pricing.pricer.Pricing;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.security.bond.model.Bond;
import org.eclipse.tradista.security.bond.service.BondPricerBusinessDelegate;

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

public class PricerMeasureUNREALIZED_PNL extends PricerMeasure {

	private static final long serialVersionUID = -6099839442804867432L;
	private BondPricerBusinessDelegate bondPricerBusinessDelegate;

	public PricerMeasureUNREALIZED_PNL() {
		super();
		bondPricerBusinessDelegate = new BondPricerBusinessDelegate();
	}

	@Pricing(defaultUNREALIZED_PNL = true)
	public BigDecimal unrealizedPnl(PricingParameter params, Bond product, Book book, Currency currency,
			LocalDate pricingDate) throws PricerException, TradistaBusinessException {

		return bondPricerBusinessDelegate.unrealizedPnlDefault(params, product, book, currency, pricingDate);
	}

	public String toString() {
		return "UNREALIZED_PNL";
	}

}