package org.eclipse.tradista.core.marketdata.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaObject;

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

public class RatePoint extends TradistaObject implements Comparable<RatePoint> {

	private static final long serialVersionUID = -3013332431720131491L;

	private BigDecimal rate;

	@Id
	private LocalDate date;

	public RatePoint(LocalDate date, BigDecimal rate) {
		this.date = date;
		this.rate = rate;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public LocalDate getDate() {
		return date;
	}

	@Override
	public int compareTo(RatePoint p) {
		return date.compareTo((p).getDate());
	}

}