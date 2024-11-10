package org.eclipse.tradista.core.marketdata.service;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.marketdata.model.Curve;
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

public class CurveFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	private CurveBusinessDelegate curveBusinessDelegate;

	public CurveFilteringInterceptor() {
		super();
		curveBusinessDelegate = new CurveBusinessDelegate();
	}

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	@Override
	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		Object[] parameters = ic.getParameters();
		if (parameters.length > 0 && parameters[0] instanceof Curve<?, ?> curve) {
			StringBuilder errMsg = new StringBuilder();
			Method method = ic.getMethod();
			if (curve.getId() != 0) {
				Curve<?, ?> c = curveBusinessDelegate.getCurveById(curve.getId());
				if (c == null) {
					errMsg.append(
							String.format("The curve %s (id %d) was not found.%n", curve.getName(), curve.getId()));
				} else if (method.getName().contains("save")) {
					if (c.getProcessingOrg() == null) {
						errMsg.append(String.format(
								"This curve %s (id %d) is a global one and you are not allowed to update it.",
								curve.getName(), curve.getId()));
					}
				} else if (method.getName().contains("delete")) {
					if (c.getProcessingOrg() == null) {
						errMsg.append(String.format(
								"This curve %s (id %d) is a global one and you are not allowed to delete it.",
								curve.getName(), curve.getId()));
					}
				}
			}
			if (curve.getProcessingOrg() != null
					&& !curve.getProcessingOrg().equals(getCurrentUser().getProcessingOrg())) {
				errMsg.append(String.format("The processing org %s was not found.", curve.getProcessingOrg()));
			}
			if (errMsg.length() > 0) {
				throw new TradistaBusinessException(errMsg.toString());
			}
		}
		if (parameters.length > 0 && parameters[0] instanceof Long curveId) {
			Method method = ic.getMethod();
			if (!method.getName().contains("CurveById")) {
				StringBuilder errMsg = new StringBuilder();
				if (curveId != 0) {
					Curve<?, ?> c = curveBusinessDelegate.getCurveById(curveId);
					if (c == null) {
						errMsg.append(String.format("The curve with id %d was not found.%n", curveId));
					} else if (method.getName().contains("delete")) {
						if (c.getProcessingOrg() == null) {
							errMsg.append(String.format(
									"This curve with id %d is a global one and you are not allowed to delete it.",
									c.getId()));
						}
					} else if (method.getName().contains("save")) {
						if (c.getProcessingOrg() == null) {
							errMsg.append(String.format(
									"This curve with id %d is a global one and you are not allowed to save points on it.",
									c.getId()));
						}
					}
				}
				if (errMsg.length() > 0) {
					throw new TradistaBusinessException(errMsg.toString());
				}
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Object postFilter(Object value) {
		if (value != null) {
			if (value instanceof Set) {
				Set<? extends Curve<?, ?>> curves = (Set<Curve<?, ?>>) value;
				if (!curves.isEmpty()) {
					User user = getCurrentUser();
					value = curves.stream()
							.filter(b -> (b.getProcessingOrg() == null)
									|| (b.getProcessingOrg().equals(user.getProcessingOrg())))
							.collect(Collectors.toSet());
				}
			}
			if (value instanceof Curve<?, ?> curve) {
				if (curve.getProcessingOrg() != null
						&& !curve.getProcessingOrg().equals(getCurrentUser().getProcessingOrg())) {
					value = null;
				}
			}
		}
		return value;
	}

}