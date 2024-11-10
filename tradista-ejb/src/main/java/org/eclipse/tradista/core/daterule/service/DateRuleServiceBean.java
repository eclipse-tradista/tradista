package org.eclipse.tradista.core.daterule.service;

import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.daterule.model.DateRule;
import org.jboss.ejb3.annotation.SecurityDomain;

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
public class DateRuleServiceBean implements DateRuleService {

	@Override
	public Set<DateRule> getAllDateRules() {
		return DateRuleSQL.getAllDateRules();
	}

	@Override
	public DateRule getDateRuleById(long id) {
		return DateRuleSQL.getDateRuleById(id);
	}

	@Override
	public DateRule getDateRuleByName(String name) {
		return DateRuleSQL.getDateRuleByName(name);
	}

	@Override
	public long saveDateRule(DateRule dateRule) throws TradistaBusinessException {
		if (dateRule.getId() == 0) {
			checkNameExistence(dateRule);
		} else {
			DateRule oldDateRule = DateRuleSQL.getDateRuleById(dateRule.getId());
			if (!oldDateRule.getName().equals(dateRule.getName())) {
				checkNameExistence(dateRule);
			}
		}
		return DateRuleSQL.saveDateRule(dateRule);
	}

	private void checkNameExistence(DateRule dateRule) throws TradistaBusinessException {
		if (getDateRuleByName(dateRule.getName()) != null) {
			throw new TradistaBusinessException(
					String.format("A date rule with the name '%s' already exists in the system.", dateRule.getName()));
		}
	}

}