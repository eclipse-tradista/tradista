package org.eclipse.tradista.core.common.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ejb.Stateless;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

/********************************************************************************
 * Copyright (c) 2025 Olivier Asuncion
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

public class TradistaExceptionHandlerInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(TradistaExceptionHandlerInterceptor.class);

	@AroundInvoke
	protected Object handleException(InvocationContext ic) throws Exception {
		Object target = ic.getTarget();
		Object value = null;

		// We handle exceptions only for services exposed through Stateless EJBs.
		if (!target.getClass().isAnnotationPresent(Stateless.class)) {
			return ic.proceed();
		}

		try {
			value = ic.proceed();
		} catch (TradistaBusinessException | TradistaTechnicalException te) {
			// Not logged yet.
			if (te.getCause() != null) {
				logger.error(String.format("%s thrown by service %s.%s", te.getClass().getSimpleName(),
						ic.getTarget().getClass().getSimpleName(), ic.getMethod().getName()), te);
				switch (te) {
				case TradistaBusinessException _ -> throw new TradistaBusinessException(te.getMessage());
				case TradistaTechnicalException _ -> throw new TradistaTechnicalException(te.getMessage());
				default -> throw new TradistaTechnicalException(te.getMessage());
				}
			}
			// Already logged
			throw te;
		} catch (RuntimeException rte) {
			// It was not logged yet.
			logger.error(String.format("Runtime exception thrown by service %s.%s",
					ic.getTarget().getClass().getSimpleName(), ic.getMethod().getName()), rte);
			throw new TradistaTechnicalException(rte.getMessage());
		}

		return value;
	}

}