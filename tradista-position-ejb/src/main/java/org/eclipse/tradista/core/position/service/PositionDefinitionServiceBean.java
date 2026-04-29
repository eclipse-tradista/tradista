package org.eclipse.tradista.core.position.service;

import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.position.persistence.PositionDefinitionSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.Resource;
import jakarta.ejb.EJBContext;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.user.model.User;
import org.eclipse.tradista.core.user.service.UserBusinessDelegate;

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
public class PositionDefinitionServiceBean implements LocalPositionDefinitionService, PositionDefinitionService {

	@Resource
	private EJBContext ctx;

	private UserBusinessDelegate userBusinessDelegate = new UserBusinessDelegate();

	@Interceptors(PositionDefinitionFilteringInterceptor.class)
	@Override
	public Set<PositionDefinition> getAllPositionDefinitions() {
		return PositionDefinitionSQL.getAllPositionDefinitions();
	}

	@Interceptors(PositionDefinitionFilteringInterceptor.class)
	@Override
	public PositionDefinition getPositionDefinitionByName(String name) {
		return PositionDefinitionSQL.getPositionDefinitionByNameAndPoId(name,
				getCurrentUser().getProcessingOrg().getId());

	}

	@Interceptors(PositionDefinitionFilteringInterceptor.class)
	@Override
	public PositionDefinition getPositionDefinitionById(long id) {
		return PositionDefinitionSQL.getPositionDefinitionById(id);
	}

	@Interceptors({ PositionDefinitionProductScopeFilteringInterceptor.class,
			PositionDefinitionFilteringInterceptor.class })
	@Override
	public long savePositionDefinition(PositionDefinition positionDefinition) throws TradistaBusinessException {
		if (positionDefinition.getId() == 0) {
			checkPositionDefinitionName(positionDefinition);
		} else {
			PositionDefinition oldPositionDefinition = PositionDefinitionSQL
					.getPositionDefinitionById(positionDefinition.getId());
			if (!oldPositionDefinition.getName().equals(positionDefinition.getName())) {
				checkPositionDefinitionName(positionDefinition);
			}
		}
		return PositionDefinitionSQL.savePositionDefinition(positionDefinition);
	}

	private void checkPositionDefinitionName(PositionDefinition positionDefinition) throws TradistaBusinessException {
		if (PositionDefinitionSQL.getPositionDefinitionByNameAndPoId(positionDefinition.getName(),
				positionDefinition.getProcessingOrg() == null ? 0
						: positionDefinition.getProcessingOrg().getId()) != null) {
			throw new TradistaBusinessException(
					String.format("A position definition named %s already exists for this Processing Org.",
							positionDefinition.getName()));
		}
	}

	@Override
	public boolean deletePositionDefinition(String name) {

		return PositionDefinitionSQL.deletePositionDefinition(name, getCurrentUser().getProcessingOrg().getId());
	}

	private User getCurrentUser() {
		User user = null;
		if (ctx.getContextData().get(SecurityUtil.CURRENT_USER) == null) {
			try {
				user = userBusinessDelegate.getUserByLogin(ctx.getCallerPrincipal().getName());
			} catch (TradistaBusinessException _) {
				// Not expected here
			}
			ctx.getContextData().put(SecurityUtil.CURRENT_USER, user);
		} else {
			user = (User) ctx.getContextData().get(SecurityUtil.CURRENT_USER);
		}
		return user;
	}

	@Override
	public Set<PositionDefinition> getAllRealTimePositionDefinitions() {
		return PositionDefinitionSQL.getAllRealTimePositionDefinitions();
	}

	@Override
	public Set<String> getPositionDefinitionsByPricingParametersSetId(long id) {
		return PositionDefinitionSQL.getPositionDefinitionsByPricingParametersSetId(id);
	}

	@Interceptors(PositionDefinitionFilteringInterceptor.class)
	@Override
	public Set<PositionDefinition> getPositionDefinitionsByPoId(long poId) {
		return PositionDefinitionSQL.getPositionDefinitionsByPoId(poId);
	}
}