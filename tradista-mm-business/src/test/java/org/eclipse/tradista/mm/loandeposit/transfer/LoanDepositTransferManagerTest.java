package org.eclipse.tradista.mm.loandeposit.transfer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.test.TradistaTestUtil;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.daycountconvention.model.DayCountConvention;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.FixingErrorBusinessDelegate;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.eclipse.tradista.mm.loan.transfer.LoanTransferManager;
import org.eclipse.tradista.mm.loandeposit.messaging.LoanDepositTradeEvent;
import org.eclipse.tradista.mm.loandeposit.model.LoanDepositTrade;
import org.eclipse.tradista.mm.loandeposit.model.LoanTrade;
import org.eclipse.tradista.mm.loandeposit.service.LoanDepositTradeBusinessDelegate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
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
class LoanDepositTransferManagerTest {

	private static final Book book = TradistaTestUtil.createTradingBook();
	private static final LegalEntity counterparty = TradistaTestUtil.createCounterparty();
	private static final Currency currency = TradistaTestUtil.EUR;
	private static final DayCountConvention dcc = new DayCountConvention(DayCountConvention.ACT_360);

	private static MockedConstruction<ConfigurationBusinessDelegate> mockedConfigConstruction;

	@Mock
	private TransferBusinessDelegate transferBusinessDelegate;

	@Mock
	private FixingErrorBusinessDelegate fixingErrorBusinessDelegate;

	@Mock
	private LoanDepositTradeBusinessDelegate loanDepositBusinessDelegate;

	@InjectMocks
	private LoanTransferManager transferManager;

	@BeforeAll
	static void setUpConfigurationBusinessDelegate() {
		mockedConfigConstruction = Mockito.mockConstruction(ConfigurationBusinessDelegate.class, (mock, _) -> {
			lenient().when(mock.getScale()).thenReturn((short) 2);
			lenient().when(mock.getRoundingMode()).thenReturn(RoundingMode.HALF_EVEN);
		});
	}

	@AfterAll
	static void tearDownConfigurationBusinessDelegate() {
		if (mockedConfigConstruction != null) {
			mockedConfigConstruction.close();
		}
	}

	@BeforeEach
	void setUp() throws Exception {
		lenient().when(
				transferBusinessDelegate.getTransfersByTradeIdAndPurpose(any(Long.class), any(), any(Boolean.class)))
				.thenReturn(new ArrayList<>());
		lenient().when(loanDepositBusinessDelegate.isBusinessDay(any(), any())).thenReturn(true);
	}

	private LoanTrade createFixedRateLoanTrade() {
		LoanTrade trade = LoanTrade.builder().fixedRate(BigDecimal.valueOf(3.5)).currency(currency)
				.amount(BigDecimal.valueOf(1_000_000)).book(book).counterparty(counterparty)
				.settlementDate(LocalDate.of(2026, 1, 1)).endDate(LocalDate.of(2026, 6, 1))
				.tradeDate(LocalDate.of(2025, 12, 1)).dayCountConvention(dcc).paymentFrequency(Tenor.THREE_MONTHS)
				.interestPayment(InterestPayment.END_OF_PERIOD).interestType(LoanDepositTrade.InterestType.SIMPLE)
				.interestFixing(null).build();
		trade.setId(1L);
		trade.setBuySell(true);
		return trade;
	}

	private LoanTrade createFloatingRateLoanTrade(InterestPayment interestFixing) {
		LoanTrade trade = LoanTrade.builder().floatingRateIndex(TradistaTestUtil.EURIBOR).currency(currency)
				.amount(BigDecimal.valueOf(1_000_000)).book(book).counterparty(counterparty)
				.settlementDate(LocalDate.of(2026, 1, 1)).endDate(LocalDate.of(2026, 6, 1))
				.tradeDate(LocalDate.of(2025, 12, 1)).dayCountConvention(dcc).paymentFrequency(Tenor.THREE_MONTHS)
				.interestPayment(InterestPayment.END_OF_PERIOD).interestFixing(interestFixing)
				.interestType(LoanDepositTrade.InterestType.SIMPLE).build();
		trade.setId(2L);
		trade.setBuySell(true);
		return trade;
	}

	@Test
	void testCreateTransfersWhenBothInterestFixingAreNullDoesNotThrowNPE() {
		LoanTrade oldTrade = createFixedRateLoanTrade();
		LoanTrade updatedTrade = createFixedRateLoanTrade();

		LoanDepositTradeEvent event = new LoanDepositTradeEvent();
		event.setOldTrade(oldTrade);
		event.setTrade(updatedTrade);

		assertDoesNotThrow(() -> transferManager.createTransfers(event));
	}

	@Test
	void testCreateTransfersWhenOldInterestFixingNullAndNewNotNullDoesNotThrowNPE() {
		LoanTrade oldTrade = createFixedRateLoanTrade();
		LoanTrade updatedTrade = createFloatingRateLoanTrade(InterestPayment.BEGINNING_OF_PERIOD);

		LoanDepositTradeEvent event = new LoanDepositTradeEvent();
		event.setOldTrade(oldTrade);
		event.setTrade(updatedTrade);

		assertDoesNotThrow(() -> transferManager.createTransfers(event));
	}

	@Test
	void testCreateTransfersWhenOldInterestFixingNotNullAndNewNullDoesNotThrowNPE() {
		LoanTrade oldTrade = createFloatingRateLoanTrade(InterestPayment.BEGINNING_OF_PERIOD);
		LoanTrade updatedTrade = createFixedRateLoanTrade();

		LoanDepositTradeEvent event = new LoanDepositTradeEvent();
		event.setOldTrade(oldTrade);
		event.setTrade(updatedTrade);

		assertDoesNotThrow(() -> transferManager.createTransfers(event));
	}

	@Test
	void testCreateTransfersWhenBothInterestFixingAreNonNullAndDifferentDoesNotThrowNPE() {
		LoanTrade oldTrade = createFloatingRateLoanTrade(InterestPayment.BEGINNING_OF_PERIOD);
		LoanTrade updatedTrade = createFloatingRateLoanTrade(InterestPayment.END_OF_PERIOD);

		LoanDepositTradeEvent event = new LoanDepositTradeEvent();
		event.setOldTrade(oldTrade);
		event.setTrade(updatedTrade);

		assertDoesNotThrow(() -> transferManager.createTransfers(event));
	}

	@Test
	void testFixCashTransferSetsStatusToKnownOnSuccess() throws TradistaBusinessException {
		LoanTrade trade = createFloatingRateLoanTrade(InterestPayment.END_OF_PERIOD);
		CashTransfer transfer = CashTransfer
				.builder(book, TransferPurpose.INTEREST_PAYMENT, LocalDate.of(2026, 4, 1), currency).trade(trade)
				.fixingDateTime(LocalDateTime.of(2026, 4, 1, 0, 0)).status(Transfer.Status.UNKNOWN).build();
		transfer.setId(100L);

		BigDecimal calculatedAmount = BigDecimal.valueOf(5000);
		when(loanDepositBusinessDelegate.getPaymentAmount(eq(trade), any(LocalDate.class), any(LocalDate.class),
				anyLong(), anyLong())).thenReturn(calculatedAmount);

		transferManager.fixCashTransfer(transfer, 1L);

		assertEquals(Transfer.Status.KNOWN, transfer.getStatus());
		assertEquals(Transfer.Direction.PAY, transfer.getDirection());
		assertEquals(calculatedAmount, transfer.getAmount());
		verify(transferBusinessDelegate).saveTransfer(transfer);
	}
}