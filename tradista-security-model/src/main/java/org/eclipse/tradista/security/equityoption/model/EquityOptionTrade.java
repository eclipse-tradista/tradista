package org.eclipse.tradista.security.equityoption.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.security.equity.model.EquityTrade;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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

public class EquityOptionTrade extends VanillaOptionTrade<EquityTrade> {

	private static final long serialVersionUID = -5389991593803505087L;

	private BigDecimal quantity;

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public EquityOption getEquityOption() {
		return (EquityOption) super.getProduct();
	}

	public void setEquityOption(EquityOption product) {
		super.setProduct(product);
	}

	@Override
	public final void setProduct(Product product) {
		throw new UnsupportedOperationException("Please use setEquityOption instead.");
	}

	@Override
	public String getProductType() {
		return EquityOption.EQUITY_OPTION;
	}

	@Override
	public void setType(OptionTrade.Type type) {
		if (getEquityOption() == null) {
			super.setType(type);
		}
	}

	@Override
	public void setStrike(BigDecimal strike) {
		if (getEquityOption() == null) {
			super.setStrike(strike);
		}
	}

	@Override
	public void setMaturityDate(LocalDate maturityDate) {
		if (getEquityOption() == null) {
			super.setMaturityDate(maturityDate);
		}
	}

	@Override
	public OptionTrade.Type getType() {
		if (getEquityOption() != null) {
			return getEquityOption().getType();
		}
		return super.getType();
	}

	@Override
	public LocalDate getMaturityDate() {
		if (getEquityOption() != null) {
			return getEquityOption().getMaturityDate();
		}
		return super.getMaturityDate();
	}

	@Override
	public BigDecimal getStrike() {
		if (getEquityOption() != null) {
			return getEquityOption().getStrike();
		}
		return super.getStrike();
	}

	@Override
	public boolean isCall() {
		if (getEquityOption() != null) {
			return getEquityOption().getType().equals(OptionTrade.Type.CALL);
		}
		return super.isCall();
	}

}