package org.eclipse.tradista.core.position.service;

import java.util.Set;

import jakarta.ejb.Local;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.position.model.PositionDefinition;

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
public interface LocalPositionDefinitionService {

	Set<PositionDefinition> getAllPositionDefinitions();

	PositionDefinition getPositionDefinitionByName(String name);

	long savePositionDefinition(PositionDefinition positionDefinition) throws TradistaBusinessException;

	boolean deletePositionDefinition(String name);

	Set<PositionDefinition> getAllRealTimePositionDefinitions();

	PositionDefinition getPositionDefinitionById(long id);

	Set<String> getPositionDefinitionsByPricingParametersSetId(long id);

}
