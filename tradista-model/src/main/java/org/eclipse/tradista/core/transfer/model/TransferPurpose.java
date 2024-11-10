package org.eclipse.tradista.core.transfer.model;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
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

public enum TransferPurpose {

	INTEREST_PAYMENT, PRIMARY_CURRENCY, QUOTE_CURRENCY, CASH_SETTLEMENT, PREMIUM, PRIMARY_CURRENCY_SPOT,
	QUOTE_CURRENCY_SPOT, PRIMARY_CURRENCY_FORWARD, QUOTE_CURRENCY_FORWARD, FIXED_LEG_INTEREST_PAYMENT,
	FIXED_LEG_NOTIONAL_PAYMENT, FIXED_LEG_NOTIONAL_REPAYMENT, FLOATING_LEG_INTEREST_PAYMENT,
	FLOATING_LEG_NOTIONAL_PAYMENT, FLOATING_LEG_NOTIONAL_REPAYMENT, BOND_SETTLEMENT, COUPON, BOND_PAYMENT,
	NOTIONAL_PAYMENT, NOTIONAL_REPAYMENT, DIVIDEND, EQUITY_SETTLEMENT, EQUITY_PAYMENT, EQUITY_OPTION_SETTLEMENT,
	FUTURE_SETTLEMENT, COLLATERAL_SETTLEMENT, RETURNED_CASH_PLUS_INTEREST, RETURNED_COLLATERAL;

	public String toString() {
		switch (this) {
		case INTEREST_PAYMENT:
			return "Interest Payment";
		case PRIMARY_CURRENCY:
			return "Primary Currency";
		case QUOTE_CURRENCY:
			return "Quote Currency";
		case CASH_SETTLEMENT:
			return "Cash Settlement";
		case PREMIUM:
			return "Premium";
		case PRIMARY_CURRENCY_SPOT:
			return "Primary Currency Spot";
		case QUOTE_CURRENCY_SPOT:
			return "Quote Currency Spot";
		case PRIMARY_CURRENCY_FORWARD:
			return "Primary Currency Forward";
		case QUOTE_CURRENCY_FORWARD:
			return "Quote Currency Forward";
		case FIXED_LEG_INTEREST_PAYMENT:
			return "Fixed Leg Interest Payment";
		case FIXED_LEG_NOTIONAL_PAYMENT:
			return "Fixed Leg Notional Payment";
		case FIXED_LEG_NOTIONAL_REPAYMENT:
			return "Fixed Leg Notional Repayment";
		case FLOATING_LEG_INTEREST_PAYMENT:
			return "Floating Leg Interest Payment";
		case FLOATING_LEG_NOTIONAL_PAYMENT:
			return "Floating Leg Notional Payment";
		case FLOATING_LEG_NOTIONAL_REPAYMENT:
			return "Floating Leg Notional Repayment";
		case BOND_SETTLEMENT:
			return "Bond Settlement";
		case COUPON:
			return "Coupon";
		case BOND_PAYMENT:
			return "Bond Payment";
		case NOTIONAL_PAYMENT:
			return "Notional Payment";
		case NOTIONAL_REPAYMENT:
			return "Notional Repayment";
		case DIVIDEND:
			return "Dividend";
		case EQUITY_SETTLEMENT:
			return "Equity Settlement";
		case EQUITY_PAYMENT:
			return "Equity Payment";
		case EQUITY_OPTION_SETTLEMENT:
			return "Equity Option Settlement";
		case FUTURE_SETTLEMENT:
			return "Future Settlement";
		case COLLATERAL_SETTLEMENT:
			return "Collateral Settlement";
		case RETURNED_CASH_PLUS_INTEREST:
			return "Returned Cash Plus Interest";
		case RETURNED_COLLATERAL:
			return "Returned Collateral";
		}
		return super.toString();
	}

	public static TransferPurpose getTransferPurpose(String displayValue) {
		switch (displayValue) {
		case "Interest Payment":
			return INTEREST_PAYMENT;
		case "Primary Currency":
			return PRIMARY_CURRENCY;
		case "Quote Currency":
			return QUOTE_CURRENCY;
		case "Cash Settlement":
			return CASH_SETTLEMENT;
		case "Premium":
			return PREMIUM;
		case "Primary Currency Spot":
			return PRIMARY_CURRENCY_SPOT;
		case "Quote Currency Spot":
			return QUOTE_CURRENCY_SPOT;
		case "Primary Currency Forward":
			return PRIMARY_CURRENCY_FORWARD;
		case "Quote Currency Forward":
			return QUOTE_CURRENCY_FORWARD;
		case "Fixed Leg Interest Payment":
			return FIXED_LEG_INTEREST_PAYMENT;
		case "Fixed Leg Notional Payment":
			return FIXED_LEG_NOTIONAL_PAYMENT;
		case "Fixed Leg Notional Repayment":
			return FIXED_LEG_NOTIONAL_REPAYMENT;
		case "Floating Leg Interest Payment":
			return FLOATING_LEG_INTEREST_PAYMENT;
		case "Floating Leg Notional Payment":
			return FLOATING_LEG_NOTIONAL_PAYMENT;
		case "Floating Leg Notional Repayment":
			return FLOATING_LEG_NOTIONAL_REPAYMENT;
		case "Bond Settlement":
			return BOND_SETTLEMENT;
		case "Coupon":
			return COUPON;
		case "Bond Payment":
			return BOND_PAYMENT;
		case "Notional Payment":
			return NOTIONAL_PAYMENT;
		case "Notional Repayment":
			return NOTIONAL_REPAYMENT;
		case "Dividend":
			return DIVIDEND;
		case "Equity Settlement":
			return EQUITY_SETTLEMENT;
		case "Equity Payment":
			return EQUITY_PAYMENT;
		case "Equity Option Settlement":
			return EQUITY_OPTION_SETTLEMENT;
		case "Future Settlement":
			return FUTURE_SETTLEMENT;
		case "Collateral Settlement":
			return COLLATERAL_SETTLEMENT;
		case "Returned Cash Plus Interest":
			return RETURNED_CASH_PLUS_INTEREST;
		case "Returned Collateral":
			return RETURNED_COLLATERAL;
		}
		return null;
	}
}