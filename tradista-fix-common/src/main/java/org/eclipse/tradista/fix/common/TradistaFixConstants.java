package org.eclipse.tradista.fix.common;

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

public final class TradistaFixConstants {

	public static final int CONTRA_FIRM_PARTY_ROLE = 17;

	public static final int EXECUTING_FIRM_PARTY_ROLE = 1;

	public static final char BUY_SIDE = '1';

	public static final char SELL_SIDE = '2';

	public static final int RIGHT_OF_SUBSTITUTION_TAG = 9001;

	public static final int RIGHT_OF_REUSE_TAG = 9002;

	public static final int TERMINABLE_ON_DEMAND_TAG = 9003;

	public static final int NOTICE_PERIOD_TAG = 9004;

	public static final int REPO_RATE_TAG = 227;

	public static final String RIGHT_OF_SUBSTITUTION = "RightOfSubstitution";

	public static final String RIGHT_OF_REUSE = "RightOfReuse";

	public static final String TERMINABLE_ON_DEMAND = "TerminableOnDemand";

	public static final String NOTICE_PERIOD = "NoticePeriod";

	private TradistaFixConstants() {
	}
}