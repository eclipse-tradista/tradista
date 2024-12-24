package org.eclipse.tradista.ai.agent.service;

import org.eclipse.tradista.ai.agent.model.Mandate;
import org.eclipse.tradista.ai.agent.persistence.MandateSQL;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;

/********************************************************************************
 * Copyright (c) 2019 Olivier Asuncion
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
public class MandateServiceBean implements MandateService {

	@Override
	public long saveMandate(Mandate mandate) throws TradistaBusinessException {
		if (mandate.getId() == 0) {
			checkName(mandate);
			return MandateSQL.saveMandate(mandate);
		} else {
			Mandate oldMandate = MandateSQL.getMandateById(mandate.getId());
			if (!mandate.getName().equals(oldMandate.getName())) {
				checkName(mandate);
			}
			return MandateSQL.saveMandate(mandate);
		}
	}

	private void checkName(Mandate mandate) throws TradistaBusinessException {
		if (MandateSQL.getMandateByName(mandate.getName()) != null) {
			throw new TradistaBusinessException(
					String.format("The mandate with name '%s' already exists.", mandate.getName()));
		}
	}

	@Override
	public Mandate getMandateById(long id) {
		return MandateSQL.getMandateById(id);
	}

}