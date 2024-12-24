package org.eclipse.tradista.security.gcrepo.service;

import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.security.gcrepo.model.GCBasket;
import org.eclipse.tradista.security.gcrepo.persistence.GCBasketSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

/********************************************************************************
 * Copyright (c) 2023 Olivier Asuncion
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
public class GCBasketServiceBean implements GCBasketService {

	@Override
	@Interceptors(GCRepoProductScopeFilteringInterceptor.class)
	public long saveGCBasket(GCBasket gcBasket) throws TradistaBusinessException {
		if (gcBasket.getId() == 0) {
			checkNameExistence(gcBasket);
			return GCBasketSQL.saveGCBasket(gcBasket);
		} else {
			GCBasket oldGCBasket = GCBasketSQL.getGCBasketById(gcBasket.getId());
			if (!gcBasket.getName().equals(oldGCBasket.getName())) {
				checkNameExistence(gcBasket);
			}
			return GCBasketSQL.saveGCBasket(gcBasket);
		}
	}

	private void checkNameExistence(GCBasket gcBasket) throws TradistaBusinessException {
		if (getGCBasketByName(gcBasket.getName()) != null) {
			throw new TradistaBusinessException(
					String.format("This GC Basket '%s' already exists in the system.", gcBasket.getName()));
		}
	}

	@Override
	public GCBasket getGCBasketByName(String name) {
		return GCBasketSQL.getGCBasketByName(name);
	}

	@Override
	public GCBasket getGCBasketById(long id) {
		return GCBasketSQL.getGCBasketById(id);
	}

	@Override
	public Set<GCBasket> getAllGCBaskets() {
		return GCBasketSQL.getAllGCBaskets();
	}

}