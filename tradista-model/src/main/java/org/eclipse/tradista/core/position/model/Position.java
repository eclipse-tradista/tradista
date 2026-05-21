package org.eclipse.tradista.core.position.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.eclipse.tradista.core.common.model.Segregable;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.product.model.ProductScoped;

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

public class Position extends TradistaObject implements Segregable, ProductScoped {

	private static final long serialVersionUID = -1061624313259067674L;

	private PositionDefinition positionDefinition;

	private LocalDateTime valueDateTime;

	private BigDecimal pnl;

	private BigDecimal realizedPnl;

	private BigDecimal unrealizedPnl;

	private BigDecimal quantity;

	private BigDecimal averagePrice;

	public PositionDefinition getPositionDefinition() {
		return TradistaModelUtil.clone(positionDefinition);
	}

	@Override
	public org.eclipse.tradista.core.legalentity.model.LegalEntity getProcessingOrg() {
		return positionDefinition != null ? positionDefinition.getProcessingOrg() : null;
	}

	public void setPositionDefinition(PositionDefinition positionDefinition) {
		this.positionDefinition = positionDefinition;
	}

	public LocalDateTime getValueDateTime() {
		return valueDateTime;
	}

	public void setValueDateTime(LocalDateTime valueDateTime) {
		this.valueDateTime = valueDateTime;
	}

	public BigDecimal getPnl() {
		return pnl;
	}

	public void setPnl(BigDecimal pnl) {
		this.pnl = pnl;
	}

	public BigDecimal getRealizedPnl() {
		return realizedPnl;
	}

	public void setRealizedPnl(BigDecimal realizedPnl) {
		this.realizedPnl = realizedPnl;
	}

	public BigDecimal getUnrealizedPnl() {
		return unrealizedPnl;
	}

	public void setUnrealizedPnl(BigDecimal unrealizedPnl) {
		this.unrealizedPnl = unrealizedPnl;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getAveragePrice() {
		return averagePrice;
	}

	public void setAveragePrice(BigDecimal averagePrice) {
		this.averagePrice = averagePrice;
	}

	@Override
	public String getProductType() {
		return positionDefinition != null ? positionDefinition.getProductType() : null;
	}

	@Override
	public Product getProduct() {
		return positionDefinition != null ? positionDefinition.getProduct() : null;
	}

	@Override
	public Position clone() {
		Position position = (Position) super.clone();
		position.positionDefinition = TradistaModelUtil.clone(positionDefinition);
		return position;
	}

}