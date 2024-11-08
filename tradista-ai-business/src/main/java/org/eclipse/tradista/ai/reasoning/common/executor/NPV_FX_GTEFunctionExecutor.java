package org.eclipse.tradista.ai.reasoning.common.executor;

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
 * License for the specific language governing permissions and limitations
 * under the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.ai.reasoning.common.model.Function;
import org.eclipse.tradista.ai.reasoning.common.model.FunctionExecutor;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
import org.eclipse.tradista.fx.common.util.FXUtil;

public class NPV_FX_GTEFunctionExecutor extends FunctionExecutor<Boolean> {

	@Override
	/**
	 * Returns the NPV of a FX on the given currencies and settlement date. Both
	 * amounts are calculated to be equivalent of one unit of the mandate currency
	 */
	public Boolean call(Function<?> function, Object... parameters) throws TradistaBusinessException {
		BigDecimal npv;
		Currency primaryCurrency = (Currency) parameters[0];
		Currency quoteCurrency = (Currency) parameters[1];
		Currency valueCurrency = (Currency) parameters[2];
		LocalDate date = (LocalDate) parameters[3];
		PricingParameter pp = (PricingParameter) parameters[4];
		BigDecimal threshold = (BigDecimal) parameters[5];
		npv = FXUtil.getNPV(primaryCurrency, quoteCurrency, valueCurrency, date, pp);
		return npv.compareTo(threshold) >= 0;
	}

}
