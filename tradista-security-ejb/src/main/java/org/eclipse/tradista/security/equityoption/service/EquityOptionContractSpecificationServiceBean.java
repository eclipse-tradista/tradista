package org.eclipse.tradista.security.equityoption.service;

import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.security.equityoption.model.EquityOptionContractSpecification;
import org.eclipse.tradista.security.equityoption.persistence.EquityOptionContractSpecificationSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

/********************************************************************************
 * Copyright (c) 2017 Olivier Asuncion
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
public class EquityOptionContractSpecificationServiceBean implements EquityOptionContractSpecificationService {

	@Override
	@Interceptors(EquityOptionProductScopeFilteringInterceptor.class)
	public long saveEquityOptionContractSpecification(EquityOptionContractSpecification eocs)
			throws TradistaBusinessException {
		if (eocs.getId() == 0) {
			checkSpecificationExistence(eocs);
			return EquityOptionContractSpecificationSQL.saveEquityOptionContractSpecification(eocs);
		} else {
			EquityOptionContractSpecification oldEquityOptionSpecification = EquityOptionContractSpecificationSQL
					.getEquityOptionContractSpecificationById(eocs.getId());
			if (!oldEquityOptionSpecification.getName().equals(oldEquityOptionSpecification.getName())) {
				checkSpecificationExistence(oldEquityOptionSpecification);
			}
			return EquityOptionContractSpecificationSQL.saveEquityOptionContractSpecification(eocs);
		}
	}

	private void checkSpecificationExistence(EquityOptionContractSpecification eocs) throws TradistaBusinessException {
		if (getEquityOptionContractSpecificationByName(eocs.getName()) != null) {
			throw new TradistaBusinessException(
					String.format("This equity option specification '%s' already exists.", eocs.getName()));
		}
	}

	@Override
	public Set<EquityOptionContractSpecification> getAllEquityOptionContractSpecifications() {
		return EquityOptionContractSpecificationSQL.getAllEquityOptionContractSpecifications();
	}

	@Override
	public EquityOptionContractSpecification getEquityOptionContractSpecificationById(long id) {
		return EquityOptionContractSpecificationSQL.getEquityOptionContractSpecificationById(id);
	}

	@Override
	public EquityOptionContractSpecification getEquityOptionContractSpecificationByName(String name) {
		return EquityOptionContractSpecificationSQL.getEquityOptionContractSpecificationByName(name);
	}

}