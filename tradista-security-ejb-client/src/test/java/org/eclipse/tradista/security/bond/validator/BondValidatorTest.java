package org.eclipse.tradista.security.bond.validator;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.test.TradistaTestUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.security.bond.model.Bond;
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

public class BondValidatorTest {

	private static final BondValidator validator = new BondValidator();
	private static final Exchange exchange = TradistaTestUtil.createExchange("EPA");
	private static final Currency currency = TradistaTestUtil.EUR;

	@Test
	public void testNullProduct() {
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(null));
	}

	@Test
	public void testMissingExchange() {
		Bond bond = Bond.builder(null, "FR0000120271").currency(currency).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(bond));
	}

	@Test
	public void testMissingIsin() {
		Bond bond = Bond.builder(exchange, null).currency(currency).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(bond));
	}

	@Test
	public void testMissingCurrency() {
		Bond bond = Bond.builder(exchange, "FR0000120271").currency(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(bond));
	}

	@Test
	public void testMissingCouponForFixedRateBond() {
		Bond bond = Bond.builder(exchange, "FR0000120271").currency(currency).coupon(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(bond));
	}

	@Test
	public void testZeroOrNegativeCouponForFixedRateBond() {
		Bond bondZero = Bond.builder(exchange, "FR0000120271").currency(currency).coupon(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(bondZero));

		Bond bondNeg = Bond.builder(exchange, "FR0000120271").currency(currency).coupon(BigDecimal.valueOf(-1.5))
				.build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(bondNeg));
	}

	@Test
	public void testMissingPrincipal() {
		Bond bond = Bond.builder(exchange, "FR0000120271").currency(currency).coupon(BigDecimal.valueOf(2.5))
				.principal(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(bond));
	}

	@Test
	public void testZeroOrNegativePrincipal() {
		Bond bondZero = Bond.builder(exchange, "FR0000120271").currency(currency).principal(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(bondZero));

		Bond bondNeg = Bond.builder(exchange, "FR0000120271").currency(currency).principal(BigDecimal.valueOf(-1000))
				.build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(bondNeg));
	}

	@Test
	public void testDatedDateBeforeIssueDate() {
		Bond bond = Bond.builder(exchange, "FR0000120271").currency(currency).issueDate(LocalDate.of(2025, 6, 1))
				.datedDate(LocalDate.of(2025, 5, 1)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(bond));
	}

	@Test
	public void testMaturityDateBeforeIssueDate() {
		Bond bond = Bond.builder(exchange, "FR0000120271").currency(currency).issueDate(LocalDate.of(2025, 6, 1))
				.maturityDate(LocalDate.of(2025, 5, 1)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(bond));
	}

	@Test
	public void testMaturityDateBeforeDatedDate() {
		Bond bond = Bond.builder(exchange, "FR0000120271").currency(currency).datedDate(LocalDate.of(2025, 6, 1))
				.maturityDate(LocalDate.of(2025, 5, 1)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(bond));
	}

	@Test
	public void testZeroOrNegativeRedemptionPrice() {
		Bond bondZero = Bond.builder(exchange, "FR0000120271").currency(currency).principal(BigDecimal.valueOf(1000))
				.redemptionPrice(BigDecimal.ZERO).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(bondZero));

		Bond bondNeg = Bond.builder(exchange, "FR0000120271").currency(currency).principal(BigDecimal.valueOf(1000))
				.redemptionPrice(BigDecimal.valueOf(-100)).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(bondNeg));
	}

	@Test
	public void testRedemptionPriceWithoutRedemptionCurrency() {
		Bond bond = Bond.builder(exchange, "FR0000120271").currency(currency).redemptionPrice(BigDecimal.valueOf(100))
				.redemptionCurrency(null).build();
		assertThrows(TradistaBusinessException.class, () -> validator.validateProduct(bond));
	}

}