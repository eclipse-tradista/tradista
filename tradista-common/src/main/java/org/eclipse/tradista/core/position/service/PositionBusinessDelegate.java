package org.eclipse.tradista.core.position.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.position.model.Position;
import org.eclipse.tradista.core.position.service.PositionService;

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

public class PositionBusinessDelegate {

	private PositionService positionService;

	public PositionBusinessDelegate() {
		positionService = TradistaServiceLocator.getInstance().getPositionService();
	}

	public long savePosition(Position position) throws TradistaBusinessException {
		checkPosition(position);
		return SecurityUtil.run(() -> positionService.savePosition(position));
	}

	private void checkPosition(Position position) throws TradistaBusinessException {
		if (position == null) {
			throw new TradistaBusinessException("The position cannot be null.");
		}
		StringBuilder errMsg = new StringBuilder();
		if (position.getPnl() == null) {
			errMsg.append(String.format("The pnl is mandatory.%n"));
		}
		if (position.getRealizedPnl() == null) {
			errMsg.append(String.format("The realized pnl is mandatory.%n"));
		}
		if (position.getUnrealizedPnl() == null) {
			errMsg.append(String.format("The unrealized pnl is mandatory.%n"));
		}
		if (position.getPositionDefinition() == null) {
			errMsg.append(String.format("The position definition is mandatory.%n"));
		}
		if (position.getValueDateTime() == null) {
			errMsg.append(String.format("The value date time is mandatory.%n"));
		}

		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

	public void savePositions(List<Position> positions) throws TradistaBusinessException {
		if (positions == null || positions.isEmpty()) {
			throw new TradistaBusinessException("The positions list is null or empty.");
		}
		SecurityUtil.run(() -> positionService.savePositions(positions));
	}

	public List<Position> getPositionsByDefinitionIdAndValueDates(long positionDefinitionId, LocalDate valueDateFrom,
			LocalDate valueDateTo) throws TradistaBusinessException {
		if (valueDateFrom != null && valueDateTo != null) {
			if (valueDateTo.isBefore(valueDateFrom)) {
				throw new TradistaBusinessException("'To' value date cannot be before 'From' value date.");
			}
		}
		return SecurityUtil.runEx(() -> positionService.getPositionsByDefinitionIdAndValueDates(positionDefinitionId,
				valueDateFrom, valueDateTo));
	}

	public void calculatePosition(String positionDefinitionName, LocalDateTime valueDateTime)
			throws TradistaBusinessException {
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isEmpty(positionDefinitionName)) {
			errMsg.append(String.format("The position definition name is mandatory.%n"));
		}
		if (valueDateTime == null) {
			errMsg.append(String.format("The value date time cannot be null.%n"));
		}
		if (errMsg.length() > 0) {
			throw new TradistaBusinessException(errMsg.toString());
		}
		SecurityUtil.runEx(() -> positionService.calculatePosition(positionDefinitionName, valueDateTime));
	}

	public Position getLastPositionByDefinitionNameAndValueDate(String positionDefinitionName, LocalDate valueDate) {
		return SecurityUtil.run(
				() -> positionService.getLastPositionByDefinitionNameAndValueDate(positionDefinitionName, valueDate));
	}

}