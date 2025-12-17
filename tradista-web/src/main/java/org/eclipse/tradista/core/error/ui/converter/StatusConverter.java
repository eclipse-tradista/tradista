package org.eclipse.tradista.core.error.ui.converter;

import java.io.Serializable;

import org.eclipse.tradista.core.error.model.Error.Status;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/********************************************************************************
 * Copyright (c) 2025 Olivier Asuncion
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

@FacesConverter("statusConverter")
public class StatusConverter implements Serializable, Converter<Status> {

	private static final long serialVersionUID = 997442674372705101L;

	public StatusConverter() {
	}

	@Override
	public Status getAsObject(FacesContext context, UIComponent component, String value) {
		return Status.valueOf(value.toUpperCase());
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Status status) {
		return status.toString();
	}

}