package org.eclipse.tradista.fx.fxoption.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.fx.fx.model.FXTrade;

/********************************************************************************
 * Copyright (c) 2014 Olivier Asuncion
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

public class FXOptionTrade extends VanillaOptionTrade<FXTrade> {

	public static final String FX_OPTION = "FXOption";

	private static final long serialVersionUID = 8952352869490542697L;

	/**
	 * For FX Options, the strike is the following rate : Quote Amount / Primary
	 * Amount (the primary amount being the one to be bought).
	 */
	@Override
	public BigDecimal getStrike() {
		FXTrade underlying = getUnderlying();
		if (underlying != null && underlying.getAmount() != null && underlying.getAmountOne() != null) {
			return underlying.getAmount().divide(underlying.getAmountOne(), RoundingMode.HALF_EVEN);
		} else {
			return null;
		}
	}

	@Override
	public Exchange getExchange() {
		// Exchange is the one of FX
		return new FXTrade().getExchange();
	}

	@Override
	public String getProductType() {
		return FX_OPTION;
	}

}