package org.eclipse.tradista.core.error.util;

import java.time.LocalDate;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.util.DateUtil;

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

public final class ErrorUtil {

	private ErrorUtil() {
	}

	public static void checkErrorDates(LocalDate errorDateFrom, LocalDate errorDateTo, LocalDate solvingDateFrom,
			LocalDate solvingDateTo, StringBuilder errMsg) {
		if (errMsg == null) {
            throw new TradistaTechnicalException("StringBuilder for error messages cannot be null");
        }
		DateUtil.checkNotAfter(errorDateFrom, errorDateTo, "Error Date From", "Error Date To",
				errMsg);
		DateUtil.checkNotAfter(solvingDateFrom, solvingDateTo, "Solving Date From", "Solving Date To",
				errMsg);
	}

	public static void checkErrorDates(LocalDate errorDateFrom, LocalDate errorDateTo, LocalDate solvingDateFrom,
			LocalDate solvingDateTo) throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		checkErrorDates(errorDateFrom, errorDateTo, solvingDateFrom, solvingDateTo, errMsg);
		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}