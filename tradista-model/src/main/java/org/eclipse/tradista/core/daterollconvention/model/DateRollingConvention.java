package org.eclipse.tradista.core.daterollconvention.model;

/********************************************************************************
 * Copyright (c) 2017 Olivier Asuncion
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

public enum DateRollingConvention {
	ACTUAL, FOLLOWING_BUSINESS_DAY, MODIFIED_FOLLOWING_BUSINESS_DAY, PREVIOUS_BUSINESS_DAY,
	MODIFIED_PREVIOUS_BUSINESS_DAY, MODIFIED_ROLLING_BUSINESS_DAY;

	public String toString() {
		switch (this) {
		case ACTUAL:
			return "Actual";
		case FOLLOWING_BUSINESS_DAY:
			return "Following Business Day";
		case MODIFIED_FOLLOWING_BUSINESS_DAY:
			return "Modified Following Business Day";
		case PREVIOUS_BUSINESS_DAY:
			return "Previous Business Day";
		case MODIFIED_PREVIOUS_BUSINESS_DAY:
			return "Modified Previous Business Day";
		case MODIFIED_ROLLING_BUSINESS_DAY:
			return "Modified Rolling Business Day";
		}
		return super.toString();
	}

	/**
	 * Gets a Date Rolling Convention from a display name. Display names are used in
	 * GUIs. A display name of a Date Rolling Convention is the result of its
	 * toString() method.
	 * 
	 * @param type
	 * @return
	 */
	public static DateRollingConvention getDateRollingConvention(String displayName) {
		switch (displayName) {
		case "Actual":
			return ACTUAL;
		case "Following Business Day":
			return FOLLOWING_BUSINESS_DAY;
		case "Modified Following Business Day":
			return MODIFIED_FOLLOWING_BUSINESS_DAY;
		case "Previous Business Day":
			return PREVIOUS_BUSINESS_DAY;
		case "Modified Previous Business Day":
			return MODIFIED_PREVIOUS_BUSINESS_DAY;
		case "Modified Rolling Business Day":
			return MODIFIED_ROLLING_BUSINESS_DAY;
		}
		return null;
	}

}