package org.eclipse.tradista.core.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.user.model.User;
import org.junit.jupiter.api.AfterEach;
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

class ClientUtilTest {

	@BeforeEach
	@AfterEach
	void resetClientUtil() {
		ClientUtil.setCurrentUser(null);
		ClientUtil.setCurrentProcessingOrg(null);
	}

	@Test
	void testCurrentUserIsAdminWhenNoUserIsSet() {
		assertFalse(ClientUtil.currentUserIsAdmin());
	}

	@Test
	void testCurrentUserIsAdminForAdminAndRegularUser() {
		User admin = new User("Admin", "User", null);
		ClientUtil.setCurrentUser(admin);
		assertTrue(ClientUtil.currentUserIsAdmin());

		LegalEntity po = new LegalEntity("PO_1");
		po.setRole(LegalEntity.Role.PROCESSING_ORG);
		User regularUser = new User("Regular", "User", po);
		ClientUtil.setCurrentUser(regularUser);
		assertFalse(ClientUtil.currentUserIsAdmin());
	}

	@Test
	void testGetCurrentUserZoneIdAndTime() {
		assertEquals(ZoneId.systemDefault(), ClientUtil.getCurrentUserZoneId());
		assertNull(ClientUtil.getCurrentUserTime(null));

		LegalEntity po = new LegalEntity("PO_NY");
		po.setRole(LegalEntity.Role.PROCESSING_ORG);
		po.setTimeZone(ZoneId.of("America/New_York"));

		User admin = new User("Admin", "User", null);
		ClientUtil.setCurrentUser(admin);
		ClientUtil.setCurrentProcessingOrg(po);

		assertEquals(ZoneId.of("America/New_York"), ClientUtil.getCurrentUserZoneId());

		Instant instant = Instant.parse("2026-06-15T12:00:00Z");
		assertEquals(ZonedDateTime.ofInstant(instant, ZoneId.of("America/New_York")),
				ClientUtil.getCurrentUserTime(instant));
	}

}