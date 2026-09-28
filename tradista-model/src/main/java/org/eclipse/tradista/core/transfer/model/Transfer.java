package org.eclipse.tradista.core.transfer.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.Segregable;
import org.eclipse.tradista.core.common.model.TimestampedObject;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
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

public abstract class Transfer extends TimestampedObject implements Segregable {

	private static final long serialVersionUID = 3471863038215096341L;

	public enum Status {
		UNKNOWN, KNOWN, POTENTIAL, CANCELED;

		@Override
		public String toString() {
			switch (this) {
			case UNKNOWN:
				return "Unknown";
			case KNOWN:
				return "Known";
			case CANCELED:
				return "Canceled";
			case POTENTIAL:
				return "Potential";
			}
			return super.toString();
		}

		/**
		 * Gets a Status from a display name. Display names are used in GUIs. A display
		 * name of a Status is the result of its toString() method.
		 * 
		 * @param type
		 * @return
		 */
		public static Status getStatus(String displayName) {
			switch (displayName) {
			case "Unknown":
				return UNKNOWN;
			case "Known":
				return KNOWN;
			case "Canceled":
				return CANCELED;
			case "Potential":
				return POTENTIAL;
			}
			return null;
		}
	}

	public enum Type {
		CASH, PRODUCT;

		@Override
		public String toString() {
			switch (this) {
			case CASH:
				return "Cash";
			case PRODUCT:
				return "Product";
			}
			return super.toString();
		}

		/**
		 * Gets a Type from a display name. Display names are used in GUIs. A display
		 * name of a Type is the result of its toString() method.
		 * 
		 * @param type
		 * @return
		 */
		public static Type getType(String displayName) {
			switch (displayName) {
			case "Cash":
				return CASH;
			case "Product":
				return PRODUCT;
			}
			return null;
		}
	}

	public enum Direction {
		PAY, RECEIVE;

		@Override
		public String toString() {
			switch (this) {
			case PAY:
				return "Pay";
			case RECEIVE:
				return "Receive";
			}
			return super.toString();
		}

		/**
		 * Gets a Direction from a display name. Display names are used in GUIs. A
		 * display name of a Direction is the result of its toString() method.
		 * 
		 * @param type
		 * @return
		 */
		public static Direction getDirection(String displayName) {
			switch (displayName) {
			case "Pay":
				return PAY;
			case "Receive":
				return RECEIVE;
			}
			return null;
		}
	};

	private Status status;

	@Id
	private TransferPurpose purpose;

	private Direction direction;

	protected BigDecimal quantityOrAmount;

	@Id
	private Trade<?> trade;

	private LocalDateTime fixingDateTime;

	@Id
	private LocalDate settlementDate;

	@Id
	private Product product;

	@Id
	private Book book;

	/**
	 * @deprecated use {@link Builder} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	protected Transfer(Book book, Product product, TransferPurpose purpose, LocalDate settlementDate, Trade<?> trade) {
		this.book = book;
		this.product = product;
		this.purpose = purpose;
		this.settlementDate = settlementDate;
		this.trade = trade;
		if (product == null) {
			if (trade != null) {
				this.product = trade.getProduct();
			}
		}
	}

	protected Transfer(Builder<?, ?> builder) {
		super(builder);
		this.status = builder.status;
		this.purpose = builder.purpose;
		this.direction = builder.direction;
		this.quantityOrAmount = builder.quantityOrAmount;
		this.trade = builder.trade;
		this.fixingDateTime = builder.fixingDateTime;
		this.settlementDate = builder.settlementDate;
		this.product = builder.product;
		this.book = builder.book;
		if (this.product == null && this.trade != null) {
			this.product = this.trade.getProduct();
		}
	}

	public abstract Builder<?, ?> toBuilder();

	public Product getProduct() {
		return TradistaModelUtil.clone(product);
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public abstract Type getType();

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public Trade<?> getTrade() {
		return TradistaModelUtil.clone(trade);
	}

	public LocalDateTime getFixingDateTime() {
		return fixingDateTime;
	}

	public void setFixingDateTime(LocalDateTime fixingDateTime) {
		this.fixingDateTime = fixingDateTime;
	}

	/**
	 * @deprecated use {@link #getCreationTime()} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public LocalDateTime getCreationDateTime() {
		if (getCreationTime() == null) {
			return null;
		}
		return LocalDateTime.ofInstant(getCreationTime(), ZoneId.systemDefault());
	}

	/**
	 * @deprecated creation time is automatically managed. Use
	 *             {@link Builder#creationTime(java.time.Instant)} if needed during construction.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public void setCreationDateTime(LocalDateTime creationDateTime) {
		if (creationDateTime == null) {
			setCreationTime(null);
		} else {
			setCreationTime(creationDateTime.atZone(ZoneId.systemDefault()).toInstant());
		}
	}

	public LocalDate getSettlementDate() {
		return settlementDate;
	}

	public TransferPurpose getPurpose() {
		return purpose;
	}

	public Book getBook() {
		return TradistaModelUtil.clone(book);
	}

	@Override
	public LegalEntity getProcessingOrg() {
		return book != null ? book.getProcessingOrg() : null;
	}

	@Override
	public Transfer clone() {
		Transfer transfer = (Transfer) super.clone();
		transfer.trade = TradistaModelUtil.clone(trade);
		transfer.product = TradistaModelUtil.clone(product);
		transfer.book = TradistaModelUtil.clone(book);
		return transfer;
	}

	public boolean isCash() {
		return getType() == Type.CASH;
	}

	public boolean isProduct() {
		return getType() == Type.PRODUCT;
	}

	public abstract static class Builder<T extends Transfer, B extends Builder<T, B>>
			extends TimestampedObject.Builder<T, B> {
		protected Status status;
		protected TransferPurpose purpose;
		protected Direction direction;
		protected BigDecimal quantityOrAmount;
		protected Trade<?> trade;
		protected LocalDateTime fixingDateTime;
		protected LocalDate settlementDate;
		protected Product product;
		protected Book book;

		public B status(Status status) {
			this.status = status;
			return self();
		}

		public B purpose(TransferPurpose purpose) {
			this.purpose = purpose;
			return self();
		}

		public B direction(Direction direction) {
			this.direction = direction;
			return self();
		}

		public B quantityOrAmount(BigDecimal quantityOrAmount) {
			this.quantityOrAmount = quantityOrAmount;
			return self();
		}

		public B trade(Trade<?> trade) {
			this.trade = trade;
			return self();
		}

		public B fixingDateTime(LocalDateTime fixingDateTime) {
			this.fixingDateTime = fixingDateTime;
			return self();
		}

		public B settlementDate(LocalDate settlementDate) {
			this.settlementDate = settlementDate;
			return self();
		}

		public B product(Product product) {
			this.product = product;
			return self();
		}

		public B book(Book book) {
			this.book = book;
			return self();
		}
	}

}