package org.eclipse.tradista.core.position.service;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.position.model.Position;
import org.eclipse.tradista.core.position.model.PositionDefinition;
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

public class PositionFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	private PositionDefinitionBusinessDelegate positionDefinitionBusinessDelegate;

	public PositionFilteringInterceptor() {
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
				throw new TradistaBusinessException(
						String.format("The Position Definition %s was not found.", posDefId));
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected Object postFilter(Object value) {
		if (value != null) {
			if (value instanceof List) {
				List<Position> positions = (List<Position>) value;
				User user = getCurrentUser();
				value = positions.stream().filter(
						p -> p.getPositionDefinition().getBook().getProcessingOrg().equals(user.getProcessingOrg()))
						.collect(Collectors.toList());
			}
		}
		return value;
	}

}