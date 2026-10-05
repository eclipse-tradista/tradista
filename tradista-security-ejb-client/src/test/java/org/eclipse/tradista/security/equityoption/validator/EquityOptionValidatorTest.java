package org.eclipse.tradista.security.equityoption.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.test.TradistaTestUtil;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equityoption.model.EquityOption;
import org.eclipse.tradista.security.equityoption.model.EquityOptionContractSpecification;
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

public class EquityOptionValidatorTest {

	private static final EquityOptionValidator validator = new EquityOptionValidator();
	private static final EquityOptionContractSpecification contractSpecification;
	private static final Equity underlying;

	static {
		Exchange exchange = TradistaTestUtil.createExchange("EPA");
		contractSpecification = new EquityOptionContractSpecification("SAN_OPTION");
		contractSpecification.setExchange(exchange);
		contractSpecification.setStyle(VanillaOptionTrade.Style.AMERICAN);
		underlying = Equity.of(exchange, "FR0000120271");
	}

	private EquityOption.Builder createValidEquityOptionBuilder() {
		return EquityOption.builder("SAN_C_100", OptionTrade.Type.CALL, BigDecimal.valueOf(100),
				LocalDate.of(2026, 6, 19), contractSpecification).underlying(underlying);
	}

	@Test
	public void testValidEquityOption() {
		EquityOption option = createValidEquityOptionBuilder().build();
		assertDoesNotThrow(() -> validator.validateProduct(option));
	}

	@Test
	public void testMissingUnderlying() {
		EquityOption option = EquityOption.builder("SAN_C_100", OptionTrade.Type.CALL, BigDecimal.valueOf(100),
				LocalDate.of(2026, 6, 19), contractSpecification).underlying(null).build();

		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(option));
	}

	@Test
	public void testMissingStyle() {
		EquityOptionContractSpecification specWithoutStyle = new EquityOptionContractSpecification("SAN_NO_STYLE");
		EquityOption option = EquityOption.builder("SAN_C_100", OptionTrade.Type.CALL, BigDecimal.valueOf(100),
				LocalDate.of(2026, 6, 19), specWithoutStyle).underlying(underlying).build();

		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(option));
	}

	@Test
	public void testMissingCode() {
		EquityOption option = EquityOption.builder(null, OptionTrade.Type.CALL, BigDecimal.valueOf(100),
				LocalDate.of(2026, 6, 19), contractSpecification).underlying(underlying).build();

		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(option));
	}

	@Test
	public void testMissingMaturityDate() {
		EquityOption option = EquityOption
				.builder("SAN_C_100", OptionTrade.Type.CALL, BigDecimal.valueOf(100), null, contractSpecification)
				.underlying(underlying).build();

		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(option));
	}

	@Test
	public void testMissingContractSpecification() {
		EquityOption option = EquityOption
				.builder("SAN_C_100", OptionTrade.Type.CALL, BigDecimal.valueOf(100), LocalDate.of(2026, 6, 19), null)
				.underlying(underlying).build();

		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(option));
	}

	@Test
	public void testMissingStrike() {
		EquityOption option = EquityOption
				.builder("SAN_C_100", OptionTrade.Type.CALL, null, LocalDate.of(2026, 6, 19), contractSpecification)
				.underlying(underlying).build();

		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(option));
	}

	@Test
	public void testZeroStrike() {
		EquityOption option = EquityOption.builder("SAN_C_100", OptionTrade.Type.CALL, BigDecimal.ZERO,
				LocalDate.of(2026, 6, 19), contractSpecification).underlying(underlying).build();

		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(option));
	}

	@Test
	public void testNegativeStrike() {
		EquityOption option = EquityOption.builder("SAN_C_100", OptionTrade.Type.CALL, BigDecimal.valueOf(-10),
				LocalDate.of(2026, 6, 19), contractSpecification).underlying(underlying).build();

		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(option));
	}

}