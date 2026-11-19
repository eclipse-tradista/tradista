package org.eclipse.tradista.commercial.commercial.service;

import org.eclipse.tradista.commercial.commercial.model.CommercialTrade;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;

/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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

public class CommercialTradeBusinessDelegate {

	private CommercialTradeService commercialTradeService;

	private CommercialTradeValidator validator;

	public CommercialTradeBusinessDelegate() {
		commercialTradeService = TradistaServiceLocator.getInstance().getCommercialTradeService();
		validator = new CommercialTradeValidator();
	}

	public long saveCommercialTrade(CommercialTrade trade, String action) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> commercialTradeService.saveCommercialTrade(trade, action));
	}

	public CommercialTrade getCommercialTradeById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The trade id must be positive.");
		}
		return SecurityUtil.run(() -> commercialTradeService.getCommercialTradeById(id));
	}

}