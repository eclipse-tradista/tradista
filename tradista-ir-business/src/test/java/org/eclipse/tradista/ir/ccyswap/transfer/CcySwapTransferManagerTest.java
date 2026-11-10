package org.eclipse.tradista.ir.ccyswap.transfer;

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
import org.eclipse.tradista.ir.ccyswap.model.CcySwapTrade;
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
class CcySwapTransferManagerTest {

	private static final Book book = TradistaTestUtil.createTradingBook();
	private static final LegalEntity counterparty = TradistaTestUtil.createCounterparty();
	private static final Currency EUR = TradistaTestUtil.EUR;
	private static final Currency USD = TradistaTestUtil.USD;
	private static final DayCountConvention dcc = new DayCountConvention(DayCountConvention.ACT_360);

	private static MockedConstruction<ConfigurationBusinessDelegate> mockedConfigConstruction;
	private static MockedStatic<PricerUtil> mockedPricerUtil;

	@Mock
	private TransferBusinessDelegate transferBusinessDelegate;

	@Mock
	private FixingErrorBusinessDelegate fixingErrorBusinessDelegate;

	@InjectMocks
	private CcySwapTransferManager transferManager;

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
		CcySwapTrade trade = CcySwapTrade.builder().currency(EUR).currencyTwo(USD).amount(BigDecimal.valueOf(1_000_000))
				.notionalAmountTwo(BigDecimal.valueOf(1_100_000)).book(book).counterparty(counterparty)
				.paymentDayCountConvention(dcc).receptionReferenceRateIndex(TradistaTestUtil.EURIBOR)
				.receptionReferenceRateIndexTenor(Tenor.THREE_MONTHS).tradeDate(LocalDate.of(2025, 6, 1))
				.settlementDate(LocalDate.of(2025, 6, 3)).maturityDate(LocalDate.of(2026, 6, 3)).buySell(true).build();
		trade.setId(1L);

		CashTransfer transfer = CashTransfer
				.builder(book, TransferPurpose.FLOATING_LEG_INTEREST_PAYMENT, LocalDate.of(2026, 4, 1), EUR)
				.trade(trade).fixingDateTime(LocalDateTime.of(2026, 4, 1, 0, 0)).status(Transfer.Status.UNKNOWN)
				.build();
		transfer.setId(10L);

		mockedPricerUtil.when(() -> PricerUtil.getValueAsOfDateFromQuote(any(), anyLong(), eq(QuoteType.INTEREST_RATE),
				eq(QuoteValue.CLOSE), any())).thenReturn(BigDecimal.valueOf(3.5));
		mockedPricerUtil.when(() -> PricerUtil.daysToYear(any(), any(), any())).thenReturn(BigDecimal.valueOf(0.25));

		transferManager.fixCashTransfer(transfer, 1L);

		assertEquals(Transfer.Status.KNOWN, transfer.getStatus());
		verify(transferBusinessDelegate).saveTransfer(transfer);
	}
}