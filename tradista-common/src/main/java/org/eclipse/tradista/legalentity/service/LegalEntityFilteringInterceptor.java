package org.eclipse.tradista.legalentity.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.legalentity.model.LegalEntity.Role;
import org.eclipse.tradista.core.user.model.User;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

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

public class LegalEntityFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	private LegalEntityBusinessDelegate legalEntityBusinessDelegate;

	public LegalEntityFilteringInterceptor() {
		super();
		legalEntityBusinessDelegate = new LegalEntityBusinessDelegate();
	}

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		Object[] parameters = ic.getParameters();
		if (parameters.length > 0 && parameters[0] instanceof LegalEntity) {
			LegalEntity legalEntity = (LegalEntity) parameters[0];
			StringBuilder errMsg = new StringBuilder();
			User user = getCurrentUser();
			if (legalEntity.getId() != 0) {
				LegalEntity le = legalEntityBusinessDelegate.getLegalEntityById(legalEntity.getId());
				if (le == null) {
					errMsg.append(String.format("The legal entity %s was not found.%n", legalEntity.getShortName()));
				}
			}
			if (legalEntity.getRole().equals(Role.PROCESSING_ORG) && !legalEntity.equals(user.getProcessingOrg())) {
				errMsg.append(String.format("The processing org %s was not found.", legalEntity.getShortName()));
			}
			if (errMsg.length() > 0) {
				throw new TradistaBusinessException(errMsg.toString());
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected Object postFilter(Object value) {
		if (value != null) {
			if (value instanceof Set) {
				Set<LegalEntity> legalEntities = (Set<LegalEntity>) value;
				User user = getCurrentUser();
				value = legalEntities.stream().filter(
						l -> l.getRole().equals(LegalEntity.Role.COUNTERPARTY) || l.equals(user.getProcessingOrg()))
						.collect(Collectors.toSet());
			}
			if (value instanceof LegalEntity) {
				LegalEntity legalEntity = (LegalEntity) value;
				User user = getCurrentUser();
				if (legalEntity.getRole().equals(LegalEntity.Role.PROCESSING_ORG)
						&& !legalEntity.equals(user.getProcessingOrg())) {
					value = null;
				}
			}
		}
		return value;
	}

}