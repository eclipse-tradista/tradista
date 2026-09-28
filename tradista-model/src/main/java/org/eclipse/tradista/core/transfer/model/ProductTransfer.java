package org.eclipse.tradista.core.transfer.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.Trade;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
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

public class ProductTransfer extends Transfer {

	private static final long serialVersionUID = 3528142875953447004L;

	/**
	 * @deprecated use {@link Builder} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public ProductTransfer(Book book, TransferPurpose purpose, LocalDate settlementDate, Trade<?> trade) {
		super(book, null, purpose, settlementDate, trade);
	}

	/**
	 * @deprecated use {@link Builder} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public ProductTransfer(Book book, Product product, TransferPurpose purpose, LocalDate settlementDate,
			Trade<?> trade) {
		super(book, product, purpose, settlementDate, trade);
	}

	protected ProductTransfer(Builder builder) {
		super(builder);
	}

	public BigDecimal getQuantity() {
		return quantityOrAmount;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantityOrAmount = quantity;
	}

	@Override
	public Type getType() {
		return Transfer.Type.PRODUCT;
	}

	@Override
	public Builder toBuilder() {
		Builder builder = new Builder(getBook(), getProduct(), getPurpose(), getSettlementDate());
		builder.id(getId()).status(getStatus()).direction(getDirection()).quantityOrAmount(getQuantity())
				.trade(getTrade()).fixingDateTime(getFixingDateTime()).creationTime(getCreationTime())
				.lastUpdateTime(getLastUpdateTime());
		return builder;
	}

	public static class Builder extends Transfer.Builder<ProductTransfer, Builder> {

		public Builder(Book book, Product product, TransferPurpose purpose, LocalDate settlementDate) {
			this.book = book;
			this.product = product;
			this.purpose = purpose;
			this.settlementDate = settlementDate;
		}

		public Builder(Book book, TransferPurpose purpose, LocalDate settlementDate) {
			this.book = book;
			this.purpose = purpose;
			this.settlementDate = settlementDate;
		}

		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public ProductTransfer build() {
			return new ProductTransfer(this);
		}
	}

}