package org.eclipse.tradista.core.daycountconvention.service;

import java.util.Set;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.eclipse.tradista.core.daycountconvention.model.DayCountConvention;
import org.eclipse.tradista.core.daycountconvention.persistence.DayCountConventionSQL;
import org.eclipse.tradista.core.daycountconvention.service.DayCountConventionService;

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
public class DayCountConventionServiceBean implements DayCountConventionService {

	@Override
	public Set<DayCountConvention> getAllDayCountConventions() {
		return DayCountConventionSQL.getAllDayCountConventions();
	}
}
