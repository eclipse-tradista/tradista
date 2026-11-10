package org.eclipse.tradista.ir.transfer;

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
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.FixingErrorBusinessDelegate;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.eclipse.tradista.ir.ccyswap.model.CcySwapTrade;
import org.eclipse.tradista.ir.ccyswap.transfer.CcySwapTransferManager;
import org.eclipse.tradista.ir.ircap.transfer.IRCapTransferManager;
import org.eclipse.tradista.ir.ircapfloorcollar.model.IRCapFloorCollarTrade;
import org.eclipse.tradista.ir.ircollar.transfer.IRCollarTransferManager;
import org.eclipse.tradista.ir.irfloor.transfer.IRFloorTransferManager;
import org.eclipse.tradista.ir.irforward.model.IRForwardTrade;
import org.eclipse.tradista.ir.irswap.model.SingleCurrencyIRSwapTrade;
import org.eclipse.tradista.ir.irswapoption.model.IRSwapOptionTrade;
import org.eclipse.tradista.ir.irswapoption.transfer.IRSwapOptionTransferManager;
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
class IRTransferManagersFixCashTransferTest {

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
	private CcySwapTransferManager ccySwapTransferManager;

	@InjectMocks
	private IRCapTransferManager capTransferManager;

	@InjectMocks
	private IRFloorTransferManager floorTransferManager;

	@InjectMocks
	private IRCollarTransferManager collarTransferManager;

	@InjectMocks
	private IRSwapOptionTransferManager swapOptionTransferManager;

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
	void testCcySwapTransferManagerFixCashTransferSetsStatusToKnown() throws TradistaBusinessException {
		CcySwapTrade trade = new CcySwapTrade();
		trade.setId(1L);
		trade.setBuySell(true);
		trade.setAmount(BigDecimal.valueOf(1_000_000));
		trade.setNotionalAmountTwo(BigDecimal.valueOf(1_100_000));
		trade.setCurrency(EUR);
		trade.setCurrencyTwo(USD);
		trade.setPaymentDayCountConvention(dcc);
		trade.setReceptionReferenceRateIndex(TradistaTestUtil.EURIBOR);
		trade.setReceptionReferenceRateIndexTenor(Tenor.THREE_MONTHS);

		CashTransfer transfer = CashTransfer
				.builder(book, TransferPurpose.FLOATING_LEG_INTEREST_PAYMENT, LocalDate.of(2026, 4, 1), EUR)
				.trade(trade).fixingDateTime(LocalDateTime.of(2026, 4, 1, 0, 0)).status(Transfer.Status.UNKNOWN)
				.build();
		transfer.setId(10L);

		mockedPricerUtil.when(() -> PricerUtil.getValueAsOfDateFromQuote(any(), anyLong(), eq(QuoteType.INTEREST_RATE),
				eq(QuoteValue.CLOSE), any())).thenReturn(BigDecimal.valueOf(3.5));
		mockedPricerUtil.when(() -> PricerUtil.daysToYear(any(), any(), any())).thenReturn(BigDecimal.valueOf(0.25));

		ccySwapTransferManager.fixCashTransfer(transfer, 1L);

		assertEquals(Transfer.Status.KNOWN, transfer.getStatus());
		verify(transferBusinessDelegate).saveTransfer(transfer);
	}

	@Test
	void testIRCapTransferManagerFixCashTransferSetsStatusToKnown() throws TradistaBusinessException {
		IRForwardTrade irForwardTrade = new IRForwardTrade();
		irForwardTrade.setAmount(BigDecimal.valueOf(1_000_000));
		irForwardTrade.setDayCountConvention(dcc);
		irForwardTrade.setReferenceRateIndex(TradistaTestUtil.EURIBOR);
		irForwardTrade.setReferenceRateIndexTenor(Tenor.THREE_MONTHS);

		IRCapFloorCollarTrade trade = new IRCapFloorCollarTrade();
		trade.setId(2L);
		trade.setBuySell(true);
		trade.setCapStrike(BigDecimal.valueOf(2.0));
		trade.setIrForwardTrade(irForwardTrade);

		CashTransfer transfer = CashTransfer
				.builder(book, TransferPurpose.CASH_SETTLEMENT, LocalDate.of(2026, 4, 1), EUR).trade(trade)
				.fixingDateTime(LocalDateTime.of(2026, 4, 1, 0, 0)).status(Transfer.Status.UNKNOWN).build();
		transfer.setId(20L);

		mockedPricerUtil.when(() -> PricerUtil.getValueAsOfDateFromQuote(any(), anyLong(), eq(QuoteType.INTEREST_RATE),
				eq(QuoteValue.CLOSE), any())).thenReturn(BigDecimal.valueOf(3.5));
		mockedPricerUtil.when(() -> PricerUtil.daysToYear(any(), any(), any())).thenReturn(BigDecimal.valueOf(0.25));

		capTransferManager.fixCashTransfer(transfer, 1L);

		assertEquals(Transfer.Status.KNOWN, transfer.getStatus());
		verify(transferBusinessDelegate).saveTransfer(transfer);
	}

