package org.eclipse.tradista.core.trade.messaging;

import org.eclipse.tradista.core.common.messaging.Event;
import org.eclipse.tradista.core.trade.model.Trade;

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

public abstract class TradeEvent<X extends Trade<?>> implements Event {

	private static final long serialVersionUID = -5889419872185649873L;

	private String appliedAction;

	private X trade;

	private X oldTrade;

	public X getTrade() {
		return trade;
	}

	public void setTrade(X trade) {
		this.trade = trade;
	}

	public X getOldTrade() {
		return oldTrade;
	}

	public void setOldTrade(X oldTrade) {
		this.oldTrade = oldTrade;
	}

	/**
	 * Return the last action applied to the object.
	 * 
	 * @return the last action applied to the object.
	 */
	public String getAppliedAction() {
		return appliedAction;
	}

	public void setAppliedAction(String action) {
		appliedAction = action;
	}

}