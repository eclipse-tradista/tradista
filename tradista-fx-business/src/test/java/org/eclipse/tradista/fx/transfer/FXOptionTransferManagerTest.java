package org.eclipse.tradista.fx.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.test.TradistaTestUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.FixingErrorBusinessDelegate;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.eclipse.tradista.fx.fx.model.FXTrade;
import org.eclipse.tradista.fx.fxoption.model.FXOptionTrade;
import org.eclipse.tradista.fx.fxoption.transfer.FXOptionTransferManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

@ExtendWith(MockitoExtension.class)
class FXOptionTransferManagerTest {

	private static final Book book = TradistaTestUtil.createTradingBook();
	private static final LegalEntity counterparty = TradistaTestUtil.createCounterparty();
	private static final Currency EUR = TradistaTestUtil.EUR;
	private static final Currency USD = TradistaTestUtil.USD;

	private static MockedStatic<PricerUtil> mockedPricerUtil;

	@Mock
	private TransferBusinessDelegate transferBusinessDelegate;

	@Mock
	private FixingErrorBusinessDelegate fixingErrorBusinessDelegate;

	@InjectMocks
	private FXOptionTransferManager transferManager;

	@BeforeAll
	static void setUpClass() {
		mockedPricerUtil = Mockito.mockStatic(PricerUtil.class);
	}

	@AfterAll
	static void tearDownClass() {
		if (mockedPricerUtil != null) {
			mockedPricerUtil.close();
		}
	}

	@Test
	void testFixCashTransferSetsStatusToKnownOnSuccess() throws TradistaBusinessException {
		FXTrade underlying = FXTrade.builder().currencyOne(EUR).currency(USD).amountOne(BigDecimal.valueOf(100_000))
				.amount(BigDecimal.valueOf(110_000)).book(book).counterparty(counterparty)
				.tradeDate(LocalDate.of(2025, 12, 1)).settlementDate(LocalDate.of(2025, 12, 3))
				.type(FXTrade.Type.FX_SPOT).build();

		FXOptionTrade trade = FXOptionTrade.builder().underlying(underlying).book(book).counterparty(counterparty)
				.currency(USD).amount(BigDecimal.valueOf(5000)).strike(BigDecimal.valueOf(1.20))
				.style(VanillaOptionTrade.Style.EUROPEAN).type(OptionTrade.Type.CALL)
				.settlementType(OptionTrade.SettlementType.CASH).settlementDateOffset(2)
				.tradeDate(LocalDate.of(2025, 6, 1)).maturityDate(LocalDate.of(2025, 12, 1))
				.settlementDate(LocalDate.of(2025, 6, 3)).exerciseDate(LocalDate.of(2025, 12, 1)).build();
		trade.setId(100L);
		trade.setBuySell(true);

		CashTransfer transfer = CashTransfer
				.builder(book, TransferPurpose.CASH_SETTLEMENT, LocalDate.of(2026, 4, 1), USD).trade(trade)
				.fixingDateTime(LocalDateTime.of(2026, 4, 1, 0, 0)).status(Transfer.Status.UNKNOWN).build();
		transfer.setId(10L);

		mockedPricerUtil.when(() -> PricerUtil.getFXClosingRate(any(), any(), any(), anyLong()))
				.thenReturn(BigDecimal.valueOf(1.10));

		transferManager.fixCashTransfer(transfer, 1L);

		assertEquals(Transfer.Status.KNOWN, transfer.getStatus());
		verify(transferBusinessDelegate).saveTransfer(transfer);
	}
}
