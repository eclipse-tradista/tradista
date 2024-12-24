package org.eclipse.tradista.core.daycountconvention.model;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaObject;

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

public class DayCountConvention extends TradistaObject implements Comparable<DayCountConvention> {

	private static final long serialVersionUID = -3809902839449814333L;

	public static final String ACT_365 = "ACT/365";

	public static final String ACT_360 = "ACT/360";

	@Id
	private String name;

	public DayCountConvention(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int compareTo(DayCountConvention dcc) {
		return name.compareTo(dcc.getName());
	}

}