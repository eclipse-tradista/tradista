package org.eclipse.tradista.security.bond.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaObject;

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
 * Class used to represented Bond coupons. Used only when calculating coupons
 * for one specific bond, that's why a Coupon object is identified by its date.
 * Prefer usage of Cashflow class, this Coupon class may be deprecated in the
 * future.
 * 
 * @author OA
 *
 */
public class Coupon extends TradistaObject {

	private static final long serialVersionUID = 5271486827367738893L;

	@Id
	private LocalDate date;

	private BigDecimal amount;

	public Coupon(LocalDate date) {
		super();
		this.date = date;
	}

	public LocalDate getDate() {
		return date;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

}