package org.eclipse.tradista.ir.ircap.transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.marketdata.model.QuoteType;
import org.eclipse.tradista.core.marketdata.model.QuoteValue;
import org.eclipse.tradista.core.pricing.util.PricerUtil;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.FixingError;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.ir.ircapfloorcollar.model.IRCapFloorCollarTrade;
import org.eclipse.tradista.ir.ircapfloorcollar.transfer.IRCapFloorCollarTransferManager;

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

public class IRCapTransferManager extends IRCapFloorCollarTransferManager {

	protected ConfigurationBusinessDelegate configurationBusinessDelegate;

	public IRCapTransferManager() {
		configurationBusinessDelegate = new ConfigurationBusinessDelegate();
	}

	@Override
	public void fixCashTransfer(CashTransfer transfer, long quoteSetId) throws TradistaBusinessException {
		IRCapFloorCollarTrade trade = (IRCapFloorCollarTrade) transfer.getTrade();
		BigDecimal notional = trade.getIrForwardTrade().getAmount();
		String quoteName = Index.INDEX + "." + trade.getIrForwardTrade().getReferenceRateIndex() + "."
				+ trade.getIrForwardTrade().getReferenceRateIndexTenor();
		BigDecimal ir = PricerUtil.getValueAsOfDateFromQuote(quoteName, quoteSetId, QuoteType.INTEREST_RATE,
				QuoteValue.CLOSE, transfer.getFixingDateTime().toLocalDate());
		BigDecimal difference;
		BigDecimal amount;
		BigDecimal fractionedNotional;
		if (ir == null) {
			FixingError fixingError = new FixingError();
			fixingError.setCashTransfer(transfer);
			fixingError.setErrorDate(LocalDateTime.now());
			String errorMsg = String.format(
					"Transfer %n cannot be fixed. Impossible to get the %s index closing value as of %tD in QuoteSet %s.",
					transfer.getId(), quoteName, transfer.getFixingDateTime(), quoteSetId);
			fixingError.setMessage(errorMsg);
			fixingError.setStatus(org.eclipse.tradista.core.error.model.Error.Status.UNSOLVED);
			List<FixingError> errors = new ArrayList<FixingError>(1);
			errors.add(fixingError);
			fixingErrorBusinessDelegate.saveFixingErrors(errors);
			throw new TradistaBusinessException(errorMsg);
		}
		if (!(ir.compareTo(trade.getCapStrike()) > 0)) {
			// No transfer
			transferBusinessDelegate.deleteTransfer(transfer.getId());
			return;
			// TODO add a warn somewhere ?
		}
		fractionedNotional = notional.multiply(PricerUtil.daysToYear(trade.getIrForwardTrade().getDayCountConvention(),
				transfer.getFixingDateTime().toLocalDate(),
				DateUtil.addTenor(transfer.getFixingDateTime().toLocalDate(),
						trade.getIrForwardTrade().getReferenceRateIndexTenor())));
		difference = ir.subtract(trade.getCapStrike());
		amount = fractionedNotional.multiply(difference.divide(BigDecimal.valueOf(100),
				configurationBusinessDelegate.getScale(), configurationBusinessDelegate.getRoundingMode()));
		Transfer.Direction direction;
		if (trade.isBuy()) {
			direction = Transfer.Direction.RECEIVE;
		} else {
			direction = Transfer.Direction.PAY;
		}
		transfer.setDirection(direction);
		transfer.setAmount(amount);
		transferBusinessDelegate.saveTransfer(transfer);
	}

}