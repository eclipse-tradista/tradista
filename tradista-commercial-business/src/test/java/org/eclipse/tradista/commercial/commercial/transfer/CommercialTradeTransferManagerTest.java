package org.eclipse.tradista.commercial.commercial.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.eclipse.tradista.commercial.commercial.messaging.CommercialTradeEvent;
import org.eclipse.tradista.commercial.commercial.model.CommercialTrade;
import org.eclipse.tradista.commercial.commercial.model.CommercialTrade.CommercialCashFlow;
import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.eclipse.tradista.core.workflow.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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

class CommercialTradeTransferManagerTest {

	private TransferBusinessDelegate transferBusinessDelegate;
	private CommercialTransferManager transferManager;

	@BeforeEach
	void setUp() {
		transferBusinessDelegate = mock(TransferBusinessDelegate.class);
		transferManager = new CommercialTransferManager();
		transferManager.transferBusinessDelegate = transferBusinessDelegate;
	}

	@Test
	@SuppressWarnings("unchecked")
	void testCreateTransfersOnNewTrade() throws TradistaBusinessException {
		CommercialCashFlow flow = new CommercialCashFlow();
		flow.setSettlementDate(LocalDate.now().plusDays(15));
		flow.setAmount(new BigDecimal("5000"));

		CommercialTrade trade = CommercialTrade.builder().id(1L).book(mock(Book.class)).currency(mock(Currency.class))
				.buySell(true).documentReference("PO-001").items(List.of(flow)).build();

		CommercialTradeEvent event = new CommercialTradeEvent();
		event.setTrade(trade);

		transferManager.createTransfers(event);

		ArgumentCaptor<List<Transfer>> captor = ArgumentCaptor.forClass(List.class);
		verify(transferBusinessDelegate).saveTransfers(captor.capture());
		List<Transfer> saved = captor.getValue();
		assertEquals(1, saved.size());
		assertEquals(Transfer.Status.KNOWN, saved.get(0).getStatus());
		assertEquals(Transfer.Direction.PAY, saved.get(0).getDirection());
		assertEquals(TransferPurpose.CASH_SETTLEMENT, saved.get(0).getPurpose());
	}

	@Test
	@SuppressWarnings("unchecked")
	void testUpdateTradePreservesSettledAndCancelsKnown() throws TradistaBusinessException {
		Book book = mock(Book.class);
		Currency currency = mock(Currency.class);

		CommercialCashFlow flow = new CommercialCashFlow();
		flow.setSettlementDate(LocalDate.now().plusDays(30));
		flow.setAmount(new BigDecimal("7000"));

		CommercialTrade oldTrade = CommercialTrade.builder().id(1L).book(book).currency(currency).build();
		CommercialTrade newTrade = CommercialTrade.builder().id(1L).book(book).currency(currency).buySell(true)
				.items(List.of(flow)).build();

		CashTransfer settledTransfer = new CashTransfer(book, TransferPurpose.CASH_SETTLEMENT,
				LocalDate.now().plusDays(5), oldTrade, currency);
		settledTransfer.setStatus(Transfer.Status.SETTLED);

		CashTransfer knownTransfer = new CashTransfer(book, TransferPurpose.CASH_SETTLEMENT,
				LocalDate.now().plusDays(30), oldTrade, currency);
		knownTransfer.setStatus(Transfer.Status.KNOWN);

		when(transferBusinessDelegate.getTransfersByTradeIdAndPurpose(1L, TransferPurpose.CASH_SETTLEMENT, false))
				.thenReturn(List.of(settledTransfer, knownTransfer));

		CommercialTradeEvent event = new CommercialTradeEvent();
		event.setOldTrade(oldTrade);
		event.setTrade(newTrade);

		transferManager.createTransfers(event);

		assertEquals(Transfer.Status.SETTLED, settledTransfer.getStatus());
		assertEquals(Transfer.Status.KNOWN, knownTransfer.getStatus());
		assertEquals(new BigDecimal("7000"), knownTransfer.getAmount());
		verify(transferBusinessDelegate).saveTransfers(anyList());
	}

	@Test
	void testCancelTradeCancelsKnownTransfers() throws TradistaBusinessException {
		Book book = mock(Book.class);
		Currency currency = mock(Currency.class);
		Status cancelledStatus = new Status();
		cancelledStatus.setName("CANCELLED");

		CommercialTrade oldTrade = CommercialTrade.builder().id(1L).book(book).currency(currency).build();
		CommercialTrade cancelledTrade = CommercialTrade.builder().id(1L).book(book).currency(currency)
				.status(cancelledStatus).build();

		CashTransfer knownTransfer = new CashTransfer(book, TransferPurpose.CASH_SETTLEMENT,
				LocalDate.now().plusDays(30), oldTrade, currency);
		knownTransfer.setStatus(Transfer.Status.KNOWN);

		when(transferBusinessDelegate.getTransfersByTradeIdAndPurpose(1L, TransferPurpose.CASH_SETTLEMENT, false))
				.thenReturn(List.of(knownTransfer));

		CommercialTradeEvent event = new CommercialTradeEvent();
		event.setOldTrade(oldTrade);
		event.setTrade(cancelledTrade);

		transferManager.createTransfers(event);

		assertEquals(Transfer.Status.CANCELED, knownTransfer.getStatus());
		verify(transferBusinessDelegate).saveTransfers(anyList());
	}

}