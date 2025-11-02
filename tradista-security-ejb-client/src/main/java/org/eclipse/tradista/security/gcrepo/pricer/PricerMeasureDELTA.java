package org.eclipse.tradista.security.gcrepo.pricer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.pricer.Pricing;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;

/********************************************************************************
 * Copyright (c) 2024 Olivier Asuncion
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

public class PricerMeasureDELTA extends PricerMeasureGCRepo {

	private static final long serialVersionUID = -6413668046655237178L;

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		init();
	}

	public String toString() {
		return "DELTA";
	}

	@Pricing
	public BigDecimal delta(PricingParameter params, GCRepoTrade trade, Currency currency, LocalDate pricingDate)
			throws TradistaBusinessException {
		return gcRepoPricerBusinessDelegate.getDelta(trade, currency, pricingDate, params);
	}
}