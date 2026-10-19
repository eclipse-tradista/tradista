package org.eclipse.tradista.core.user.ui.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

class UserManagerTest {

	private UserManager userManager;

	@BeforeEach
	void setUp() {
		userManager = new UserManager();
	}

	@Test
	void testInitialStateWhenNoUserLoggedIn() {
		assertNull(userManager.getCurrentUser());
		assertNull(userManager.getCurrentProcessingOrg());
		assertFalse(userManager.isCurrentUserAdmin());
		assertEquals(StringUtils.EMPTY, userManager.getCurrentUserFirstName());
		assertEquals(StringUtils.EMPTY, userManager.getCurrentUserSurname());
		assertEquals(ZoneId.systemDefault(), userManager.getCurrentUserZoneId());
		assertNull(userManager.getUserTime(null));
	}

	@Test
	void testRegularUserLoginInitializesCurrentProcessingOrg() {
		LegalEntity po = new LegalEntity("PO_PARIS");
		po.setRole(LegalEntity.Role.PROCESSING_ORG);
		po.setTimeZone(ZoneId.of("Europe/Paris"));
		User regularUser = new User("Jean", "Dupont", po);

		userManager.setCurrentUser(regularUser);

		assertEquals(regularUser, userManager.getCurrentUser());
		assertEquals(po, userManager.getCurrentProcessingOrg());
		assertFalse(userManager.isCurrentUserAdmin());
		assertEquals("Jean", userManager.getCurrentUserFirstName());
		assertEquals("Dupont", userManager.getCurrentUserSurname());
		assertEquals(ZoneId.of("Europe/Paris"), userManager.getCurrentUserZoneId());
	}

	@Test
	void testAdminUserLoginAndProcessingOrgSelection() {
		User adminUser = new User("Admin", "Root", null);

		userManager.setCurrentUser(adminUser);

		assertEquals(adminUser, userManager.getCurrentUser());
		assertNull(userManager.getCurrentProcessingOrg());
		assertTrue(userManager.isCurrentUserAdmin());
		assertEquals(ZoneId.systemDefault(), userManager.getCurrentUserZoneId());

		LegalEntity selectedPo = new LegalEntity("PO_TOKYO");
		selectedPo.setRole(LegalEntity.Role.PROCESSING_ORG);
		selectedPo.setTimeZone(ZoneId.of("Asia/Tokyo"));

		userManager.setCurrentProcessingOrg(selectedPo);

		assertEquals(selectedPo, userManager.getCurrentProcessingOrg());
		assertEquals(ZoneId.of("Asia/Tokyo"), userManager.getCurrentUserZoneId());

		Instant instant = Instant.parse("2026-06-15T12:00:00Z");
		ZonedDateTime userTime = userManager.getUserTime(instant);
		assertEquals(ZonedDateTime.ofInstant(instant, ZoneId.of("Asia/Tokyo")), userTime);
	}

	@Test
	void testSessionIsolationBetweenMultipleUserManagers() {
		UserManager sessionA = new UserManager();
		UserManager sessionB = new UserManager();

		LegalEntity poA = new LegalEntity("PO_A");
		poA.setRole(LegalEntity.Role.PROCESSING_ORG);
		User userA = new User("Alice", "Smith", poA);

		User adminB = new User("Bob", "Admin", null);
		LegalEntity poB = new LegalEntity("PO_B");
		poB.setRole(LegalEntity.Role.PROCESSING_ORG);

		sessionA.setCurrentUser(userA);
		sessionB.setCurrentUser(adminB);
		sessionB.setCurrentProcessingOrg(poB);

		assertEquals(userA, sessionA.getCurrentUser());
		assertEquals(poA, sessionA.getCurrentProcessingOrg());
		assertFalse(sessionA.isCurrentUserAdmin());

		assertEquals(adminB, sessionB.getCurrentUser());
		assertEquals(poB, sessionB.getCurrentProcessingOrg());
		assertTrue(sessionB.isCurrentUserAdmin());
	}

}