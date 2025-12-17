package org.eclipse.tradista.core.transfer.service;

import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.TradistaAuthorizationFilteringInterceptor;
import org.eclipse.tradista.core.transfer.model.FixingError;
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

public class FixingErrorFilteringInterceptor extends TradistaAuthorizationFilteringInterceptor {

	private TransferBusinessDelegate transferBusinessDelegate;

	public FixingErrorFilteringInterceptor() {
		super();
		transferBusinessDelegate = new TransferBusinessDelegate();
	}

	@AroundInvoke
	public Object filter(InvocationContext ic) throws Exception {
		return proceed(ic);
	}

	protected void preFilter(InvocationContext ic) throws TradistaBusinessException {
		Object[] parameters = ic.getParameters();
		long transferId = (long) parameters[0];
		if (transferId != 0) {
			Transfer transfer = transferBusinessDelegate.getTransferById(transferId);
			if (transfer == null || !transfer.isCash()) {
				throw new TradistaBusinessException(String.format("The Cash Transfer %s was not found.", transferId));
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected Object postFilter(Object value) {
		if (value != null) {
			if (value instanceof List) {
				List<FixingError> fixingErrors = (List<FixingError>) value;
				User user = getCurrentUser();
				value = fixingErrors.stream()
						.filter(fe -> fe.getCashTransfer().getBook().getProcessingOrg().equals(user.getProcessingOrg()))
						.toList();
			}
		}
		return value;
	}

}