package org.eclipse.tradista.ir.future.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.daycountconvention.model.DayCountConvention;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.marketdata.model.Instrument;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.ir.irforward.model.IRForwardTrade;

/*
 * Copyright 2015 Olivier Asuncion
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.    */
/**
 * Class representing futures trades.
 * 
 * Amount : trade price = 100 * (1 - negotiated rate)
 *
 * @param <F>
 */
public class FutureTrade extends IRForwardTrade<Future> implements Instrument {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7130145836929238705L;

	private BigDecimal quantity;

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	@Override
	public String getProductType() {
		return Future.FUTURE;
	}

	@Override
	public Tenor getFrequency() {
		return Tenor.NO_TENOR;
	}

	@Override
	public void setFrequency(Tenor frequency) {
		// not allowed to modify the frequency for Future..
	}

	/*
	 * Payment in a Future is done at the maturity date of the trade that is also
	 * the beginning of the period considered for the interest calculation
	 */
	@Override
	public InterestPayment getInterestPayment() {
		return InterestPayment.BEGINNING_OF_PERIOD;
	}

	@Override
	public void setInterestPayment(InterestPayment interestPayment) {
		// not allowed to modify the interest payment for Future..
	}

	@Override
	public String getInstrumentName() {
		return Future.FUTURE;
	}

	@Override
	public DayCountConvention getDayCountConvention() {
		return getProduct().getDayCountConvention();
	}

	@Override
	public void setDayCountConvention(DayCountConvention dayCountConvention) {
		// Forbidden to set the DCC. DCC is defined by the product.
	}

	@Override
	public Index getReferenceRateIndex() {
		return getProduct().getReferenceRateIndex();
	}

	@Override
	public Tenor getReferenceRateIndexTenor() {
		return getProduct().getReferenceRateIndexTenor();
	}

	@Override
	public void setReferenceRateIndex(Index referenceRate) {
		// Forbidden to set the reference rate index. Reference rate index is
		// defined by the product.
	}

	@Override
	public void setReferenceRateIndexTenor(Tenor referenceRateIndexTenor) {
		// Forbidden to set the reference rate index tenor. Reference rate index
		// tenor is defined by the product.
	}

	@Override
	public Currency getCurrency() {
		return getProduct().getCurrency();
	}

	@Override
	public void setCurrency(Currency currency) {
		// Forbidden to set the currency. The currency is defined by the
		// product.
	}

	@Override
	public LocalDate getMaturityDate() {
		return getProduct().getMaturityDate();
	}

	@Override
	public void setMaturityDate(LocalDate maturityDate) {
		// Forbidden to set the maturity date. The maturity date is defined by
		// the
		// product (it is the future maturity date).
	}
}