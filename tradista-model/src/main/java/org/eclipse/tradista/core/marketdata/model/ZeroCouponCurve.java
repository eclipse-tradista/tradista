package org.eclipse.tradista.core.marketdata.model;

import org.eclipse.tradista.core.legalentity.model.LegalEntity;

/********************************************************************************
 * Copyright (c) 2014 Olivier Asuncion
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

public class ZeroCouponCurve extends InterestRateCurve {

	private static final long serialVersionUID = -965571671567710336L;

	public static final String ZERO_COUPON_CURVE = "ZeroCouponCurve";

	public ZeroCouponCurve(String name, LegalEntity po) {
		super(name, po);
	}

	public String getType() {
		return ZERO_COUPON_CURVE;
	}

}