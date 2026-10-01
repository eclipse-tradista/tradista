package org.eclipse.tradista.ir.future.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.ir.future.model.Future;
import org.eclipse.tradista.ir.future.model.FutureContractSpecification;
import org.junit.jupiter.api.BeforeAll;
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

public class FutureValidatorTest {

	private static FutureValidator validator;
	private static FutureContractSpecification contractSpecification;
	private static Exchange exchange;

	@BeforeAll
	public static void setUp() {
		validator = new FutureValidator();
		exchange = new Exchange("EPA");
		contractSpecification = new FutureContractSpecification("EURIBOR_3M");
		contractSpecification.setExchange(exchange);
	}

	@Test
	public void testValidFuture() {
		Future future = new Future.Builder("MAR26", contractSpecification)
				.maturityDate(LocalDate.of(2026, 3, 20))
				.build();

		assertDoesNotThrow(() -> validator.validateProduct(future));
	}

	@Test
	public void testNullFuture() {
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(null));
	}

	@Test
	public void testMissingExchange() {
		FutureContractSpecification specWithoutExchange = new FutureContractSpecification("EURIBOR_3M");
		Future future = new Future.Builder("MAR26", specWithoutExchange)
				.maturityDate(LocalDate.of(2026, 3, 20))
				.build();

		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(future));
	}

	@Test
	public void testMissingContractSpecification() {
		Future future = new Future.Builder("MAR26", null)
				.exchange(exchange)
				.maturityDate(LocalDate.of(2026, 3, 20))
				.build();

		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(future));
	}

	@Test
	public void testMissingMaturityDate() {
		Future future = new Future.Builder("MAR26", contractSpecification)
				.maturityDate(null)
				.build();

		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(future));
	}

	@Test
	public void testNullSymbol() {
		Future future = new Future.Builder(null, contractSpecification)
				.maturityDate(LocalDate.of(2026, 3, 20))
				.build();

		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(future));
	}

	@Test
	public void testEmptySymbol() {
		Future future = new Future.Builder("", contractSpecification)
				.maturityDate(LocalDate.of(2026, 3, 20))
				.build();

		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(future));
	}

	@Test
	public void testInvalidSymbolLength() {
		Future future = new Future.Builder("MAR2026", contractSpecification)
				.maturityDate(LocalDate.of(2026, 3, 20))
				.build();

		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(future));
	}

	@Test
	public void testInvalidSymbolMonth() {
		Future future = new Future.Builder("XYZ26", contractSpecification)
				.maturityDate(LocalDate.of(2026, 3, 20))
				.build();

		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(future));
	}

	@Test
	public void testInvalidSymbolYear() {
		Future future = new Future.Builder("MARXX", contractSpecification)
				.maturityDate(LocalDate.of(2026, 3, 20))
				.build();

		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(future));
	}

}
