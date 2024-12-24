package org.eclipse.tradista.core.marketdata.validator;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.marketdata.model.InterestRateCurve;

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

public class DefaultZeroCouponCurveValidator extends DefaultInterestRateCurveValidator {

	public void validateCurve(InterestRateCurve curve) throws TradistaBusinessException {
		super.validateCurve(curve);
		StringBuilder errMsg = new StringBuilder();
		if (curve.isGenerated()) {
			if (curve.getAlgorithm() == null) {
				errMsg.append(String.format("The algorithm is mandatory when the curve is generated.%n"));
			}
			if (curve.getInterpolator() == null) {
				errMsg.append(String.format("The interpolator is mandatory when the curve is generated.%n"));
			}
			if (curve.getInstance() == null) {
				errMsg.append(String.format("The instance is mandatory when the curve is generated.%n"));
			}
			if (curve.getQuoteDate() == null) {
				errMsg.append(String.format("The quote date is mandatory when the curve is generated.%n"));
			}
			if (curve.getQuoteSet() == null) {
				errMsg.append(String.format("The quote set is mandatory when the curve is generated.%n"));
			}
			if (curve.getQuotes() == null || curve.getQuotes().isEmpty()) {
				errMsg.append(String.format("At least one quote must be selected when the curve is generated.%n"));
			}
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

}
