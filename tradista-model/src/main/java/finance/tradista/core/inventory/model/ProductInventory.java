package org.eclipse.tradista.core.inventory.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.product.model.Product;

/********************************************************************************
 * Copyright (c) 2016 Olivier Asuncion
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

public class ProductInventory extends TradistaObject implements Comparable<ProductInventory> {

	private static final long serialVersionUID = -386706153298729738L;

	@Id
	private Product product;

	private BigDecimal quantity;

	private BigDecimal averagePrice;

	@Id
	private LocalDate from;

	private LocalDate to;

	@Id
	private Book book;

	public ProductInventory(Book book, LocalDate from, Product product) {
		super();
		this.book = book;
		this.from = from;
		this.product = product;
	}

	public Product getProduct() {
		return TradistaModelUtil.clone(product);
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public LocalDate getFrom() {
		return from;
	}

	public LocalDate getTo() {
		return to;
	}

	public void setTo(LocalDate to) {
		this.to = to;
	}

	public BigDecimal getAveragePrice() {
		return averagePrice;
	}

	public void setAveragePrice(BigDecimal averagePrice) {
		this.averagePrice = averagePrice;
	}

	public Book getBook() {
		return TradistaModelUtil.clone(book);
	}

	@Override
	public int compareTo(ProductInventory pi) {
		if (pi == null) {
			return 1;
		}
		int eq = getBook().toString().compareTo(pi.getBook().toString());
		if (eq == 0) {
			eq = getProduct().getProductType().toString().compareTo(pi.getProduct().getProductType().toString());
			if (eq == 0) {
				eq = (int) (getProduct().getId() - pi.getProduct().getId());
				if (eq == 0) {
					return getFrom().compareTo(pi.getFrom());
				} else {
					return eq;
				}
			}
		}
		return eq;
	}

	@Override
	public String toString() {
		return "Inventory [product=" + product + ", quantity=" + quantity + ", averagePrice=" + averagePrice + ", from="
				+ from + ", to=" + to + ", book=" + book + "]";
	}

	@Override
	public ProductInventory clone() {
		ProductInventory productInventory = (ProductInventory) super.clone();
		productInventory.book = TradistaModelUtil.clone(book);
		productInventory.product = TradistaModelUtil.clone(product);
		return productInventory;
	}

}