package org.eclipse.tradista.fx.fxndf.model;

import java.math.BigDecimal;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.fx.common.model.AbstractFXTrade;

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

public class FXNDFTrade extends AbstractFXTrade<Product> {

	private static final long serialVersionUID = -4927177189578884165L;

	public static final String FX_NDF = "FXNDF";

	private Currency nonDeliverableCurrency;

	private BigDecimal ndfRate;

	public Currency getNonDeliverableCurrency() {
		return TradistaModelUtil.clone(nonDeliverableCurrency);
	}

	public void setNonDeliverableCurrency(Currency nonDeliverableCurrency) {
		this.nonDeliverableCurrency = nonDeliverableCurrency;
	}

	public BigDecimal getNdfRate() {
		return ndfRate;
	}

	public void setNdfRate(BigDecimal ndfRate) {
		this.ndfRate = ndfRate;
	}

	@Override
	public String getProductType() {
		return FX_NDF;
	}

	@Override
	public FXNDFTrade clone() {
		FXNDFTrade fxNdfTrade = (FXNDFTrade) super.clone();
		fxNdfTrade.nonDeliverableCurrency = TradistaModelUtil.clone(nonDeliverableCurrency);
		return fxNdfTrade;
	}

}