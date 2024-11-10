package org.eclipse.tradista.core.marketdata.service;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.VolatilitySurface;
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

public class VolatilitySurfaceFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	private QuoteBusinessDelegate quoteBusinessDelegate;

	private SurfaceBusinessDelegate surfaceBusinessDelegate;

	public VolatilitySurfaceFilteringInterceptor() {
		super();
		quoteBusinessDelegate = new QuoteBusinessDelegate();
		surfaceBusinessDelegate = new SurfaceBusinessDelegate();
	}

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		Object[] parameters = ic.getParameters();
		if (parameters.length > 0) {
			if (parameters[0] instanceof VolatilitySurface) {
				VolatilitySurface<?, ?, ?> surface = (VolatilitySurface<?, ?, ?>) parameters[0];
				StringBuilder errMsg = new StringBuilder();
				if (surface.getId() != 0) {
					VolatilitySurface<?, ?, ?> s = surfaceBusinessDelegate.getSurfaceById(surface.getId());
					if (s == null) {
						errMsg.append(String.format("The surface %d was not found.%n", surface.getId()));
					} else if (s.getProcessingOrg() == null) {
						errMsg.append(
								String.format("This surface %d is a global one and you are not allowed to update it.",
										surface.getId()));
					}
				}
				if (surface.getQuoteSet() != null) {
					QuoteSet qs = quoteBusinessDelegate.getQuoteSetById(surface.getQuoteSet().getId());
					if (qs == null) {
						errMsg.append(
								String.format("The quote set %s was not found.", surface.getQuoteSet().getName()));
					}
				}
				if (errMsg.length() > 0) {
					throw new TradistaBusinessException(errMsg.toString());
				}
			}
			if (parameters[0] instanceof Long) {
				Method method = ic.getMethod();
				if (method.getName().contains("delete")) {
					Long surfaceId = (Long) parameters[0];
					StringBuilder errMsg = new StringBuilder();
					if (surfaceId != 0) {
						VolatilitySurface<?, ?, ?> s = surfaceBusinessDelegate.getSurfaceById(surfaceId);
						if (s == null) {
							errMsg.append(String.format("The surface %d was not found.%n", surfaceId));
						} else if (s.getProcessingOrg() == null) {
							errMsg.append(String.format(
									"This surface %d is a global one and you are not allowed to delete it.",
									surfaceId));
						}
					}
					if (errMsg.length() > 0) {
						throw new TradistaBusinessException(errMsg.toString());
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected Object postFilter(Object value) {
		if (value != null) {
			User user = getCurrentUser();
			if (value instanceof Set) {
				Set<? extends VolatilitySurface<?, ?, ?>> pps = (Set<VolatilitySurface<?, ?, ?>>) value;
				if (!pps.isEmpty()) {
					value = pps.stream()
							.filter(b -> (b.getProcessingOrg() == null)
									|| (b.getProcessingOrg().equals(user.getProcessingOrg())))
							.collect(Collectors.toSet());
				}
			}
			if (value instanceof VolatilitySurface) {
				VolatilitySurface<?, ?, ?> vol = (VolatilitySurface<?, ?, ?>) value;
				if (vol.getProcessingOrg() != null && !user.getProcessingOrg().equals(vol.getProcessingOrg())) {
					value = null;
				}
			}
		}
		return value;
	}

}