package org.eclipse.tradista.ir.fra.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.marketdata.model.Instrument;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.ir.irforward.model.IRForwardTrade;

/********************************************************************************
 * Copyright (c) 2014 Olivier Asuncion
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

public class FRATrade extends IRForwardTrade<Product> implements Instrument {

	private static final long serialVersionUID = 7130145836929238705L;

	private BigDecimal fixedRate;

	private LocalDate startDate;

	public BigDecimal getFixedRate() {
		return fixedRate;
	}

	public void setFixedRate(BigDecimal fixedRate) {
		this.fixedRate = fixedRate;
	}

	public static final String FRA = "FRA";

	public String getProductType() {
		return FRA;
	}

	@Override
	public Tenor getFrequency() {
		return Tenor.NO_TENOR;
	}

	@Override
	public void setFrequency(Tenor frequency) {
		// not allowed to modify the frequency for FRA..
	}

	/*
	 * Payment in a FRA is done at the maturity date of the trade that is also the
	 * beginning of the period considered for the interest calculation (period also
	 * known as 'Contract period')
	 */
	@Override
	public InterestPayment getInterestPayment() {
		return InterestPayment.BEGINNING_OF_PERIOD;
	}

	@Override
	public void setInterestPayment(InterestPayment interestPayment) {
		// not allowed to modify the interest payment for FRA..
	}

	@Override
	public String getInstrumentName() {
		return FRA;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return getMaturityDate();
	}

	public void setEndDate(LocalDate endDate) {
		setMaturityDate(endDate);
	}

	public LocalDate getPaymentDate() {
		return getSettlementDate();
	}

	public void setPaymentDate(LocalDate paymentDate) {
		setSettlementDate(paymentDate);
	}

}