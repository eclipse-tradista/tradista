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
package org.eclipse.tradista.core.rating.ui.converter;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.rating.model.RatingAgency;
import org.eclipse.tradista.core.rating.service.RatingBusinessDelegate;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

@FacesConverter("ratingAgencyConverter")
public class RatingAgencyConverter implements Converter<RatingAgency> {

	private RatingBusinessDelegate ratingBusinessDelegate;

	public RatingAgencyConverter() {
		ratingBusinessDelegate = new RatingBusinessDelegate();
	}

	@Override
	public RatingAgency getAsObject(FacesContext context, UIComponent component, String value) {
		if (value == null || value.trim().isEmpty() || value.equals("null")) {
			return null;
		}
		try {
			return ratingBusinessDelegate.getRatingAgencyByName(value);
		} catch (TradistaBusinessException e) {
			return null;
		}
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, RatingAgency value) {
		if (value == null) {
			return null;
		}
		return value.getName();
	}
}
