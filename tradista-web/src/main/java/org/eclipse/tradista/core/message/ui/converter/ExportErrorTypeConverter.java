package org.eclipse.tradista.core.message.ui.converter;

import java.io.Serializable;

import org.eclipse.tradista.core.message.model.ExportError.ExportErrorType;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

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

@FacesConverter("exportErrorTypeConverter")
public class ExportErrorTypeConverter implements Serializable, Converter<ExportErrorType> {

	private static final long serialVersionUID = 148663751152500494L;

	public ExportErrorTypeConverter() {
	}

	@Override
	public ExportErrorType getAsObject(FacesContext context, UIComponent component, String value) {
		return ExportErrorType.valueOf(value.toUpperCase());
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, ExportErrorType exportErrorType) {
		return exportErrorType.toString();
	}

}