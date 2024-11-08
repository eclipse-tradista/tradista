package org.eclipse.tradista.legalentity.service;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.legalentity.service.LegalEntityService;

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

public class LegalEntityBusinessDelegate {

	private LegalEntityService legalEntityService;

	public LegalEntityBusinessDelegate() {
		legalEntityService = TradistaServiceLocator.getInstance().getLegalEntityService();
	}

	public Set<LegalEntity> getAllLegalEntities() {
		return SecurityUtil.run(() -> legalEntityService.getAllLegalEntities());
	}

	public Set<LegalEntity> getAllCounterparties() {
		return SecurityUtil.run(() -> legalEntityService.getAllCounterparties());
	}

	public long saveLegalEntity(LegalEntity legalEntity) throws TradistaBusinessException {
		if (legalEntity == null) {
			throw new TradistaBusinessException("The legal entity cannot be null.");
		}
		StringBuilder errMsg = new StringBuilder();
		if (StringUtils.isBlank(legalEntity.getShortName())) {
			errMsg.append("The short name cannot be empty.");
		}
		if (legalEntity.getRole() == null) {
			errMsg.append("The role cannot be null.");
		}
		if (legalEntity.getShortName() != null && legalEntity.getShortName().length() > 20) {
			errMsg.append("The short name cannot exceed 20 characters.");
		}
		if (legalEntity.getLongName() != null && legalEntity.getLongName().length() > 100) {
			errMsg.append("The long name cannot exceed 100 characters.");
		}
		if (legalEntity.getDescription() != null && legalEntity.getDescription().length() > 1000) {
			errMsg.append("The description cannot exceed 1000 characters.");
		}
		return SecurityUtil.runEx(() -> legalEntityService.saveLegalEntity(legalEntity));
	}

	public Set<LegalEntity> getLegalEntitiesByShortNameAndRole(String shortName, LegalEntity.Role role) {
		return SecurityUtil.run(() -> legalEntityService.getLegalEntitiesByShortNameAndRole(shortName, role));
	}

	public LegalEntity getLegalEntityByShortName(String shortName) {
		return SecurityUtil.run(() -> legalEntityService.getLegalEntityByShortName(shortName));
	}

	public LegalEntity getLegalEntityById(long id) throws TradistaBusinessException {
		if (id <= 0) {
			throw new TradistaBusinessException("The legal entity id must be positive.");
		}
		return SecurityUtil.run(() -> legalEntityService.getLegalEntityById(id));
	}

	public Set<LegalEntity> getAllProcessingOrgs() {
		return SecurityUtil.run(() -> legalEntityService.getAllProcessingOrgs());
	}

}