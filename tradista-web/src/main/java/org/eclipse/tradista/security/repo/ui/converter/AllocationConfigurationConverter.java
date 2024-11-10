package org.eclipse.tradista.security.repo.ui.converter;

import java.io.Serializable;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.security.repo.model.AllocationConfiguration;
import org.eclipse.tradista.security.repo.service.AllocationConfigurationBusinessDelegate;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

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

@FacesConverter("allocationConfigurationConverter")
public class AllocationConfigurationConverter implements Serializable, Converter<AllocationConfiguration> {

	private static final long serialVersionUID = -902705474150240949L;

	private AllocationConfigurationBusinessDelegate allocationConfigurationBusinessDelegate;

	public AllocationConfigurationConverter() {
		allocationConfigurationBusinessDelegate = new AllocationConfigurationBusinessDelegate();
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, AllocationConfiguration allocConfig) {
		return allocConfig.getName();
	}

	@Override
	public AllocationConfiguration getAsObject(FacesContext context, UIComponent component, String value) {
		try {
			return allocationConfigurationBusinessDelegate.getAllocationConfigurationByName(value);
		} catch (TradistaBusinessException tbe) {
			throw new ConverterException(String.format("Could not convert allocation configuration %s", value), tbe);
		}
	}

}