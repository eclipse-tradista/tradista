package org.eclipse.tradista.core.position.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.position.model.Position;

import jakarta.ejb.Local;

/********************************************************************************
 * Copyright (c) 2022 Olivier Asuncion
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

@Local
public interface LocalPositionService {

	long savePosition(Position position);

	void savePositions(List<Position> positions);

	List<Position> getPositionsByDefinitionIdAndValueDates(long positionDefinitionId, LocalDate valueDateFrom,
			LocalDate valueDateTo) throws TradistaBusinessException;

	void calculatePosition(String positionDefinition, LocalDateTime valueDateTime) throws TradistaBusinessException;

	Position getLastPositionByDefinitionNameAndValueDate(String positionDefinitionName, LocalDate valueDate);

}