package org.eclipse.tradista.core.product.model;

import java.time.LocalDate;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.exchange.model.Exchange;

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

public abstract class Product extends TradistaObject {

	private static final long serialVersionUID = 518407081850145938L;
	private LocalDate creationDate;

	@Id
	private Exchange exchange;

	public Product(Exchange exchange) {
		super();
		this.exchange = exchange;
	}

	public Exchange getExchange() {
		return TradistaModelUtil.clone(exchange);
	}

	public LocalDate getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDate creationDate) {
		this.creationDate = creationDate;
	}

	public abstract String getProductType();

	@Override
	public Product clone() {
		Product product = (Product) super.clone();
		product.exchange = TradistaModelUtil.clone(exchange);
		return product;
	}

}