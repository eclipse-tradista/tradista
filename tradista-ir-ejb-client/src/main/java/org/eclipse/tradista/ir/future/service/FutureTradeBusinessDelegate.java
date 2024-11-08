package org.eclipse.tradista.ir.future.service;

import java.time.LocalDate;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.ir.future.model.FutureTrade;
import org.eclipse.tradista.ir.future.service.FutureTradeService;
import org.eclipse.tradista.ir.future.validator.FutureTradeValidator;

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

public class FutureTradeBusinessDelegate {

	private FutureTradeService futureTradeService;

	private FutureTradeValidator validator;

	public FutureTradeBusinessDelegate() {
		futureTradeService = TradistaServiceLocator.getInstance().getFutureTradeService();
		validator = new FutureTradeValidator();
	}

	public long saveFutureTrade(FutureTrade trade) throws TradistaBusinessException {
		validator.validateTrade(trade);
		return SecurityUtil.runEx(() -> futureTradeService.saveFutureTrade(trade));
	}

	public List<FutureTrade> getFutureTradesBeforeTradeDateByFutureAndBookIds(LocalDate date, long futureId,
			long bookId) throws TradistaBusinessException {
		if (date == null) {
			throw new TradistaBusinessException("The date is mandatory.");
		}
		return SecurityUtil
				.run(() -> futureTradeService.getFutureTradesBeforeTradeDateByFutureAndBookIds(date, futureId, bookId));
	}

	public FutureTrade getFutureTradeById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The trade id must be positive.");
		}
		return SecurityUtil.run(() -> futureTradeService.getFutureTradeById(id));
	}

}