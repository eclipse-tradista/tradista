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
package org.eclipse.tradista.core.rating.ui.controller;

import java.io.Serializable;
import org.eclipse.tradista.core.rating.model.Rating;

public class RatingDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	private String code;
	private String description;
	private org.eclipse.tradista.core.rating.model.RatingAgency agency;

	public RatingDTO() {
	}

	public RatingDTO(Rating rating) {
		this.id = rating.getId();
		this.code = rating.getCode();
		this.description = rating.getDescription();
		this.agency = rating.getAgency();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public org.eclipse.tradista.core.rating.model.RatingAgency getAgency() {
		return agency;
	}

	public void setAgency(org.eclipse.tradista.core.rating.model.RatingAgency agency) {
		this.agency = agency;
	}
}
