package org.eclipse.tradista.security.repo.ui.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/********************************************************************************
 * Copyright (c) 2024 Olivier Asuncion
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

public class PartialTermination implements Serializable {

	private static final long serialVersionUID = -5613097439320509323L;

	private BigDecimal reduction;

	private LocalDate date;

	public PartialTermination(LocalDate date, BigDecimal reduction) {
		this.date = date;
		this.reduction = reduction;
	}

	public BigDecimal getReduction() {
		return reduction;
	}

	public void setReduction(BigDecimal reduction) {
		this.reduction = reduction;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}
}