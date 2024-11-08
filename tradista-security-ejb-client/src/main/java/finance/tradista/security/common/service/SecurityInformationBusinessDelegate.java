package org.eclipse.tradista.security.common.service;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;

public class SecurityInformationBusinessDelegate {

	private SecurityInformationService securityInformationService;

	public SecurityInformationBusinessDelegate() {
		securityInformationService = TradistaServiceLocator.getInstance().getSecurityInformationService();
	}

	public String getSecurityModuleVersion() {
		return SecurityUtil.run(() -> securityInformationService.getSecurityModuleVersion());
	}

}