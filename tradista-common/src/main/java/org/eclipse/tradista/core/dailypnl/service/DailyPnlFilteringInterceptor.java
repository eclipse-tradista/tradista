package org.eclipse.tradista.core.dailypnl.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.dailypnl.model.DailyPnl;
import org.eclipse.tradista.core.position.model.PositionDefinition;
import org.eclipse.tradista.core.position.service.PositionDefinitionBusinessDelegate;
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

public class DailyPnlFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	private PositionDefinitionBusinessDelegate positionDefinitionBusinessDelegate;

	public DailyPnlFilteringInterceptor() {
		super();
		positionDefinitionBusinessDelegate = new PositionDefinitionBusinessDelegate();
	}

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		Object[] parameters = ic.getParameters();
		long posDefId = (long) parameters[0];
		if (posDefId != 0) {
			PositionDefinition posDef = positionDefinitionBusinessDelegate.getPositionDefinitionById(posDefId);
			if (posDef == null) {
				throw new TradistaBusinessException(String.format("The Book %s was not found.", posDefId));
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected Object postFilter(Object value) {
		if (value != null) {
			if (value instanceof Set) {
				Set<DailyPnl> dailyPnls = (Set<DailyPnl>) value;
				User user = getCurrentUser();
				value = dailyPnls.stream().filter(
						pnl -> pnl.getPositionDefinition().getBook().getProcessingOrg().equals(user.getProcessingOrg()))
						.collect(Collectors.toSet());
			}
		}
		return value;
	}

}