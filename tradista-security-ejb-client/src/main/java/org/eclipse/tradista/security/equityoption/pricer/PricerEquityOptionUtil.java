package org.eclipse.tradista.security.equityoption.pricer;

import java.math.BigDecimal;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.security.equityoption.model.EquityOptionTrade;

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

public final class PricerEquityOptionUtil {

	public static BigDecimal getProfitAfterExercice(EquityOptionTrade equityOptionTrade)
			throws TradistaBusinessException {

		if (equityOptionTrade == null) {
			throw new TradistaBusinessException("The equity Option trade is mandatory.");
		}

		if (equityOptionTrade.getExerciseDate() == null) {
			// Log warn
			return BigDecimal.valueOf(0);
		}

		if (equityOptionTrade.isCall()) {
			if (equityOptionTrade.getEquityOption() != null) {
				return equityOptionTrade.getStrike().multiply(equityOptionTrade.getEquityOption().getQuantity())
						.negate();
			}
			return equityOptionTrade.getStrike().negate();
		}

		if (equityOptionTrade.isPut()) {
			if (equityOptionTrade.getEquityOption() != null) {
				return equityOptionTrade.getStrike().multiply(equityOptionTrade.getEquityOption().getQuantity());
			}
			return equityOptionTrade.getStrike();
		}

		return null;
	}

}