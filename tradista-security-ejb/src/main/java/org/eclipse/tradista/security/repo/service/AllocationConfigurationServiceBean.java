package org.eclipse.tradista.security.repo.service;

import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.security.gcrepo.persistence.AllocationConfigurationSQL;
import org.eclipse.tradista.security.repo.model.AllocationConfiguration;
import org.eclipse.tradista.security.specificrepo.service.SpecificRepoProductScopeFilteringInterceptor;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

/********************************************************************************
 * Copyright (c) 2024 Olivier Asuncion
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
public class AllocationConfigurationServiceBean implements AllocationConfigurationService {

	@Override
	@Interceptors({ SpecificRepoProductScopeFilteringInterceptor.class, AllocationConfigurationFilteringInterceptor.class })
	public long saveAllocationConfiguration(AllocationConfiguration allocationConfiguration)
			throws TradistaBusinessException {
		if (allocationConfiguration.getId() == 0) {
			checkAllocationConfigurationExistence(allocationConfiguration);
			return AllocationConfigurationSQL.saveAllocationConfiguration(allocationConfiguration);
		} else {
			AllocationConfiguration oldAllocationConfiguration = AllocationConfigurationSQL
					.getAllocationConfigurationById(allocationConfiguration.getId());
			if (!allocationConfiguration.getName().equals(oldAllocationConfiguration.getName())
					|| !allocationConfiguration.getProcessingOrg()
							.equals(oldAllocationConfiguration.getProcessingOrg())) {
				checkAllocationConfigurationExistence(allocationConfiguration);
			}
			return AllocationConfigurationSQL.saveAllocationConfiguration(allocationConfiguration);
		}
	}

	private void checkAllocationConfigurationExistence(AllocationConfiguration allocationConfiguration)
			throws TradistaBusinessException {
		if (AllocationConfigurationSQL.getAllocationConfigurationByNameAndPoId(allocationConfiguration.getName(),
				allocationConfiguration.getProcessingOrg().getId()) != null) {
			throw new TradistaBusinessException(String.format(
					"This Allocation Configuration '%s' already exists in the system for the PO '%s'.",
					allocationConfiguration.getName(), allocationConfiguration.getProcessingOrg().getShortName()));
		}
	}

	@Override
	@Interceptors(AllocationConfigurationFilteringInterceptor.class)
	public AllocationConfiguration getAllocationConfigurationByName(String name) {
		return AllocationConfigurationSQL.getAllocationConfigurationByName(name);
	}

	@Override
	@Interceptors(AllocationConfigurationFilteringInterceptor.class)
	public AllocationConfiguration getAllocationConfigurationById(long id) {
		return AllocationConfigurationSQL.getAllocationConfigurationById(id);
	}

	@Override
	@Interceptors(AllocationConfigurationFilteringInterceptor.class)
	public Set<AllocationConfiguration> getAllAllocationConfigurations() {
		return AllocationConfigurationSQL.getAllAllocationConfigurations();
	}

}