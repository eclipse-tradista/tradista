package org.eclipse.tradista.core.common.ui.converter;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.messaging.TradistaMessagingConfiguration;
import org.eclipse.tradista.core.user.ui.manager.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

@FacesConverter("instantConverter")
public class InstantConverter implements Converter<Instant> {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss z");

	private static final Logger logger = LoggerFactory.getLogger(InstantConverter.class);

	@Override
	public Instant getAsObject(FacesContext context, UIComponent component, String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return Instant.parse(value);
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Instant value) {
		if (value == null) {
			return StringUtils.EMPTY;
		}

		// 1. Retrieve the UserMAnager from the session via the Expression Language (EL)
		UserManager userManager = context.getApplication().evaluateExpressionGet(context, "#{userManager}",
				UserManager.class);

		if (userManager != null) {
			ZonedDateTime ldt = userManager.getUserTime(value);
			return ldt.format(FORMATTER);
		}

		logger.warn("User manager not found: a default display is used for this instant: {}", value);

		// Fallback in case the UserManager is not found
		return value.toString();
	}
}