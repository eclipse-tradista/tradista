/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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

package org.eclipse.tradista.core.common.service;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;

/**
 * Strategy interface for parameter access checks triggered by
 * {@link AccessCheckedBy}-annotated annotations.
 *
 * <p>
 * Implementations are instantiated reflectively by
 * {@link TradistaSegregationHandlerInterceptor} and must expose a public no-arg
 * constructor.
 */
public interface AccessChecker {

	/**
	 * Verifies that the given parameter is accessible in the system for the current
	 * user. Appends a human-readable error message to {@code errMsg} if it is not.
	 *
	 * @param parameter the method parameter value to check (never {@code null})
	 * @param errMsg    accumulator for error messages
	 * @throws TradistaBusinessException if a technical error occurs during the
	 *                                   lookup
	 */
	void check(Object parameter, StringBuilder errMsg) throws TradistaBusinessException;

}