package org.eclipse.tradista.ir.irswap.model;

import org.eclipse.tradista.core.marketdata.model.Instrument;

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

public class SingleCurrencyIRSwapTrade extends IRSwapTrade implements Instrument {

	private static final long serialVersionUID = -6188291608649255466L;

	public static final String IR_SWAP = "IRSwap";

	public String getProductType() {
		return IR_SWAP;
	}

	@Override
	public String getInstrumentName() {
		return IR_SWAP;
	}

}
