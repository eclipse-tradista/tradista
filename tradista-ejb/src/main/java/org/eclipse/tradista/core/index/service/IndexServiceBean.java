package org.eclipse.tradista.core.index.service;

import java.util.Set;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.index.persistence.IndexSQL;
import org.eclipse.tradista.core.index.service.IndexService;

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
public class IndexServiceBean implements IndexService {

	@Override
	public long saveIndex(Index index) throws TradistaBusinessException {
		if (index.getId() == 0) {
			checkIndexNameExistence(index);
		} else {
			Index oldIndex = IndexSQL.getIndexById(index.getId());
			if (!oldIndex.getName().equals(oldIndex.getName())) {
				checkIndexNameExistence(index);
			}
		}
		return IndexSQL.saveIndex(index);
	}

	private void checkIndexNameExistence(Index index) throws TradistaBusinessException {
		if (IndexSQL.getIndexByName(index.getName()) != null) {
			throw new TradistaBusinessException(
					String.format("An index named %s already exists in the system.", index.getName()));
		}
	}

	@Override
	public Set<Index> getAllIndexes() {
		return IndexSQL.getAllIndexes();
	}

	@Override
	public Index getIndexByName(String indexName) {
		return IndexSQL.getIndexByName(indexName);
	}

}