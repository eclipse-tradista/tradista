package org.eclipse.tradista.security.repo.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.test.TradistaTestUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.daycountconvention.model.DayCountConvention;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.eclipse.tradista.security.gcrepo.model.GCRepoTrade;
import org.eclipse.tradista.security.repo.model.RepoTrade;
import org.eclipse.tradista.security.repo.trade.RepoTradeUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class RepoTransferUtilTest {

	private static final Book book = TradistaTestUtil.createBook();
	private static final LegalEntity counterparty = TradistaTestUtil.createCounterparty();
	private static final Currency EUR = TradistaTestUtil.EUR;

	private static MockedStatic<RepoTradeUtil> mockedRepoTradeUtil;

	@Mock
	private TransferBusinessDelegate transferBusinessDelegate;

	@BeforeAll
	static void setUpClass() {
		mockedRepoTradeUtil = Mockito.mockStatic(RepoTradeUtil.class);
	}

	@AfterAll
	static void tearDownClass() {
		if (mockedRepoTradeUtil != null) {
			mockedRepoTradeUtil.close();
		}
	}

	@Test
	void testFixCashTransferSetsStatusToKnownOnSuccess() throws Exception {
		Field transferDelegateField = RepoTransferUtil.class.getDeclaredField("transferBusinessDelegate");
		transferDelegateField.setAccessible(true);
		Object originalDelegate = transferDelegateField.get(null);
		transferDelegateField.set(null, transferBusinessDelegate);

		try {
			GCRepoTrade trade = GCRepoTrade.builder().book(book).counterparty(counterparty).currency(EUR)
					.tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
					.endDate(LocalDate.of(2025, 6, 10)).amount(BigDecimal.valueOf(1_000_000))
					.repoRate(BigDecimal.valueOf(2.5)).build();
			trade.setId(40L);
			trade.setBuySell(true);

			CashTransfer transfer = CashTransfer
					.builder(book, TransferPurpose.CASH_SETTLEMENT, LocalDate.of(2026, 4, 1), EUR).trade(trade)
					.fixingDateTime(LocalDateTime.of(2026, 4, 1, 0, 0)).status(Transfer.Status.UNKNOWN).build();
			transfer.setId(40L);

			mockedRepoTradeUtil.when(() -> RepoTradeUtil.getClosingLegPayment(any(RepoTrade.class), anyLong(),
					any(DayCountConvention.class))).thenReturn(BigDecimal.valueOf(10050));

			RepoTransferUtil.fixCashTransfer(transfer, 1L);

			assertEquals(Transfer.Status.KNOWN, transfer.getStatus());
			verify(transferBusinessDelegate).saveTransfer(transfer);
		} finally {
			transferDelegateField.set(null, originalDelegate);
		}
	}
}