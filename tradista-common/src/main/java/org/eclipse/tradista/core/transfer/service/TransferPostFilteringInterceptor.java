package org.eclipse.tradista.core.transfer.service;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.transfer.model.Transfer;
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

public class TransferPostFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	@SuppressWarnings("unchecked")
	public void filter(Object value) throws Exception {
		if (value != null) {
			if (value instanceof Transfer) {
				Transfer transfer = (Transfer) value;
				User user = getCurrentUser();
				if (!transfer.getBook().getProcessingOrg().equals(user.getProcessingOrg())) {
					value = null;
				}
			}
			if (value instanceof List) {
				List<Transfer> transfers = (List<Transfer>) value;
				User user = getCurrentUser();
				value = transfers.stream().filter(t -> t.getBook().getProcessingOrg().equals(user.getProcessingOrg()))
						.collect(Collectors.toList());
			}
		}
	}

}