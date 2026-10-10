package org.eclipse.tradista.core.daterule.validator;

import java.io.Serializable;
import java.time.Period;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.daterule.model.DateRule;

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

public class DateRuleValidator implements Serializable {

	private static final long serialVersionUID = 1L;

	public void validateDateRule(DateRule dateRule) throws TradistaBusinessException {
		if (dateRule == null) {
			throw new TradistaBusinessException("The date rule cannot be null.");
		}
		StringBuilder errorMsg = new StringBuilder();
		if (StringUtils.isBlank(dateRule.getName())) {
			errorMsg.append(String.format("The name cannot be empty.%n"));
		}

		if (dateRule.isSequence()) {
			if (dateRule.getSteps() == null || dateRule.getSteps().isEmpty()) {
				errorMsg.append(String.format("The date rule is a sequence but there is no sub date rules.%n"));
			} else {
				for (DateRule.Step step : dateRule.getSteps()) {
					if (step.getDateRule() == null) {
						errorMsg.append(String.format("The sub date rule cannot be null.%n"));
					} else if (step.getDateRule().equals(dateRule)) {
						errorMsg.append(String.format("The date rule cannot contain itself as a sub date rule.%n"));
					} else {
						if (step.getDateRule().isSequence()) {
							errorMsg.append(String.format("The sub date rule %s cannot be a sequence.%n",
									step.getDateRule().getName()));
						} else {
							if (step.getDuration() == null || step.getDuration().equals(Period.ZERO)) {
								errorMsg.append(String.format("The sub date rule %s cannot run for a duration of 0.%n",
										step.getDateRule().getName()));
							}
							try {
								validateDateRule(step.getDateRule());
							} catch (TradistaBusinessException tbe) {
								errorMsg.append(tbe.getMessage());
							}
						}
					}
				}
			}
		} else {
			if (dateRule.getDateRollingConvention() == null) {
				errorMsg.append(String.format("The date rolling convention cannot be null.%n"));
			}
			if (dateRule.getMonths() == null || dateRule.getMonths().isEmpty()) {
				errorMsg.append(String.format("There should be at least one month.%n"));
			}
			if (StringUtils.isBlank(dateRule.getPosition())) {
				errorMsg.append(String.format("The position cannot be empty.%n"));
			}
		}

		if (!errorMsg.isEmpty()) {
			throw new TradistaBusinessException(errorMsg.toString());
		}

	}

}
