package org.eclipse.tradista.ir.irswapoption.validator;

import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.marketdata.validator.DefaultQuoteValidator;
import org.eclipse.tradista.ir.irswapoption.model.IRSwapOptionTrade;
import org.eclipse.tradista.ir.irswapoption.service.SwaptionVolatilitySurfaceBusinessDelegate;

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

public class IRSwapOptionQuoteValidator extends DefaultQuoteValidator {

	@Override
	public void validateQuoteName(String quoteName) throws TradistaBusinessException {
		validateQuoteBasics(quoteName);
		StringBuilder errMsg = new StringBuilder();
		String[] data = quoteName.split("\\.");

		if (data.length < 5) {
			throw new TradistaBusinessException(String.format(
					"The quote name (%s) must be as follows: IRSwapOption.IRCurveName.frequency.swapMaturity.optionExpiry.%n",
					quoteName));
		}

		if (!data[0].equals(IRSwapOptionTrade.IR_SWAP_OPTION)) {
			errMsg.append(String.format("The quote name (%s) must start with %s.%n", quoteName,
					IRSwapOptionTrade.IR_SWAP_OPTION));
		}
		Set<String> maturities = new SwaptionVolatilitySurfaceBusinessDelegate().getAllSwapMaturitiesAsString();
		if (!maturities.contains(data[3])) {
			errMsg.append(String.format("The swap maturity (%s) must be a valid one: %s%n.", data[3], maturities));
		}
		Set<String> expiries = new SwaptionVolatilitySurfaceBusinessDelegate().getAllOptionExpiriesAsString();
		if (!expiries.contains(data[4])) {
			errMsg.append(String.format("The option expiry (%s) must be a valid one: %s%n.", data[4], expiries));
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}
