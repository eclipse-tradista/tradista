package org.eclipse.tradista.core.currency.model;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;

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

public class Currency extends TradistaObject {

	private static final long serialVersionUID = 5174185101451156191L;

	@Id
	private String isoCode;

	private String name;

	private boolean nonDeliverable;

	private int fixingDateOffset;

	private Calendar calendar;

	public Currency(String isoCode) {
		this.isoCode = isoCode;
	}

	public Calendar getCalendar() {
		return TradistaModelUtil.clone(calendar);
	}

	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}

	public boolean isNonDeliverable() {
		return nonDeliverable;
	}

	public void setNonDeliverable(boolean nonDeliverable) {
		this.nonDeliverable = nonDeliverable;
	}

	public int getFixingDateOffset() {
		return fixingDateOffset;
	}

	public void setFixingDateOffset(int fixingDateOffset) {
		this.fixingDateOffset = fixingDateOffset;
	}

	public String getIsoCode() {
		return isoCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return isoCode;
	}

	@Override
	public Currency clone() {
		Currency currency = (Currency) super.clone();
		currency.calendar = TradistaModelUtil.clone(calendar);
		return currency;
	}

}