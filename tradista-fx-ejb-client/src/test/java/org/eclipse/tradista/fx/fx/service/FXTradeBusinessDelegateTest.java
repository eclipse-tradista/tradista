package org.eclipse.tradista.fx.fx.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.test.TradistaTestUtil;
import org.eclipse.tradista.fx.fx.model.FXTrade;
import org.eclipse.tradista.fx.test.util.FXTestUtil;
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

class FXTradeBusinessDelegateTest {

	private static final FXTradeBusinessDelegate delegate = new FXTradeBusinessDelegate();

	@BeforeAll
	static void setUp() {
		FXTestUtil.setupFXExchange();
	}

	@Test
	void testDeterminateTypeNullTrade() throws TradistaBusinessException {
		assertNull(delegate.determinateType(null));
	}

	@Test
	void testDeterminateTypeMissingDates() throws TradistaBusinessException {
		FXTrade noDates = FXTrade.builder().currencyOne(TradistaTestUtil.EUR).currency(TradistaTestUtil.USD).build();
		assertNull(delegate.determinateType(noDates));

		FXTrade noSettlementDate = FXTrade.builder().currencyOne(TradistaTestUtil.EUR).currency(TradistaTestUtil.USD)
				.tradeDate(LocalDate.of(2025, 12, 1)).build();
		assertNull(delegate.determinateType(noSettlementDate));

		FXTrade noTradeDate = FXTrade.builder().currencyOne(TradistaTestUtil.EUR).currency(TradistaTestUtil.USD)
				.settlementDate(LocalDate.of(2025, 12, 3)).build();
		assertNull(delegate.determinateType(noTradeDate));
	}

	@Test
	void testDeterminateTypeSpotAndForward() throws TradistaBusinessException {
		FXTrade spotTrade = FXTrade.builder().currencyOne(TradistaTestUtil.EUR).currency(TradistaTestUtil.USD)
				.tradeDate(LocalDate.of(2025, 12, 1)).settlementDate(LocalDate.of(2025, 12, 3)).build();
		assertEquals(FXTrade.Type.FX_SPOT, delegate.determinateType(spotTrade));

		FXTrade forwardTrade = FXTrade.builder().currencyOne(TradistaTestUtil.EUR).currency(TradistaTestUtil.USD)
				.tradeDate(LocalDate.of(2025, 12, 1)).settlementDate(LocalDate.of(2025, 12, 10)).build();
		assertEquals(FXTrade.Type.FX_FORWARD, delegate.determinateType(forwardTrade));
	}

}