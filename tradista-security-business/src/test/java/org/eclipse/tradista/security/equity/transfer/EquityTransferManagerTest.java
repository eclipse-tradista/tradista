package org.eclipse.tradista.security.equity.transfer;

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
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.productinventory.service.ProductInventoryBusinessDelegate;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.service.FixingErrorBusinessDelegate;
import org.eclipse.tradista.core.transfer.service.TransferBusinessDelegate;
import org.eclipse.tradista.security.equity.model.Equity;
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
class EquityTransferManagerTest {

	private static final Book book = TradistaTestUtil.createTradingBook();
	private static final Currency EUR = TradistaTestUtil.EUR;
	private static final Exchange EPA = TradistaTestUtil.createExchange("EPA");

	private static MockedConstruction<ConfigurationBusinessDelegate> mockedConfigConstruction;
	private static MockedStatic<PricerUtil> mockedPricerUtil;

	@Mock
	private TransferBusinessDelegate transferBusinessDelegate;

	@Mock
	private FixingErrorBusinessDelegate fixingErrorBusinessDelegate;

	@Mock
	private ProductInventoryBusinessDelegate productInventoryBusinessDelegate;

	@InjectMocks
	private EquityTransferManager transferManager;

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
		Equity equity = Equity.builder(EPA, "FR0000120271").currency(EUR).build();

		CashTransfer transfer = CashTransfer.builder(book, TransferPurpose.DIVIDEND, LocalDate.of(2026, 4, 1), EUR)
				.product(equity).fixingDateTime(LocalDateTime.of(2026, 4, 1, 0, 0)).status(Transfer.Status.UNKNOWN)
				.build();
		transfer.setId(20L);

		mockedPricerUtil.when(() -> PricerUtil.getValueAsOfDateFromQuote(any(), anyLong(), eq(QuoteType.DIVIDEND_YIELD),
				eq(QuoteValue.CLOSE), any())).thenReturn(BigDecimal.valueOf(2.5));
		mockedPricerUtil.when(() -> PricerUtil.getValueAsOfDateFromQuote(any(), anyLong(), eq(QuoteType.EQUITY_PRICE),
				eq(QuoteValue.CLOSE), any())).thenReturn(BigDecimal.valueOf(50.0));
		lenient().when(productInventoryBusinessDelegate.getQuantityByDateProductAndBookIds(anyLong(), anyLong(), any()))
				.thenReturn(BigDecimal.valueOf(1000));

		transferManager.fixCashTransfer(transfer, 1L);

		assertEquals(Transfer.Status.KNOWN, transfer.getStatus());
		verify(transferBusinessDelegate).saveTransfer(transfer);
	}
}