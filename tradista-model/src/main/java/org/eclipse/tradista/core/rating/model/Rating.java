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

import java.util.List;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;

public class Rating extends TradistaObject implements Comparable<Rating> {

	private static final long serialVersionUID = 1L;

	@Id
	private String code;

	@Id
	private RatingAgency agency;

	private String description;

	public Rating(String code, RatingAgency agency) {
		this.code = code;
		this.agency = agency;
	}

	public String getCode() {
		return code;
	}

	public RatingAgency getAgency() {
		return TradistaModelUtil.clone(agency);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return code;
	}

	@Override
	public int compareTo(Rating rating) {
		int agencyComp = agency.compareTo(rating.getAgency());
		if (agencyComp != 0) {
			return agencyComp;
		}
		return code.compareTo(rating.getCode());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Rating clone() {
		Rating rating = (Rating) super.clone();
		rating.agency = TradistaModelUtil.clone(agency);
		return rating;
	}
}