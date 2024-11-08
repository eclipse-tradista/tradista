package org.eclipse.tradista.core.error.service;

import java.time.LocalDate;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.eclipse.tradista.core.error.model.Error.Status;
import org.eclipse.tradista.core.error.persistence.ErrorSQL;
import org.eclipse.tradista.core.error.service.ErrorService;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;

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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class ErrorServiceBean implements ErrorService {

	@Override
	public void deleteErrors(String errorType, Status status, LocalDate errorDateFrom, LocalDate errorDateTo) {
		ErrorSQL.deleteErrors(errorType, status, errorDateFrom, errorDateTo);
	}

}
