package org.eclipse.tradista.core.marketdata.model;

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

public enum QuoteType {
	BETA, BOND_PRICE, DELTA, DIVIDEND_YIELD, EQUITY_OPTION_PRICE, EQUITY_PRICE, VOLATILITY, INTEREST_RATE,
	EXCHANGE_RATE, FUTURE_PRICE;

	public String toString() {
		switch (this) {
		case BETA:
			return "Beta";
		case BOND_PRICE:
			return "BondPrice";
		case DELTA:
			return "Delta";
		case DIVIDEND_YIELD:
			return "DividendYield";
		case EQUITY_OPTION_PRICE:
			return "EquityOptionPrice";
		case EQUITY_PRICE:
			return "EquityPrice";
		case VOLATILITY:
			return "Volatility";
		case INTEREST_RATE:
			return "InterestRate";
		case EXCHANGE_RATE:
			return "ExchangeRate";
		case FUTURE_PRICE:
			return "FuturePrice";
		}
		return super.toString();
	}

	/**
	 * Gets a QuoteType from a display name. Display names are used in GUIs. A
	 * display name of a QuoteType is the result of its toString() method.
	 * 
	 * @param type
	 * @return
	 */
	public static QuoteType getQuoteType(String displayName) {
		switch (displayName) {
		case "Beta":
			return BETA;
		case "BondPrice":
			return BOND_PRICE;
		case "Delta":
			return DELTA;
		case "DividendYield":
			return DIVIDEND_YIELD;
		case "EquityOptionPrice":
			return EQUITY_OPTION_PRICE;
		case "EquityPrice":
			return EQUITY_PRICE;
		case "Volatility":
			return VOLATILITY;
		case "InterestRate":
			return INTEREST_RATE;
		case "FuturePrice":
			return FUTURE_PRICE;
		case "ExchangeRate":
			return EXCHANGE_RATE;
		}
		return null;
	}

}
