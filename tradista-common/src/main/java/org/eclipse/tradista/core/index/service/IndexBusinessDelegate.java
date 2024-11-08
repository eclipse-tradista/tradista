package org.eclipse.tradista.core.index.service;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.index.service.IndexService;

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

public class IndexBusinessDelegate {

	private IndexService indexService;

	public IndexBusinessDelegate() {
		indexService = TradistaServiceLocator.getInstance().getIndexService();
	}

	public Index getIndexByName(String indexName) throws TradistaBusinessException {
		if (StringUtils.isEmpty(indexName)) {
			throw new TradistaBusinessException("The index name cannot be null.");
		}
		return SecurityUtil.run(() -> indexService.getIndexByName(indexName));
	}

	public Set<Index> getAllIndexes() {
		return SecurityUtil.run(() -> indexService.getAllIndexes());
	}

	public long saveIndex(Index index) throws TradistaBusinessException {
		if (StringUtils.isBlank(index.getName())) {
			throw new TradistaBusinessException("The name cannot be empty.");
		}
		return SecurityUtil.runEx(() -> indexService.saveIndex(index));
	}

}