package org.eclipse.tradista.ir.future.service;

import java.time.LocalDate;
import java.util.List;

import jakarta.ejb.Remote;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.ir.future.model.FutureTrade;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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

@Remote
public interface FutureTradeService {

	long saveFutureTrade(FutureTrade trade) throws TradistaBusinessException;

	List<FutureTrade> getFutureTradesBeforeTradeDateByFutureAndBookIds(LocalDate date, long futureId, long bookId);

	FutureTrade getFutureTradeById(long id);

}
