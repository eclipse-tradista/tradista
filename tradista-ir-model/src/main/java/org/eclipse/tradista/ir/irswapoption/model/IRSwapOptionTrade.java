package org.eclipse.tradista.ir.irswapoption.model;

import java.math.BigDecimal;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.ir.irswap.model.SingleCurrencyIRSwapTrade;

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

public class IRSwapOptionTrade extends VanillaOptionTrade<SingleCurrencyIRSwapTrade> {

	private static final long serialVersionUID = 8952352869490542697L;

	public static final String IR_SWAP_OPTION = "IRSwapOption";

	private BigDecimal cashSettlementAmount;

	private Index alternativeCashSettlementReferenceRateIndex;

	private Tenor alternativeCashSettlementReferenceRateIndexTenor;

	public BigDecimal getCashSettlementAmount() {
		return cashSettlementAmount;
	}

	public void setCashSettlementAmount(BigDecimal cashSettlementAmount) {
		this.cashSettlementAmount = cashSettlementAmount;
	}

	public Index getAlternativeCashSettlementReferenceRateIndex() {
		return TradistaModelUtil.clone(alternativeCashSettlementReferenceRateIndex);
	}

	public void setAlternativeCashSettlementReferenceRateIndex(Index alternativeCashSettlementReferenceRateIndex) {
		this.alternativeCashSettlementReferenceRateIndex = alternativeCashSettlementReferenceRateIndex;
	}

	public Tenor getAlternativeCashSettlementReferenceRateIndexTenor() {
		return alternativeCashSettlementReferenceRateIndexTenor;
	}

	public void setAlternativeCashSettlementReferenceRateIndexTenor(
			Tenor alternativeCashSettlementReferenceRateIndexTenor) {
		this.alternativeCashSettlementReferenceRateIndexTenor = alternativeCashSettlementReferenceRateIndexTenor;
	}

	@Override
	public String getProductType() {
		return IR_SWAP_OPTION;
	}

	@Override
	public IRSwapOptionTrade clone() {
		IRSwapOptionTrade irSwapOptionTrade = (IRSwapOptionTrade) super.clone();
		irSwapOptionTrade.alternativeCashSettlementReferenceRateIndex = TradistaModelUtil
				.clone(alternativeCashSettlementReferenceRateIndex);
		return irSwapOptionTrade;
	}

}