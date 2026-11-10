package org.eclipse.tradista.ir.ircollar.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.test.TradistaTestUtil;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.daycountconvention.model.DayCountConvention;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.FixingErrorBusinessDelegate;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.eclipse.tradista.ir.fra.model.FRATrade;
import org.eclipse.tradista.ir.ircapfloorcollar.model.IRCapFloorCollarTrade;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
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
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

@ExtendWith(MockitoExtension.class)
class IRCollarTransferManagerTest {

	private static final Book book = TradistaTestUtil.createTradingBook();
	private static final LegalEntity counterparty = TradistaTestUtil.createCounterparty();
	private static final Currency EUR = TradistaTestUtil.EUR;
	private static final DayCountConvention dcc = new DayCountConvention(DayCountConvention.ACT_360);

	private static MockedConstruction<ConfigurationBusinessDelegate> mockedConfigConstruction;
	private static MockedStatic<PricerUtil> mockedPricerUtil;

	@Mock
	private TransferBusinessDelegate transferBusinessDelegate;

	@Mock
	private FixingErrorBusinessDelegate fixingErrorBusinessDelegate;

	@InjectMocks
	private IRCollarTransferManager transferManager;

	@BeforeAll
	static void setUpClass() {
		mockedConfigConstruction = Mockito.mockConstruction(ConfigurationBusinessDelegate.class, (mock, _) -> {
			lenient().when(mock.getScale()).thenReturn((short) 2);
			lenient().when(mock.getRoundingMode()).thenReturn(RoundingMode.HALF_EVEN);
		});
		mockedPricerUtil = Mockito.mockStatic(PricerUtil.class);
	}

	@AfterAll
	static void tearDownClass() {
		if (mockedConfigConstruction != null) {
			mockedConfigConstruction.close();
		}
		if (mockedPricerUtil != null) {
			mockedPricerUtil.close();
		}
	}

	@Test
	void testFixCashTransferSetsStatusToKnownOnSuccess() throws TradistaBusinessException {
		FRATrade forwardTrade = FRATrade.builder().fixedRate(BigDecimal.valueOf(2.5))
				.startDate(LocalDate.of(2025, 5, 30)).maturityDate(LocalDate.of(2025, 8, 30))
				.referenceRateIndex(TradistaTestUtil.EURIBOR).referenceRateIndexTenor(Tenor.THREE_MONTHS)
				.dayCountConvention(dcc).currency(EUR).amount(BigDecimal.valueOf(1_000_000)).book(book)
				.counterparty(counterparty).tradeDate(LocalDate.of(2025, 5, 28))
				.settlementDate(LocalDate.of(2025, 6, 1)).build();

		IRCapFloorCollarTrade trade = IRCapFloorCollarTrade.builder().capStrike(BigDecimal.valueOf(3.0))
				.floorStrike(BigDecimal.valueOf(1.0)).irForwardTrade(forwardTrade).currency(EUR)
				.amount(BigDecimal.valueOf(10_000)).book(book).counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 5, 28)).settlementDate(LocalDate.of(2025, 6, 1)).buySell(true).build();
		trade.setId(4L);

		CashTransfer transfer = CashTransfer
				.builder(book, TransferPurpose.CASH_SETTLEMENT, LocalDate.of(2026, 4, 1), EUR).trade(trade)
				.fixingDateTime(LocalDateTime.of(2026, 4, 1, 0, 0)).status(Transfer.Status.UNKNOWN).build();
		transfer.setId(40L);

		mockedPricerUtil.when(() -> PricerUtil.getValueAsOfDateFromQuote(any(), anyLong(), eq(QuoteType.INTEREST_RATE),
				eq(QuoteValue.CLOSE), any())).thenReturn(BigDecimal.valueOf(3.5));
		mockedPricerUtil.when(() -> PricerUtil.daysToYear(any(), any(), any())).thenReturn(BigDecimal.valueOf(0.25));

		transferManager.fixCashTransfer(transfer, 1L);

		assertEquals(Transfer.Status.KNOWN, transfer.getStatus());
		verify(transferBusinessDelegate).saveTransfer(transfer);
	}
}