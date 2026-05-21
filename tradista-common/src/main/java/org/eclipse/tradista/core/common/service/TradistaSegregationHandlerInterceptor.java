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
import org.eclipse.tradista.core.common.model.Segregable;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.user.model.User;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

/**
 * Global EJB interceptor responsible for enforcing processing org (PO)
 * segregation on all business methods. Registered via ejb-jar.xml so it applies
 * automatically to every EJB without requiring per-method annotations.
 *
 * <p>
 * Extends {@link TradistaAuthorizationFilteringInterceptor} and uses its
 * proceed() orchestration (preFilter → ic.proceed() → postFilter).
 *
 * <p>
 * Pre-call checks ({@code preFilter}):
 * <ul>
 * <li>Any parameter implementing {@link Segregable} is automatically verified
 * against the caller's PO - no annotation required.</li>
 * <li>Parameters of type {@link String} or {@link Long} representing a PO must
 * be annotated with {@link CheckProcessingOrg}.</li>
 * </ul>
 *
 * <p>
 * Post-call filtering ({@code postFilter}): return values implementing
 * {@link Segregable}, or collections thereof, are filtered so that only objects
 * Belonging to the caller's PO (or global objects with a null PO) are returned.
 */

public class TradistaSegregationHandlerInterceptor extends TradistaAuthorizationFilteringInterceptor {

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	@Override
	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		User user = getCurrentUser();
		if (user == null || user.getProcessingOrg() == null) {
			return;
		}
		boolean protectGlobal = ic.getMethod().isAnnotationPresent(ProtectGlobal.class);
		java.lang.annotation.Annotation[][] paramAnnotations = ic.getMethod().getParameterAnnotations();
		Object[] parameters = ic.getParameters();
		StringBuilder errMsg = new StringBuilder();

		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i] instanceof Segregable segregable) {
				// Auto-check Segregable params — no annotation needed
				SecurityUtil.checkProcessingOrg(user, segregable.getProcessingOrg(), errMsg, protectGlobal);
			} else {
				for (java.lang.annotation.Annotation annotation : paramAnnotations[i]) {
					// @CheckProcessingOrg: verify non-Segregable PO params (String, Long)
					if (annotation instanceof CheckProcessingOrg) {
						if (parameters[i] instanceof String po) {
							if (!user.getProcessingOrg().getShortName().equals(po)) {
								errMsg.append(String.format("The processing org %s was not found.", po));
							}
						} else if (parameters[i] instanceof Long poId) {
							if (user.getProcessingOrg().getId() != poId) {
								errMsg.append(String.format("The processing org with id %d was not found.", poId));
							}
						}
					}
					// @AccessCheckedBy: delegate access check to the domain-specific checker
					AccessCheckedBy accessCheckedBy = annotation.annotationType().getAnnotation(AccessCheckedBy.class);
					if (accessCheckedBy != null && parameters[i] != null) {
						try {
							AccessChecker checker = accessCheckedBy.value().getDeclaredConstructor().newInstance();
							checker.check(parameters[i], errMsg);
						} catch (ReflectiveOperationException e) {
							throw new TradistaBusinessException(
									String.format("Could not instantiate AccessChecker %s: %s",
											accessCheckedBy.value().getSimpleName(), e.getMessage()));
						}
					}
				}
			}
		}

		if (!errMsg.isEmpty()) {
			throw new TradistaBusinessException(errMsg.toString());
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Object postFilter(Object value) throws TradistaBusinessException {
		if (value == null) {
			return null;
		}
		User user = getCurrentUser();
		if (value instanceof Segregable segregable) {
			if (segregable.getProcessingOrg() != null
					&& !user.getProcessingOrg().equals(segregable.getProcessingOrg())) {
				return null;
			}
		} else if (value instanceof java.util.Collection<?> collection && !collection.isEmpty()) {
			Object firstElement = collection.iterator().next();
			if (firstElement instanceof Segregable) {
				if (value instanceof java.util.List<?>) {
					java.util.List<Segregable> list = (java.util.List<Segregable>) collection;
					return list.stream().filter(
							s -> s.getProcessingOrg() == null || user.getProcessingOrg().equals(s.getProcessingOrg()))
							.toList();
				} else if (value instanceof java.util.Set<?>) {
					java.util.Set<Segregable> set = (java.util.Set<Segregable>) collection;
					return set.stream().filter(
							s -> s.getProcessingOrg() == null || user.getProcessingOrg().equals(s.getProcessingOrg()))
							.collect(java.util.stream.Collectors.toSet());
				}
			}
		}
		return value;
	}

}