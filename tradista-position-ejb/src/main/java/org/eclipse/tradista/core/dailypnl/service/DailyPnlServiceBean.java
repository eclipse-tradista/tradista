package org.eclipse.tradista.core.dailypnl.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.calendar.service.CalendarBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.ProtectGlobal;
import org.eclipse.tradista.core.common.util.DateUtil;
import org.eclipse.tradista.core.dailypnl.model.DailyPnl;
import org.eclipse.tradista.core.dailypnl.persistence.DailyPnlSQL;
import org.eclipse.tradista.core.error.model.Error;
import org.eclipse.tradista.core.position.model.Position;
import org.eclipse.tradista.core.position.model.PositionCalculationError;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.position.service.CheckPositionDefinitionAccess;
import org.eclipse.tradista.core.position.service.PositionCalculationErrorService;
import org.eclipse.tradista.core.position.service.PositionDefinitionService;
import org.eclipse.tradista.core.position.service.PositionService;
import org.eclipse.tradista.core.trade.service.ProductScope;
import org.eclipse.tradista.core.trade.service.ProductScopeMode;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

/********************************************************************************
 * Copyright (c) 2016 Olivier Asuncion
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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class DailyPnlServiceBean implements DailyPnlService {

	@EJB
	private PositionDefinitionService positionDefinitionService;

	@EJB
	private PositionService positionService;

	@EJB
	private PositionCalculationErrorService positionCalculationErrorService;

	private static final String POSITION_MUST_BE_CALCULATED = "The position as of %tD must be calculated. The position calculation error must be solved.";

	@Override
	@ProductScope
	public DailyPnl calculateDailyPnl(String positionDefinitionName, String calendarCode, LocalDate valueDate)
			throws TradistaBusinessException {
		PositionDefinition posDef = positionDefinitionService.getPositionDefinitionByName(positionDefinitionName);
		if (posDef == null) {
			throw new TradistaBusinessException(
					String.format("The position definition named '%s' cannot be found.", positionDefinitionName));
		}
		Calendar cal = new CalendarBusinessDelegate().getCalendarByCode(calendarCode);
		if (cal == null) {
			throw new TradistaBusinessException(
					String.format("The calendar with code '%s' cannot be found.", calendarCode));
		}

		// 1. Get the previous business day

		LocalDate previousDay = DateUtil.previousBusinessDay(valueDate, cal);

		Position previousDayPosition = positionService.getLastPositionByDefinitionNameAndValueDate(posDef.getName(),
				previousDay);
		Position position;

		// 2. If the position doesn't exist, we check is there was an unsolved
		// error
		if (previousDayPosition == null) {
			List<PositionCalculationError> errors = positionCalculationErrorService.getPositionCalculationErrors(
					posDef.getId(), Error.Status.UNSOLVED, 0, 0, previousDay, previousDay, null, null, null, null);
			if (errors != null && !errors.isEmpty()) {
				throw new TradistaBusinessException(String.format(POSITION_MUST_BE_CALCULATED, previousDay));
			} else {
				// There was no error, so we calculate the position
				positionService.calculatePosition(posDef.getName(), LocalDateTime.of(previousDay, LocalTime.MIN));
				previousDayPosition = positionService.getLastPositionByDefinitionNameAndValueDate(posDef.getName(),
						previousDay);
				// 3. If the position still doesn't exist, it means there was an
				// unsolved error to be solved.
				if (previousDayPosition == null) {
					throw new TradistaBusinessException(String.format(POSITION_MUST_BE_CALCULATED, previousDay));
				}
			}
		}

		positionService.calculatePosition(posDef.getName(), valueDate.atTime(LocalTime.MAX));

		List<PositionCalculationError> errors = positionCalculationErrorService.getPositionCalculationErrors(
				posDef.getId(), Error.Status.UNSOLVED, 0, 0, valueDate, valueDate, null, null, null, null);

		if (errors != null && !errors.isEmpty()) {
			throw new TradistaBusinessException(String.format(POSITION_MUST_BE_CALCULATED, valueDate));
		} else {
			position = positionService.getLastPositionByDefinitionNameAndValueDate(posDef.getName(), valueDate);
		}

		// Check if there was already a daily pnl for this position definition,
		// calendar and value date
		// If yes, we update it. Otherwise, we create a new one.

		DailyPnl dailyPnl = getDailyPnlByPositionDefinitionCalendarAndValueDate(posDef, cal, valueDate);

		if (dailyPnl == null) {
			dailyPnl = new DailyPnl(positionDefinitionService.getPositionDefinitionByName(posDef.getName()), cal,
					valueDate);
		}
		dailyPnl.setPnl(position.getPnl().subtract(previousDayPosition.getPnl()));
		dailyPnl.setRealizedPnl(position.getRealizedPnl().subtract(previousDayPosition.getRealizedPnl()));
		dailyPnl.setUnrealizedPnl(position.getUnrealizedPnl().subtract(previousDayPosition.getUnrealizedPnl()));

		return dailyPnl;

	}

	private DailyPnl getDailyPnlByPositionDefinitionCalendarAndValueDate(PositionDefinition positionDefinition,
			Calendar calendar, LocalDate valueDate) {
		return DailyPnlSQL.getDailyPnlByPositionDefinitionCalendarAndValueDate(positionDefinition, calendar, valueDate);
	}

	@Override
	@ProtectGlobal
	@ProductScope(mode = ProductScopeMode.ON_CREATION)
	public long saveDailyPnl(DailyPnl dailyPnl) {
		return DailyPnlSQL.saveDailyPnl(dailyPnl);
	}

	@Override
	public Set<DailyPnl> getDailyPnlsByDefinitionIdCalendarAndValueDates(
			@CheckPositionDefinitionAccess long positionDefinitionId, String calendarCode, LocalDate valueDateFrom,
			LocalDate valueDateTo) throws TradistaBusinessException {
		return DailyPnlSQL.getDailyPnlsByPositionDefinitionCalendarAndValueDate(positionDefinitionId, calendarCode,
				valueDateFrom, valueDateTo);
	}

}
