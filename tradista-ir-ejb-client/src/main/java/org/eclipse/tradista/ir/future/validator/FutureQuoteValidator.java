package org.eclipse.tradista.ir.future.validator;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.marketdata.validator.DefaultQuoteValidator;
import org.eclipse.tradista.ir.future.model.Future;
import org.eclipse.tradista.ir.future.model.FutureContractSpecification;
import org.eclipse.tradista.ir.future.service.FutureBusinessDelegate;
import org.eclipse.tradista.ir.future.service.FutureContractSpecificationBusinessDelegate;

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

public class FutureQuoteValidator extends DefaultQuoteValidator {

	@Override
	public void validateQuoteName(String quoteName) throws TradistaBusinessException {
		validateQuoteBasics(quoteName);
		StringBuilder errMsg = new StringBuilder();
		String[] data = quoteName.split("\\.");

		if (data.length < 3) {
			throw new TradistaBusinessException(
					String.format("The quote name (%s) must be as follows: %s.FutureContractSpecfication.Symbol",
							quoteName, Future.FUTURE));
		}

		if (!data[0].equals(Future.FUTURE)) {
			errMsg.append(String.format("The quote name (%s) must start with %s.%n", quoteName, Future.FUTURE));
		}
		FutureContractSpecification fcs = new FutureContractSpecificationBusinessDelegate()
				.getFutureContractSpecificationByName(data[1]);
		if (fcs == null) {
			errMsg.append(String.format("The future contract specification %s must exist in the system.%n", data[1]));
		}
		if (!new FutureBusinessDelegate().isValidSymbol(data[2])) {
			errMsg.append(String.format("The future symbol %s must be a valid one (MMMYY).%n", data[2]));
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}