package org.eclipse.tradista.core.tenor.model;

/********************************************************************************
 * Copyright (c) 2016 Olivier Asuncion
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

public enum Tenor {
	NO_TENOR, ONE_MONTH, TWO_MONTHS, THREE_MONTHS, FOUR_MONTHS, FIVE_MONTHS, SIX_MONTHS, ONE_YEAR, EIGHTEEN_MONTHS,
	TWO_YEARS;

	public String toString() {
		switch (this) {
		case NO_TENOR:
			return "No Tenor";
		case ONE_MONTH:
			return "1M";
		case TWO_MONTHS:
			return "2M";
		case THREE_MONTHS:
			return "3M";
		case FOUR_MONTHS:
			return "4M";
		case FIVE_MONTHS:
			return "5M";
		case SIX_MONTHS:
			return "6M";
		case ONE_YEAR:
			return "1Y";
		case EIGHTEEN_MONTHS:
			return "18M";
		case TWO_YEARS:
			return "2Y";
		}
		return super.toString();
	}

	/**
	 * Gets a Tenor from a display name. Display names are used in GUIs. A display
	 * name of a Tenor is the result of its toString() method.
	 * 
	 * @param type
	 * @return
	 */
	public static Tenor getTenor(String displayName) {
		switch (displayName) {
		case "1M":
			return ONE_MONTH;
		case "2M":
			return TWO_MONTHS;
		case "3M":
			return THREE_MONTHS;
		case "4M":
			return FOUR_MONTHS;
		case "5M":
			return FIVE_MONTHS;
		case "6M":
			return SIX_MONTHS;
		case "1Y":
			return ONE_YEAR;
		case "18M":
			return EIGHTEEN_MONTHS;
		case "2Y":
			return TWO_YEARS;
		}
		return null;
	}

}
