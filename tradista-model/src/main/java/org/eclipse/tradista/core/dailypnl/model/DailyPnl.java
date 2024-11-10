package org.eclipse.tradista.core.dailypnl.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.position.model.PositionDefinition;

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

public class DailyPnl extends TradistaObject {

	private static final long serialVersionUID = 8562519011584539331L;

	@Id
	private PositionDefinition positionDefinition;

	@Id
	private Calendar calendar;

	@Id
	private LocalDate valueDate;

	private BigDecimal pnl;

	private BigDecimal realizedPnl;

	private BigDecimal unrealizedPnl;

	public DailyPnl(PositionDefinition positionDefinition, Calendar calendar, LocalDate valueDate) {
		super();
		this.positionDefinition = positionDefinition;
		this.calendar = calendar;
		this.valueDate = valueDate;
	}

	public PositionDefinition getPositionDefinition() {
		return (PositionDefinition) TradistaModelUtil.clone(positionDefinition);
	}

	public Calendar getCalendar() {
		return TradistaModelUtil.clone(calendar);
	}

	public LocalDate getValueDate() {
		return valueDate;
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

	@Override
	public DailyPnl clone() {
		DailyPnl dailyPnl = (DailyPnl) super.clone();
		dailyPnl.calendar = TradistaModelUtil.clone(calendar);
		dailyPnl.positionDefinition = TradistaModelUtil.clone(positionDefinition);
		return dailyPnl;
	}

}