	@Test
	void testIRFloorTransferManagerFixCashTransferSetsStatusToKnown() throws TradistaBusinessException {
		IRForwardTrade irForwardTrade = new IRForwardTrade();
		irForwardTrade.setAmount(BigDecimal.valueOf(1_000_000));
		irForwardTrade.setDayCountConvention(dcc);
		irForwardTrade.setReferenceRateIndex(TradistaTestUtil.EURIBOR);
		irForwardTrade.setReferenceRateIndexTenor(Tenor.THREE_MONTHS);

		IRCapFloorCollarTrade trade = new IRCapFloorCollarTrade();
		trade.setId(3L);
		trade.setBuySell(true);
		trade.setFloorStrike(BigDecimal.valueOf(3.0));
		trade.setIrForwardTrade(irForwardTrade);

		CashTransfer transfer = CashTransfer
				.builder(book, TransferPurpose.CASH_SETTLEMENT, LocalDate.of(2026, 4, 1), EUR).trade(trade)
				.fixingDateTime(LocalDateTime.of(2026, 4, 1, 0, 0)).status(Transfer.Status.UNKNOWN).build();
		transfer.setId(30L);

		mockedPricerUtil.when(() -> PricerUtil.getValueAsOfDateFromQuote(any(), anyLong(), eq(QuoteType.INTEREST_RATE),
				eq(QuoteValue.CLOSE), any())).thenReturn(BigDecimal.valueOf(1.5));
		mockedPricerUtil.when(() -> PricerUtil.daysToYear(any(), any(), any())).thenReturn(BigDecimal.valueOf(0.25));

		floorTransferManager.fixCashTransfer(transfer, 1L);

		assertEquals(Transfer.Status.KNOWN, transfer.getStatus());
		verify(transferBusinessDelegate).saveTransfer(transfer);
	}

	@Test
	void testIRCollarTransferManagerFixCashTransferSetsStatusToKnown() throws TradistaBusinessException {
		IRForwardTrade irForwardTrade = new IRForwardTrade();
		irForwardTrade.setAmount(BigDecimal.valueOf(1_000_000));
		irForwardTrade.setDayCountConvention(dcc);
		irForwardTrade.setReferenceRateIndex(TradistaTestUtil.EURIBOR);
		irForwardTrade.setReferenceRateIndexTenor(Tenor.THREE_MONTHS);

		IRCapFloorCollarTrade trade = new IRCapFloorCollarTrade();
		trade.setId(4L);
		trade.setBuySell(true);
		trade.setCapStrike(BigDecimal.valueOf(3.0));
		trade.setFloorStrike(BigDecimal.valueOf(1.0));
		trade.setIrForwardTrade(irForwardTrade);

		CashTransfer transfer = CashTransfer
				.builder(book, TransferPurpose.CASH_SETTLEMENT, LocalDate.of(2026, 4, 1), EUR).trade(trade)
				.fixingDateTime(LocalDateTime.of(2026, 4, 1, 0, 0)).status(Transfer.Status.UNKNOWN).build();
		transfer.setId(40L);

		mockedPricerUtil.when(() -> PricerUtil.getValueAsOfDateFromQuote(any(), anyLong(), eq(QuoteType.INTEREST_RATE),
				eq(QuoteValue.CLOSE), any())).thenReturn(BigDecimal.valueOf(3.5));
		mockedPricerUtil.when(() -> PricerUtil.daysToYear(any(), any(), any())).thenReturn(BigDecimal.valueOf(0.25));

		collarTransferManager.fixCashTransfer(transfer, 1L);

		assertEquals(Transfer.Status.KNOWN, transfer.getStatus());
		verify(transferBusinessDelegate).saveTransfer(transfer);
	}

	@Test
	void testIRSwapOptionTransferManagerFixCashTransferSetsStatusToKnown() throws TradistaBusinessException {
		SingleCurrencyIRSwapTrade swap = SingleCurrencyIRSwapTrade.builder().currency(EUR).book(book)
				.counterparty(counterparty).amount(BigDecimal.valueOf(1_000_000))
				.paymentFixedInterestRate(BigDecimal.valueOf(2.0)).paymentReferenceRateIndex(TradistaTestUtil.EURIBOR)
				.paymentReferenceRateIndexTenor(Tenor.THREE_MONTHS)
				.receptionReferenceRateIndex(TradistaTestUtil.EURIBOR)
				.receptionReferenceRateIndexTenor(Tenor.THREE_MONTHS).maturityTenor(Tenor.ONE_YEAR)
				.paymentDayCountConvention(dcc).receptionDayCountConvention(dcc).build();

		IRSwapOptionTrade trade = IRSwapOptionTrade.builder().underlying(swap).book(book).counterparty(counterparty)
				.currency(EUR).amount(BigDecimal.valueOf(5000)).strike(BigDecimal.valueOf(2.5))
				.style(VanillaOptionTrade.Style.EUROPEAN).type(OptionTrade.Type.CALL)
				.settlementType(OptionTrade.SettlementType.CASH).settlementDateOffset(2)
				.tradeDate(LocalDate.of(2025, 6, 1)).settlementDate(LocalDate.of(2025, 6, 3))
				.maturityDate(LocalDate.of(2025, 12, 1)).exerciseDate(LocalDate.of(2025, 12, 1)).build();
		trade.setId(5L);
		trade.setBuySell(true);

		CashTransfer transfer = CashTransfer
				.builder(book, TransferPurpose.CASH_SETTLEMENT, LocalDate.of(2026, 4, 1), EUR).trade(trade)
				.fixingDateTime(LocalDateTime.of(2026, 4, 1, 0, 0)).status(Transfer.Status.UNKNOWN).build();
		transfer.setId(50L);

		mockedPricerUtil.when(() -> PricerUtil.getValueAsOfDateFromQuote(any(), anyLong(), eq(QuoteType.INTEREST_RATE),
				eq(QuoteValue.CLOSE), any())).thenReturn(BigDecimal.valueOf(3.5));
		mockedPricerUtil.when(() -> PricerUtil.daysToYear(Tenor.ONE_YEAR)).thenReturn(BigDecimal.valueOf(1.0));

		swapOptionTransferManager.fixCashTransfer(transfer, 1L);

		assertEquals(Transfer.Status.KNOWN, transfer.getStatus());
		verify(transferBusinessDelegate).saveTransfer(transfer);
	}
}
