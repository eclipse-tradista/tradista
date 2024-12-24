package org.eclipse.tradista.security.equity.transfer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.Transfer.Direction;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.security.equity.model.Equity;
import org.eclipse.tradista.security.equity.model.EquityTrade;

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

public final class EquityTransferUtil {

	/**
	 * Returns the list of received dividends for a given equity trade.
	 * 
	 * @param equity trade the concerned equity trade
	 * @return the list of received dividends for a given trade
	 * @throws TradistaBusinessException If the trade or the equity is null, a
	 *                                   TradistaBusinessException is thrown
	 */
	public static List<CashTransfer> generateDividends(EquityTrade trade) throws TradistaBusinessException {
		if (trade == null) {
			throw new TradistaBusinessException("The trade is mandatory.");
		} else {
			if (trade.getProduct() == null) {
				throw new TradistaBusinessException(
						String.format("Trade %d has no equity. Equity is mandatory.%n", trade.getId()));
			} else {
				if (!trade.getProduct().isPayDividend()) {
					return null;
				}
			}
		}

		if (trade.isSell()) {
			return null;
		}

		Equity equity = trade.getProduct();

		Tenor frequency = equity.getDividendFrequency();
		List<CashTransfer> dividends = new ArrayList<CashTransfer>();

		LocalDate activeFrom = equity.getActiveFrom();

		LocalDate cashFlowDate = activeFrom;

		while (!cashFlowDate.isAfter(equity.getActiveTo())) {
			if (cashFlowDate.isAfter(activeFrom) && !cashFlowDate.isBefore(trade.getSettlementDate())) {
				CashTransfer cashTransfer = new CashTransfer(trade.getBook(), equity, TransferPurpose.DIVIDEND,
						cashFlowDate, equity.getDividendCurrency());
				cashTransfer.setCreationDateTime(LocalDateTime.now());
				cashTransfer.setFixingDateTime(cashFlowDate.atStartOfDay());
				cashTransfer.setStatus(Transfer.Status.UNKNOWN);
				cashTransfer.setDirection(Direction.RECEIVE);

				dividends.add(cashTransfer);
			}

			cashFlowDate = DateUtil.addTenor(cashFlowDate, frequency);
		}
		return dividends;
	}
}