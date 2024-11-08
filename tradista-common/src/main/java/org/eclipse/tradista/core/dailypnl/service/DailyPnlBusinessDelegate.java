package org.eclipse.tradista.core.dailypnl.service;

import java.time.LocalDate;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.dailypnl.model.DailyPnl;
import org.eclipse.tradista.core.dailypnl.service.DailyPnlService;

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

public class DailyPnlBusinessDelegate {

	private DailyPnlService dailyPnlService;

	public DailyPnlBusinessDelegate() {
		dailyPnlService = TradistaServiceLocator.getInstance().getDailyPnlService();
	}

	public DailyPnl calculateDailyPnl(String positionDefinitionName, String calendarName, LocalDate valueDate)
			throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isEmpty(positionDefinitionName)) {
			errMsg.append(String.format("The position definition name is mandatory.%n"));
		}
		if (StringUtils.isEmpty(calendarName)) {
			errMsg.append(String.format("The position definition name is mandatory.%n"));
		}
		if (valueDate == null) {
			errMsg.append(String.format("The value date cannot be null.%n"));
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		return SecurityUtil
				.runEx(() -> dailyPnlService.calculateDailyPnl(positionDefinitionName, calendarName, valueDate));
	}

	public long saveDailyPnl(DailyPnl dailyPnl) throws TradistaBusinessException {
		if (dailyPnl == null) {
			throw new TradistaBusinessException("The daily PNL cannot be null.");
		}
		StringBuilder errMsg = new StringBuilder();
		if (dailyPnl.getCalendar() == null) {
			errMsg.append(String.format("The daily pnl calendar is mandatory.%n"));
		}
		if (dailyPnl.getPositionDefinition() == null) {
			errMsg.append(String.format("The daily pnl position definition is mandatory.%n"));
		}
		if (dailyPnl.getValueDate() == null) {
			errMsg.append(String.format("The daily pnl value date is mandatory.%n"));
		}
		if (dailyPnl.getPnl() == null) {
			errMsg.append(String.format("The daily pnl PNL amount is mandatory.%n"));
		}
		if (dailyPnl.getRealizedPnl() == null) {
			errMsg.append(String.format("The daily pnl REALIZED PNL amount is mandatory.%n"));
		}
		if (dailyPnl.getUnrealizedPnl() == null) {
			errMsg.append(String.format("The daily pnl UNREALIZED PNL is mandatory.%n"));
		}

		return SecurityUtil.run(() -> dailyPnlService.saveDailyPnl(dailyPnl));

	}

	public Set<DailyPnl> getDailyPnlsByDefinitionIdCalendarAndValueDates(long positionDefinitionId, String calendarCode,
			LocalDate valueDateFrom, LocalDate valueDateTo) throws TradistaBusinessException {
		if (valueDateFrom != null && valueDateTo != null) {
			if (valueDateTo.isBefore(valueDateFrom)) {
				throw new TradistaBusinessException(
						String.format("'To' value date cannot be before 'From' value date.%n"));
			}
		}
		return SecurityUtil
				.runEx(() -> dailyPnlService.getDailyPnlsByDefinitionIdCalendarAndValueDates(positionDefinitionId,
						calendarCode, valueDateFrom, valueDateTo));
	}

}