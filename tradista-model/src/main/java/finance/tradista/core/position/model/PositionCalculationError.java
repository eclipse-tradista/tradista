package org.eclipse.tradista.core.position.model;

import java.time.LocalDate;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.error.model.Error;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.Trade;

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

public class PositionCalculationError extends Error {

	private static final long serialVersionUID = -6672980501663576889L;

	private PositionDefinition positionDefinition;

	private LocalDate valueDate;

	private Trade<? extends Product> trade;

	private Product product;

	public static final String POSITION_CALCULATION = "PositionCalculation";

	public PositionCalculationError() {
		setType(POSITION_CALCULATION);
	}

	public PositionDefinition getPositionDefinition() {
		return TradistaModelUtil.clone(positionDefinition);
	}

	public void setPositionDefinition(PositionDefinition positionDefinition) {
		this.positionDefinition = positionDefinition;
	}

	public LocalDate getValueDate() {
		return valueDate;
	}

	public void setValueDate(LocalDate valueDate) {
		this.valueDate = valueDate;
	}

	public Trade<? extends Product> getTrade() {
		return TradistaModelUtil.clone(trade);
	}

	public void setTrade(Trade<? extends Product> trade) {
		this.trade = trade;
	}

	public Product getProduct() {
		return TradistaModelUtil.clone(product);
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Book getBook() {
		if (positionDefinition.getBook() != null) {
			return positionDefinition.getBook();
		}
		if (trade != null) {
			return trade.getBook();
		}
		return null;
	}

	@Override
	public String getSubjectKey() {
		return getType() + "-" + getPositionDefinition() + "-" + getValueDate();
	}

	@Override
	public PositionCalculationError clone() {
		PositionCalculationError positionCalculationError = (PositionCalculationError) super.clone();
		positionCalculationError.positionDefinition = TradistaModelUtil.clone(positionDefinition);
		positionCalculationError.trade = TradistaModelUtil.clone(trade);
		positionCalculationError.product = TradistaModelUtil.clone(product);
		return positionCalculationError;
	}
}