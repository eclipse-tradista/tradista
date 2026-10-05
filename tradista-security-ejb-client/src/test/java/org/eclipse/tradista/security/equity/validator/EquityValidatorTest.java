package org.eclipse.tradista.security.equity.validator;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.test.TradistaTestUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.security.equity.model.Equity;
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

public class EquityValidatorTest {

	private static final EquityValidator validator = new EquityValidator();
	private static final Exchange exchange = TradistaTestUtil.createExchange("EPA");
	private static final Currency currency = TradistaTestUtil.EUR;

	@Test
	public void testNullProduct() {
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(null));
	}

	@Test
	public void testMissingExchange() {
		Equity equity = Equity.builder(null, "FR0000120271").currency(currency).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(equity));
	}

	@Test
	public void testMissingIsin() {
		Equity equity = Equity.builder(exchange, null).currency(currency).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(equity));
	}

	@Test
	public void testMissingCurrency() {
		Equity equity = Equity.builder(exchange, "FR0000120271").currency(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(equity));
	}

	@Test
	public void testMissingActiveFrom() {
		Equity equity = Equity.builder(exchange, "FR0000120271").currency(currency).activeFrom(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(equity));
	}

	@Test
	public void testMissingActiveTo() {
		Equity equity = Equity.builder(exchange, "FR0000120271").currency(currency).activeFrom(LocalDate.of(2020, 1, 1))
				.activeTo(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(equity));
	}

	@Test
	public void testActiveFromAfterActiveTo() {
		Equity equity = Equity.builder(exchange, "FR0000120271").currency(currency).activeFrom(LocalDate.of(2025, 1, 1))
				.activeTo(LocalDate.of(2020, 1, 1)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(equity));
	}

	@Test
	public void testZeroOrNegativeTotalIssued() {
		Equity equityZero = Equity.builder(exchange, "FR0000120271").currency(currency).totalIssued(0).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(equityZero));

		Equity equityNeg = Equity.builder(exchange, "FR0000120271").currency(currency).totalIssued(-100).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(equityNeg));
	}

	@Test
	public void testNegativeTradingSize() {
		Equity equity = Equity.builder(exchange, "FR0000120271").currency(currency).tradingSize(-1).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(equity));
	}

	@Test
	public void testPayDividendWithoutDividendCurrency() {
		Equity equity = Equity.builder(exchange, "FR0000120271").currency(currency).payDividend(true)
				.dividendCurrency(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(equity));
	}

}