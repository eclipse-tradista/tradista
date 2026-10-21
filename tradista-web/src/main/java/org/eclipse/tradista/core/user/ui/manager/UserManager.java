package org.eclipse.tradista.core.user.ui.manager;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.user.model.User;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

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

@Named
@SessionScoped
public class UserManager implements Serializable {

	private static final long serialVersionUID = -3434595366265551240L;

	private User currentUser;

	/**
	 * The Processing Org currently selected by the admin user. When set, all
	 * relevant UI components filter their data accordingly.
	 */
	private LegalEntity currentProcessingOrg;

	public User getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
		this.currentProcessingOrg = currentUser != null ? currentUser.getProcessingOrg() : null;
	}

	public LegalEntity getCurrentProcessingOrg() {
		return currentProcessingOrg;
	}

	public void setCurrentProcessingOrg(LegalEntity currentProcessingOrg) {
		this.currentProcessingOrg = currentProcessingOrg;
	}

	public String getCurrentUserFirstName() {
		return currentUser != null ? currentUser.getFirstName() : StringUtils.EMPTY;
	}

	public String getCurrentUserSurname() {
		return currentUser != null ? currentUser.getSurname() : StringUtils.EMPTY;
	}

	public ZoneId getCurrentUserZoneId() {
		if (currentProcessingOrg != null && currentProcessingOrg.getTimeZone() != null) {
			return currentProcessingOrg.getTimeZone();
		}
		return ZoneId.systemDefault();
	}

	public ZonedDateTime getUserTime(Instant instant) {
		if (instant == null) {
			return null;
		}
		return ZonedDateTime.ofInstant(instant, getCurrentUserZoneId());
	}

	public boolean isCurrentUserAdmin() {
		return currentUser != null && currentUser.isAdmin();
	}

}