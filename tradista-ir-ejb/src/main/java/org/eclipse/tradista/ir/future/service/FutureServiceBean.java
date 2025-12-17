package org.eclipse.tradista.ir.future.service;

import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.ir.future.model.Future;
import org.eclipse.tradista.ir.future.persistence.FutureSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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
public class FutureServiceBean implements FutureService {

	@Override
	public Set<Future> getAllFutures() {
		return FutureSQL.getAllFutures();
	}

	@Override
	public Future getFutureById(long id) {
		return FutureSQL.getFutureById(id);
	}

	@Override
	public Future getFutureByContractSpecificationAndSymbol(String contractSpecification, String symbol) {
		return FutureSQL.getFutureByContractSpecificationAndSymbol(contractSpecification, symbol);
	}

	@Override
	@Interceptors(FutureProductScopeFilteringInterceptor.class)
	public long saveFuture(Future future) throws TradistaBusinessException {
		if (future.getId() == 0) {
			checkSymbolExistence(future);
			return FutureSQL.saveFuture(future);
		} else {
			Future oldFuture = FutureSQL.getFutureById(future.getId());
			if (!future.getSymbol().equals(oldFuture.getSymbol())) {
				checkSymbolExistence(future);
			}
			return FutureSQL.saveFuture(future);
		}
	}

	private void checkSymbolExistence(Future future) throws TradistaBusinessException {
		if (getFutureByContractSpecificationAndSymbol(future.getContractSpecification().getName(),
				future.getSymbol()) != null) {
			throw new TradistaBusinessException(String.format("This future '%s' already exists for the contract %s.",
					future.getSymbol(), future.getContractSpecification().getName()));
		}
	}

}