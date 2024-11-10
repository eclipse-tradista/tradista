package org.eclipse.tradista.security.equityoption.validator;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.marketdata.validator.DefaultQuoteValidator;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.security.equityoption.model.EquityOption;
import org.eclipse.tradista.security.equityoption.service.EquityOptionBusinessDelegate;

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

public class EquityOptionQuoteValidator extends DefaultQuoteValidator {

	@Override
	public void validateQuoteName(String quoteName) throws TradistaBusinessException {
		validateQuoteBasics(quoteName);
		StringBuilder errMsg = new StringBuilder();
		String[] data = quoteName.split("\\.");

		if (data.length != 6) {
			throw new TradistaBusinessException(String.format(
					"The quote name (%s) must be as follows: %s.contractSpecificationName.code.type.optionExpiry.strike",
					quoteName, EquityOption.EQUITY_OPTION));
		}

		OptionTrade.Type type = OptionTrade.Type.getType(data[3]);
		if (type == null) {
			throw new TradistaBusinessException(String.format("The equity option type must be a valid Option type: %s",
					Arrays.asList(OptionTrade.Type.values())));
		}

		LocalDate maturityDate;
		try {
			maturityDate = LocalDate.parse(data[4]);
		} catch (DateTimeParseException dtpe) {
			throw new TradistaBusinessException("The equity option maturity date must be a valid date.");
		}

		Number strike = null;
		try {
			ParsePosition position = new ParsePosition(0);
			DecimalFormat df = new DecimalFormat();
			DecimalFormatSymbols dfs = new DecimalFormatSymbols();
			dfs.setDecimalSeparator(',');
			df.setDecimalFormatSymbols(dfs);
			strike = df.parse(data[5], position);
			if (position.getIndex() != data[5].length()) {
				throw new ParseException("failed to parse entire string: " + data[5], position.getIndex());
			}
		} catch (ParseException pe) {
			throw new TradistaBusinessException("The equity option strike must be a valid decimal");
		}

		EquityOption eo = new EquityOptionBusinessDelegate()
				.getEquityOptionByCodeTypeStrikeMaturityDateAndContractSpecificationName(data[2], type,
						new BigDecimal(strike.toString()), maturityDate, data[1]);

		if (eo == null) {
			throw new TradistaBusinessException(String.format(
					"The equity option with contract %s, code %s, type %s, strike %s and maturity date %s must exist in the system.",
					data[1], data[2], type, strike, maturityDate));
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}