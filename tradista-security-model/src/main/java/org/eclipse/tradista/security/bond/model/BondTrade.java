package org.eclipse.tradista.security.bond.model;

import java.math.BigDecimal;

import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.trade.model.Trade;

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
 * Class representing a trade on Bond. Amount represents the unit price of a
 * bond for this deal. *
 * 
 * @param <B> the traded bond.
 */
public class BondTrade extends Trade<Bond> {

	private static final long serialVersionUID = 198715914648744270L;

	private BigDecimal quantity;

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	@Override
	public Currency getCurrency() {
		if (getProduct() != null) {
			return getProduct().getCurrency();
		}
		return null;
	}

}