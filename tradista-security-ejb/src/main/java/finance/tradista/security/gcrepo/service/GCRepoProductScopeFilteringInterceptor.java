package finance.tradista.security.gcrepo.service;

import finance.tradista.core.trade.service.ProductScopeFilteringInterceptor;
import finance.tradista.security.gcrepo.model.GCRepoTrade;

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

public class GCRepoProductScopeFilteringInterceptor extends ProductScopeFilteringInterceptor {

	public GCRepoProductScopeFilteringInterceptor() {
		super(GCRepoTrade.GC_REPO);
	}
}
