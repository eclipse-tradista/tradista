package org.eclipse.tradista.core.product.model;

import java.time.LocalDate;
import java.time.ZoneId;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TimestampedObject;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
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

public abstract class Product extends TimestampedObject {

	private static final long serialVersionUID = 518407081850145938L;

	@Id
	private Exchange exchange;

	/**
	 * @deprecated use {@link Builder} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	protected Product(Exchange exchange) {
		this.exchange = exchange;
	}

	protected Product(Builder<?, ?> builder) {
		super(builder);
		this.exchange = builder.exchange;
	}

	@Override
	public Builder<?, ?> toBuilder() {
		return null;
	}

	public Exchange getExchange() {
		return TradistaModelUtil.clone(exchange);
	}

	public void setExchange(Exchange exchange) {
		this.exchange = exchange;
	}

	/**
	 * @deprecated use {@link #getCreationTime()} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public LocalDate getCreationDate() {
		if (getCreationTime() == null) {
			return null;
		}
		return LocalDate.ofInstant(getCreationTime(), ZoneId.systemDefault());
	}

	/**
	 * @deprecated creation time is automatically managed and immutable. Use
	 *             {@link Builder#creationTime(java.time.Instant)} if needed during construction.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public void setCreationDate(LocalDate creationDate) {
		// No-op: creationTime is immutable and managed at construction/builder level.
	}


	public abstract String getProductType();

	@Override
	public Product clone() {
		Product product = (Product) super.clone();
		product.exchange = TradistaModelUtil.clone(exchange);
		return product;
	}

	public abstract static class Builder<T extends Product, B extends Builder<T, B>>
			extends TimestampedObject.Builder<T, B> {
		protected Exchange exchange;

		public B exchange(Exchange exchange) {
			this.exchange = exchange;
			return self();
		}
	}

}