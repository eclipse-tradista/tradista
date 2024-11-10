package org.eclipse.tradista.core.position.model;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.pricing.pricer.PricingParameter;
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

public class PositionDefinition extends TradistaObject {

	private static final long serialVersionUID = -4204899247050005377L;

	@Id
	private String name;

	private Book book;

	private String productType;

	private Product product;

	private LegalEntity counterparty;

	private boolean isRealTime;

	private Currency currency;

	private PricingParameter pricingParameter;

	@Id
	private LegalEntity processingOrg;

	public PositionDefinition(String name, LegalEntity processingOrg) {
		super();
		this.name = name;
		this.processingOrg = processingOrg;
	}

	public String getName() {
		return name;
	}

	public Book getBook() {
		return TradistaModelUtil.clone(book);
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public Product getProduct() {
		return TradistaModelUtil.clone(product);
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public LegalEntity getCounterparty() {
		return TradistaModelUtil.clone(counterparty);
	}

	public void setCounterparty(LegalEntity counterparty) {
		this.counterparty = counterparty;
	}

	public boolean isRealTime() {
		return isRealTime;
	}

	public void setRealTime(boolean isRealTime) {
		this.isRealTime = isRealTime;
	}

	public Currency getCurrency() {
		return TradistaModelUtil.clone(currency);
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public PricingParameter getPricingParameter() {
		return TradistaModelUtil.clone(pricingParameter);
	}

	public void setPricingParameter(PricingParameter pricingParameter) {
		this.pricingParameter = pricingParameter;
	}

	public LegalEntity getProcessingOrg() {
		return TradistaModelUtil.clone(processingOrg);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public PositionDefinition clone() {
		PositionDefinition positionDefinition = (PositionDefinition) super.clone();
		positionDefinition.book = TradistaModelUtil.clone(book);
		positionDefinition.product = TradistaModelUtil.clone(product);
		positionDefinition.counterparty = TradistaModelUtil.clone(counterparty);
		positionDefinition.currency = TradistaModelUtil.clone(currency);
		positionDefinition.pricingParameter = TradistaModelUtil.clone(pricingParameter);
		positionDefinition.processingOrg = TradistaModelUtil.clone(processingOrg);
		return positionDefinition;
	}

}