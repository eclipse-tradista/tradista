/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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
package org.eclipse.tradista.core.rating.model;

import java.time.LocalDate;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaObject;

public class RatingAssignment extends TradistaObject {

	private static final long serialVersionUID = 1L;

	@Id
	private Ratable ratable;

	@Id
	private Rating rating;

	@Id
	private LocalDate validFrom;

	private LocalDate validTo;

	public RatingAssignment(Ratable ratable, Rating rating, LocalDate validFrom) {
		this.ratable = ratable;
		this.rating = rating;
		this.validFrom = validFrom;
	}

	public Ratable getRatable() {
		return ratable;
	}

	public Rating getRating() {
		return rating;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public void setValidTo(LocalDate validTo) {
		this.validTo = validTo;
	}
}