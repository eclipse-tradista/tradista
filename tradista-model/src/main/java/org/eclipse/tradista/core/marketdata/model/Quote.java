package org.eclipse.tradista.core.marketdata.model;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaObject;

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

public class Quote extends TradistaObject implements MarketData {

	private static final long serialVersionUID = 8868004835569574694L;

	@Id
	private String name;

	@Id
	private QuoteType type;

	public Quote(long id, String name, QuoteType type) {
		super(id);
		this.name = name;
		this.type = type;
	}

	public Quote(String name, QuoteType type) {
		super();
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public QuoteType getType() {
		return type;
	}

	public String toString() {
		return this.getName();
	}

}