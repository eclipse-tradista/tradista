package org.eclipse.tradista.core.interestpayment.model;

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

public enum InterestPayment {
	BEGINNING_OF_PERIOD, END_OF_PERIOD;

	public String toString() {
		switch (this) {
		case BEGINNING_OF_PERIOD:
			return "Beginning of period";
		case END_OF_PERIOD:
			return "End of period";
		}
		return super.toString();
	}

	/**
	 * Gets an Interest Payment from a display name. Display names are used in GUIs.
	 * A display name of an Interest Payment is the result of its toString() method.
	 * 
	 * @param type
	 * @return
	 */
	public static InterestPayment getInterestPayment(String displayName) {
		switch (displayName) {
		case "Beginning of period":
			return BEGINNING_OF_PERIOD;
		case "End of period":
			return END_OF_PERIOD;
		}
		return null;
	}

}