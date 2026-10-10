package org.eclipse.tradista.security.bond.transfer;

import static org.eclipse.tradista.core.common.util.TradistaConstants.FIXED;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.security.bond.model.Bond;
import org.eclipse.tradista.security.bond.model.BondTrade;

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

public final class BondTransferUtil {

	private BondTransferUtil() {
	}

	/**
	 * @param trade the bond trade for which we want to generate the payments.
	 * @throws TradistaBusinessException
	 */
	public static List<CashTransfer> generateCashSettlements(BondTrade trade) throws TradistaBusinessException {
		if (trade == null) {
			throw new TradistaBusinessException("The trade is mandatory.");
		}

		Bond bond = trade.getProduct();

		Tenor frequency = bond.getCouponFrequency();
		List<CashTransfer> cts = new ArrayList<>();
		LocalDate datedDate = bond.getDatedDate();
		LocalDate couponDate = trade.getSettlementDate();

		CashTransfer notionalPaid = CashTransfer
				.builder(trade.getBook(), TransferPurpose.BOND_PAYMENT, trade.getSettlementDate(), bond.getCurrency())
				.trade(trade).direction(trade.isBuy() ? Transfer.Direction.PAY : Transfer.Direction.RECEIVE)
				.quantityOrAmount(trade.getAmount().multiply(trade.getQuantity()))
				.fixingDateTime(LocalDate.ofInstant(trade.getCreationTime(), ZoneId.systemDefault()).atStartOfDay())
				.status(Transfer.Status.KNOWN).build();
		cts.add(notionalPaid);

		// We try to calculate the coupons only if the bond is not a ZC
		if (!bond.getCouponFrequency().equals(Tenor.NO_TENOR)) {
			while (!couponDate.isAfter(bond.getMaturityDate())) {
				if (couponDate.isAfter(datedDate)) {
					CashTransfer.Builder couponBuilder = CashTransfer
							.builder(trade.getBook(), bond, TransferPurpose.COUPON, couponDate, bond.getCurrency())
							.trade(trade);
					if (bond.getCouponType().equals(FIXED)) {
						couponBuilder.direction(Transfer.Direction.RECEIVE);
						couponBuilder.quantityOrAmount(trade.getQuantity().multiply(
								bond.getPrincipal().multiply(bond.getCoupon().divide(BigDecimal.valueOf(100)))));
						couponBuilder.fixingDateTime(bond.getIssueDate().atStartOfDay());
						couponBuilder.status(Transfer.Status.KNOWN);
					} else {
						couponBuilder.fixingDateTime(couponDate.atStartOfDay());
						couponBuilder.status(Transfer.Status.UNKNOWN);
					}
					cts.add(couponBuilder.build());
				}

				couponDate = DateUtil.addTenor(couponDate, frequency);
			}
		}

		CashTransfer notionalPaidBack = CashTransfer
				.builder(trade.getBook(), TransferPurpose.NOTIONAL_REPAYMENT, bond.getMaturityDate(),
						bond.getCurrency())
				.trade(trade).direction(Transfer.Direction.RECEIVE)
				.quantityOrAmount(bond.getPrincipal().multiply(trade.getQuantity()))
				.fixingDateTime(bond.getIssueDate().atStartOfDay()).status(Transfer.Status.KNOWN).build();
		cts.add(notionalPaidBack);

		return cts;
	}

}