package org.eclipse.tradista.core.message.service;

import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.message.model.ImportError;
import org.springframework.util.CollectionUtils;

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
 *******************************************************************************/

public class ImportErrorAuthorizationFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	private MessageBusinessDelegate messageBusinessDelegate;

	public ImportErrorAuthorizationFilteringInterceptor() {
		messageBusinessDelegate = new MessageBusinessDelegate();
	}

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Object postFilter(Object value) throws TradistaBusinessException {
		if (value != null) {
			if (value instanceof List) {
				List<ImportError> errors = (List<ImportError>) value;
				value = errors.stream().filter(e -> {
					try {
						List<?> msgs = messageBusinessDelegate.getMessages(e.getMessage().getId(), Boolean.TRUE, null,
								null, 0, null, null, null, null, null, null);
						return !CollectionUtils.isEmpty(msgs);
					} catch (TradistaBusinessException _) {
						return false;
					}
				}).toList();
			}
			if (value instanceof ImportError error) {
				List<?> msgs = messageBusinessDelegate.getMessages(error.getMessage().getId(), Boolean.TRUE, null, null,
						0, null, null, null, null, null, null);
				value = CollectionUtils.isEmpty(msgs) ? null : value;
			}
		}
		return value;
	}

}