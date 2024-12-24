package org.eclipse.tradista.ir.ccyswap.model;

import java.math.BigDecimal;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.ir.irswap.model.IRSwapTrade;

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
 * License for the specific language governing permissions and limitations
 * under the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
/**
 * Class to represent Cross-Currency swaps. Note : notionalAmountTwo : the
 * notional received at the starting of the trade, and paid at the maturity of
 * the trade currencyTwo : the currency of the fixed leg (to pay)
 * 
 * @author OA
 *
 * @param <P>
 */
public class CcySwapTrade extends IRSwapTrade {

	private static final long serialVersionUID = -6188291608649255466L;

	public static final String CCY_SWAP = "CcySwap";

	private Currency currencyTwo;

	private BigDecimal notionalAmountTwo;

	public Currency getCurrencyTwo() {
		return TradistaModelUtil.clone(currencyTwo);
	}

	public void setCurrencyTwo(Currency currencyTwo) {
		this.currencyTwo = currencyTwo;
	}

	public BigDecimal getNotionalAmountTwo() {
		return notionalAmountTwo;
	}

	public void setNotionalAmountTwo(BigDecimal notionalAmountTwo) {
		this.notionalAmountTwo = notionalAmountTwo;
	}

	public String getProductType() {
		return CCY_SWAP;
	}

	@Override
	public CcySwapTrade clone() {
		CcySwapTrade ccySwapTrade = (CcySwapTrade) super.clone();
		ccySwapTrade.currencyTwo = TradistaModelUtil.clone(currencyTwo);
		return ccySwapTrade;
	}

}