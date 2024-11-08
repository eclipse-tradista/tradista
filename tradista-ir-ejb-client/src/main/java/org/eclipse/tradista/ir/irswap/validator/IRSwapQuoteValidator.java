package org.eclipse.tradista.ir.irswap.validator;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.index.service.IndexBusinessDelegate;
import org.eclipse.tradista.core.marketdata.validator.DefaultQuoteValidator;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.ir.irswap.model.IRSwapTrade;

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

public class IRSwapQuoteValidator extends DefaultQuoteValidator {

	@Override
	public void validateQuoteName(String quoteName) throws TradistaBusinessException {
		validateQuoteBasics(quoteName);
		StringBuilder errMsg = new StringBuilder();
		String[] data = quoteName.split("\\.");

		if (data.length < 3) {
			throw new TradistaBusinessException(String.format(
					"The quote name (%s) must be as follows: %s.Index.Maturity%n", quoteName, IRSwapTrade.IR_SWAP));
		}

		if (!data[0].equals(IRSwapTrade.IR_SWAP)) {
			errMsg.append(String.format("The quote name (%s) must start with %s.%n", quoteName, IRSwapTrade.IR_SWAP));
		}
		if (new IndexBusinessDelegate().getIndexByName(data[1]) == null) {
			errMsg.append(String.format("The Index (%s) must exist in the system.%n.", data[1]));
		}
		if (Tenor.valueOf(data[2]) == null) {
			errMsg.append(
					String.format("The swap maturity (%s) must be a valid Tenor: %s%n.", data[2], Tenor.values()));
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}