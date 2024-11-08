package org.eclipse.tradista.security.gcrepo.model;

import java.util.Set;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.security.common.model.Security;

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

public class GCBasket extends TradistaObject {

	private static final long serialVersionUID = -7581319325631065360L;

	@Id
	private String name;

	private Set<Security> securities;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@SuppressWarnings("unchecked")
	public Set<Security> getSecurities() {
		return (Set<Security>) TradistaModelUtil.deepCopy(securities);
	}

	public void setSecurities(Set<Security> securities) {
		this.securities = securities;
	}

	@SuppressWarnings("unchecked")
	@Override
	public GCBasket clone() {
		GCBasket gcBasket = (GCBasket) super.clone();
		gcBasket.setSecurities((Set<Security>) TradistaModelUtil.deepCopy(securities));
		return gcBasket;
	}

	@Override
	public String toString() {
		return name;
	}
}