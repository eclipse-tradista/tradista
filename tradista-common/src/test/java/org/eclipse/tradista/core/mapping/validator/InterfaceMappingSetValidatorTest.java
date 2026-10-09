package org.eclipse.tradista.core.mapping.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.mapping.model.InterfaceMappingSet;
import org.eclipse.tradista.core.mapping.model.MappingType;
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

public class InterfaceMappingSetValidatorTest {

	private static final InterfaceMappingSetValidator validator = new InterfaceMappingSetValidator();

	private LegalEntity createProcessingOrg() {
		LegalEntity po = new LegalEntity("PO_TEST");
		po.setRole(LegalEntity.Role.PROCESSING_ORG);
		return po;
	}

	private InterfaceMappingSet createValidInterfaceMappingSet() {
		InterfaceMappingSet ims = new InterfaceMappingSet("TEST_INTERFACE", MappingType.Book,
				InterfaceMappingSet.Direction.INCOMING, createProcessingOrg());
		ims.addMapping("EXT_BOOK_1", "INT_BOOK_1");
		ims.addMapping("EXT_BOOK_2", "INT_BOOK_2");
		return ims;
	}

	@Test
	public void testValidInterfaceMappingSet() {
		InterfaceMappingSet ims = createValidInterfaceMappingSet();
		assertDoesNotThrow(() -> validator.validateInterfaceMappingSet(ims));
	}

	@Test
	public void testNullInterfaceMappingSet() {
		assertThrows(TradistaBusinessException.class, () -> validator.validateInterfaceMappingSet(null));
	}

	@Test
	public void testNullDirection() {
		InterfaceMappingSet ims = new InterfaceMappingSet("TEST_INTERFACE", MappingType.Book, null,
				createProcessingOrg());
		ims.addMapping("EXT_1", "INT_1");
		assertThrows(TradistaBusinessException.class, () -> validator.validateInterfaceMappingSet(ims));
	}

	@Test
	public void testNullProcessingOrg() {
		InterfaceMappingSet ims = new InterfaceMappingSet("TEST_INTERFACE", MappingType.Book,
				InterfaceMappingSet.Direction.INCOMING, null);
		ims.addMapping("EXT_1", "INT_1");
		assertThrows(TradistaBusinessException.class, () -> validator.validateInterfaceMappingSet(ims));
	}

	@Test
	public void testEmptyMappings() {
		InterfaceMappingSet ims = new InterfaceMappingSet("TEST_INTERFACE", MappingType.Book,
				InterfaceMappingSet.Direction.INCOMING, createProcessingOrg());
		// No mappings added
		assertThrows(TradistaBusinessException.class, () -> validator.validateInterfaceMappingSet(ims));

		ims.setMappings(null);
		assertThrows(TradistaBusinessException.class, () -> validator.validateInterfaceMappingSet(ims));

		ims.setMappings(new HashSet<>());
		assertThrows(TradistaBusinessException.class, () -> validator.validateInterfaceMappingSet(ims));
	}

	@Test
	public void testBlankMappingValue() {
		InterfaceMappingSet ims = createValidInterfaceMappingSet();
		Set<InterfaceMappingSet.Mapping> mappings = new HashSet<>();
		mappings.add(new InterfaceMappingSet.Mapping(ims, null, "INT_VAL"));
		ims.setMappings(mappings);
		assertThrows(TradistaBusinessException.class, () -> validator.validateInterfaceMappingSet(ims));

		mappings.clear();
		mappings.add(new InterfaceMappingSet.Mapping(ims, "   ", "INT_VAL"));
		ims.setMappings(mappings);
		assertThrows(TradistaBusinessException.class, () -> validator.validateInterfaceMappingSet(ims));
	}

	@Test
	public void testBlankMappedValue() {
		InterfaceMappingSet ims = createValidInterfaceMappingSet();
		Set<InterfaceMappingSet.Mapping> mappings = new HashSet<>();
		mappings.add(new InterfaceMappingSet.Mapping(ims, "EXT_VAL", null));
		ims.setMappings(mappings);
		assertThrows(TradistaBusinessException.class, () -> validator.validateInterfaceMappingSet(ims));

		mappings.clear();
		mappings.add(new InterfaceMappingSet.Mapping(ims, "EXT_VAL", "   "));
		ims.setMappings(mappings);
		assertThrows(TradistaBusinessException.class, () -> validator.validateInterfaceMappingSet(ims));
	}

}
