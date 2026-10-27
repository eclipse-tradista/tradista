package org.eclipse.tradista.core.common.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.user.model.User;

/********************************************************************************
 * Copyright (c) 2019 Olivier Asuncion
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
/**
 * Utility class holding the authenticated {@link User} and the currently
 * selected Processing Org ({@link LegalEntity}) for standalone remote EJB
 * clients.
 * <p>
 * <strong>Note:</strong> Because this class stores state in {@code static}
 * fields, it is intended exclusively for single-user standalone remote EJB
 * clients (such as the JavaFX desktop client) running in their own JVM. It must
 * <strong>not</strong> be used on the server side or in the multi-user web
 * application ({@code tradista-web}), where session-scoped state is managed by
 * {@code org.eclipse.tradista.core.user.ui.manager.UserManager}.
 * </p>
 */
public final class ClientUtil {

	private static User currentUser;

	/**
	 * The Processing Org currently selected by the admin user. When set, all
	 * relevant UI components filter their data accordingly.
	 */
	private static LegalEntity currentProcessingOrg;

	private ClientUtil() {
	}

	/**
	 * Sets the currently authenticated user in the static client context.
	 * <p>
	 * Must only be called from standalone remote EJB clients.
	 * </p>
	 *
	 * @param user the authenticated {@link User}
	 */
	public static void setCurrentUser(User user) {
		currentUser = user;
	}

	/**
	 * Returns the currently authenticated user from the static client context.
	 * <p>
	 * Must only be called from standalone remote EJB clients.
	 * </p>
	 *
	 * @return the current {@link User}, or {@code null} if none is set
	 */
	public static User getCurrentUser() {
		return currentUser;
	}

	/**
	 * Checks whether the currently authenticated user is an administrator.
	 * <p>
	 * Must only be called from standalone remote EJB clients.
	 * </p>
	 *
	 * @return {@code true} if a current user is set and has administrator
	 *         privileges, {@code false} otherwise
	 */
	public static boolean isCurrentUserAdmin() {
		return currentUser != null && currentUser.isAdmin();
	}

	/**
	 * Checks whether the currently authenticated user is an administrator.
	 * <p>
	 * Must only be called from standalone remote EJB clients.
	 * </p>
	 *
	 * @return {@code true} if a current user is set and has administrator
	 *         privileges, {@code false} otherwise
	 * @deprecated As of version 3.3.0, replaced by {@link #isCurrentUserAdmin()} to
	 *             comply with Java naming conventions.
	 */
	@Deprecated(since = "3.3.0", forRemoval = true)
	public static boolean currentUserIsAdmin() {
		return isCurrentUserAdmin();
	}

	/**
	 * Returns the Processing Org currently selected by an administrator in the
	 * static client context.
	 * <p>
	 * Must only be called from standalone remote EJB clients.
	 * </p>
	 *
	 * @return the currently selected Processing Org ({@link LegalEntity}), or
	 *         {@code null} if none is selected
	 */
	public static LegalEntity getCurrentProcessingOrg() {
		return currentProcessingOrg;
	}

	/**
	 * Sets the Processing Org currently selected by an administrator in the static
	 * client context.
	 * <p>
	 * Must only be called from standalone remote EJB clients.
	 * </p>
	 *
	 * @param processingOrg the selected Processing Org ({@link LegalEntity})
	 */
	public static void setCurrentProcessingOrg(LegalEntity processingOrg) {
		currentProcessingOrg = processingOrg;
	}

	/**
	 * Resolves the {@link ZoneId} of the current user's Processing Org, falling
	 * back to the currently selected Processing Org's time zone or the system
	 * default time zone.
	 * <p>
	 * Must only be called from standalone remote EJB clients.
	 * </p>
	 *
	 * @return the resolved {@link ZoneId} (never {@code null})
	 */
	public static ZoneId getCurrentUserZoneId() {
		User user = getCurrentUser();
		if (user != null && user.getProcessingOrg() != null && user.getProcessingOrg().getTimeZone() != null) {
			return user.getProcessingOrg().getTimeZone();
		}
		if (currentProcessingOrg != null && currentProcessingOrg.getTimeZone() != null) {
			return currentProcessingOrg.getTimeZone();
		}
		return ZoneId.systemDefault();
	}

	/**
	 * Converts the given {@link Instant} to a {@link ZonedDateTime} in the current
	 * user's time zone.
	 * <p>
	 * Must only be called from standalone remote EJB clients.
	 * </p>
	 *
	 * @param instant the instant to convert
	 * @return the corresponding {@link ZonedDateTime}, or {@code null} if
	 *         {@code instant} is {@code null}
	 */
	public static ZonedDateTime getCurrentUserTime(Instant instant) {
		if (instant == null) {
			return null;
		}
		return ZonedDateTime.ofInstant(instant, getCurrentUserZoneId());
	}
}