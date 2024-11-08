package org.eclipse.tradista.ir.future.model;

import java.time.LocalDate;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.daycountconvention.model.DayCountConvention;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.tenor.model.Tenor;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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

public class Future extends Product {

	private static final long serialVersionUID = 328506381431797478L;

	@Id
	private String symbol;

	@Id
	private FutureContractSpecification contractSpecification;

	public Future(String symbol, FutureContractSpecification futureContractSpecification) {
		super(futureContractSpecification != null ? futureContractSpecification.getExchange() : null);
		this.symbol = symbol;
		this.contractSpecification = futureContractSpecification;
	}

	private LocalDate maturityDate;

	public String getSymbol() {
		return symbol;
	}

	public FutureContractSpecification getContractSpecification() {
		return TradistaModelUtil.clone(contractSpecification);
	}

	public LocalDate getMaturityDate() {
		return maturityDate;
	}

	public void setMaturityDate(LocalDate maturityDate) {
		this.maturityDate = maturityDate;
	}

	public static final String FUTURE = "Future";

	@Override
	public String getProductType() {
		return FUTURE;
	}

	@Override
	public String toString() {
		return symbol + " - " + contractSpecification;
	}

	public DayCountConvention getDayCountConvention() {
		if (contractSpecification != null) {
			return contractSpecification.getDayCountConvention();
		}
		return null;
	}

	public Index getReferenceRateIndex() {
		if (contractSpecification != null) {
			return contractSpecification.getReferenceRateIndex();
		}
		return null;
	}

	public Tenor getReferenceRateIndexTenor() {
		if (contractSpecification != null) {
			return contractSpecification.getReferenceRateIndexTenor();
		}
		return null;
	}

	public Currency getCurrency() {
		if (contractSpecification != null) {
			return contractSpecification.getCurrency();
		}
		return null;
	}

	public Exchange getExchange() {
		if (contractSpecification != null) {
			return contractSpecification.getExchange();
		}
		return null;
	}

	@Override
	public Future clone() {
		Future future = (Future) super.clone();
		future.contractSpecification = TradistaModelUtil.clone(contractSpecification);
		return future;
	}

}