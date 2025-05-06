package org.eclipse.tradista.fix.common;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.service.CurrencyBusinessDelegate;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;

import quickfix.FieldMap;
import quickfix.FieldNotFound;

/********************************************************************************
 * Copyright (c) 2025 Olivier Asuncion
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

public final class TradistaFixUtil {

	public static final Pattern DATE_REGEX = Pattern.compile("^(\\d{4})(0[1-9]|1[0-2])(0[1-9]|1\\d|2\\d|3[01])$");
	public static final Pattern YES_NO_REGEX = Pattern.compile("^[YN]$");
	public static final Pattern NUMBER_OF_DAYS_REGEX = Pattern.compile("^\\d+D$");
	public static final Pattern AMOUNT_REGEX = Pattern.compile("^-?\\d+(\\.\\d+)?$");

	private static CurrencyBusinessDelegate currencyBusinessDelegate;

	private TradistaFixUtil() {
		currencyBusinessDelegate = new CurrencyBusinessDelegate();
	}

	public static void checkFixDate(FieldMap fieldMap, int tag, String fieldName, boolean isMandatory,
			StringBuilder errMsg) {
		checkFixField(fieldMap, tag, fieldName, DATE_REGEX, isMandatory, errMsg);
	}

	public static void checkFixAmount(FieldMap fieldMap, int tag, String fieldName, boolean isMandatory,
			StringBuilder errMsg) {
		checkFixField(fieldMap, tag, fieldName, AMOUNT_REGEX, isMandatory, errMsg);
	}

	public static void checkFixField(FieldMap fieldMap, int tag, String fieldName, Pattern pattern, boolean isMandatory,
			StringBuilder errMsg) {
		StringBuilder callErrMsg = new StringBuilder();
		if (fieldMap == null) {
			callErrMsg.append(
					String.format("TradistaFixUtil#checkFixField is not called properly, fieldMap is mandatory.%n"));
		}
		if (tag <= 0) {
			callErrMsg.append(String
					.format("TradistaFixUtil#checkFixField is not called properly, tag (%d) must be positive.%n", tag));
		}
		if (StringUtils.isEmpty(fieldName)) {
			callErrMsg.append(
					String.format("TradistaFixUtil#checkFixField is not called properly, fieldName is mandatory.%n"));
		}
		if (errMsg == null) {
			callErrMsg.append(
					String.format("TradistaFixUtil#checkFixField is not called properly, errMsg is mandatory.%n"));
		}
		if (pattern == null) {
			callErrMsg.append("TradistaFixUtil#checkFixField is not called properly, pattern is mandatory.");
		}
		if (!callErrMsg.isEmpty()) {
			throw new TradistaTechnicalException(callErrMsg.toString());
		}

		if (!fieldMap.isSetField(tag)) {
			if (isMandatory) {
				errMsg.append(String.format("%s field is mandatory.%n", fieldName));
			}
		} else {
			boolean check;
			try {
				check = pattern.matcher(fieldMap.getString(tag)).matches();
				if (!check) {
					errMsg.append(
							String.format("%s field should match this regex: %s.%n", fieldName, pattern.pattern()));
				}
			} catch (FieldNotFound fnfe) {
				// Not expected here.
			}
		}
	}

	public static LocalDate parseFixDate(FieldMap fieldMap, int tag) {
		LocalDate date = null;
		try {
			date = LocalDate.parse(fieldMap.getString(tag), DateTimeFormatter.ofPattern("yyyyMMdd"));
		} catch (FieldNotFound fnfe) {
			throw new TradistaTechnicalException(String.format("Field %d cannot be parsed as it is empty.", tag));
		}
		return date;
	}

	public static BigDecimal parseFixAmount(FieldMap fieldMap, int tag) {
		BigDecimal amount = null;
		try {
			amount = new BigDecimal(fieldMap.getString(tag));
		} catch (FieldNotFound fnfe) {
			throw new TradistaTechnicalException(String.format("Field %d cannot be parsed as it is empty.", tag));
		}
		return amount;
	}

	public static Currency parseFixCurrency(FieldMap fieldMap, int tag) throws TradistaBusinessException {
		Currency currency = null;
		try {
			currency = currencyBusinessDelegate.getCurrencyByIsoCode(fieldMap.getString(tag));
		} catch (FieldNotFound fnfe) {
			throw new TradistaTechnicalException(String.format("Field %d cannot be parsed as it is empty.", tag));
		}
		return currency;
	}

	public static LegalEntity parseFixLegalEntity(FieldMap fieldMap, int field) {
		// TODO To complete
		return null;
	}
}