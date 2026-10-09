package org.eclipse.tradista.core.daterule.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.daterollconvention.model.DateRollingConvention;
import org.eclipse.tradista.core.daterule.model.DateRule;
import org.junit.jupiter.api.Test;

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

public class DateRuleValidatorTest {

	private static final DateRuleValidator validator = new DateRuleValidator();

	private DateRule createValidStandardDateRule(String name) throws TradistaBusinessException {
		DateRule dateRule = new DateRule(name);
		dateRule.setDateRollingConvention(DateRollingConvention.MODIFIED_FOLLOWING_BUSINESS_DAY);
		Set<Month> months = new HashSet<>();
		months.add(Month.MARCH);
		months.add(Month.JUNE);
		months.add(Month.SEPTEMBER);
		months.add(Month.DECEMBER);
		dateRule.setMonths(months);
		dateRule.setDay(DayOfWeek.WEDNESDAY);
		dateRule.setPosition("3rd");
		return dateRule;
	}

	@Test
	public void testValidStandardDateRule() throws TradistaBusinessException {
		DateRule dateRule = createValidStandardDateRule("IMM_QUARTERLY");
		assertDoesNotThrow(() -> validator.validateDateRule(dateRule));
	}

	@Test
	public void testValidSequenceDateRule() throws TradistaBusinessException {
		DateRule sequenceRule = new DateRule("SEQUENCE_RULE");
		sequenceRule.setSequence(true);

		DateRule subRule1 = createValidStandardDateRule("SUB_RULE_1");
		DateRule subRule2 = createValidStandardDateRule("SUB_RULE_2");

		List<DateRule.Step> steps = new ArrayList<>();
		steps.add(new DateRule.Step(1, subRule1, Period.ofYears(1)));
		steps.add(new DateRule.Step(2, subRule2, Period.ofYears(2)));
		sequenceRule.setSteps(steps);

		assertDoesNotThrow(() -> validator.validateDateRule(sequenceRule));
	}

	@Test
	public void testNullDateRule() {
		assertThrows(TradistaBusinessException.class, () -> validator.validateDateRule(null));
	}

	@Test
	public void testBlankName() throws TradistaBusinessException {
		DateRule nullName = createValidStandardDateRule("VALID");
		// DateRule only sets name via constructor; instantiate with null/blank
		DateRule dateRuleNull = new DateRule(null);
		dateRuleNull.setDateRollingConvention(DateRollingConvention.MODIFIED_FOLLOWING_BUSINESS_DAY);
		dateRuleNull.setMonths(Set.of(Month.JANUARY));
		dateRuleNull.setPosition("1st");
		assertThrows(TradistaBusinessException.class, () -> validator.validateDateRule(dateRuleNull));

		DateRule dateRuleBlank = new DateRule("   ");
		dateRuleBlank.setDateRollingConvention(DateRollingConvention.MODIFIED_FOLLOWING_BUSINESS_DAY);
		dateRuleBlank.setMonths(Set.of(Month.JANUARY));
		dateRuleBlank.setPosition("1st");
		assertThrows(TradistaBusinessException.class, () -> validator.validateDateRule(dateRuleBlank));
	}

	@Test
	public void testStandardDateRuleNullRollingConvention() throws TradistaBusinessException {
		DateRule dateRule = createValidStandardDateRule("TEST_RULE");
		dateRule.setDateRollingConvention(null);
		assertThrows(TradistaBusinessException.class, () -> validator.validateDateRule(dateRule));
	}

	@Test
	public void testStandardDateRuleEmptyMonths() throws TradistaBusinessException {
		DateRule dateRule = createValidStandardDateRule("TEST_RULE");
		dateRule.setMonths(null);
		assertThrows(TradistaBusinessException.class, () -> validator.validateDateRule(dateRule));

		dateRule.setMonths(new HashSet<>());
		assertThrows(TradistaBusinessException.class, () -> validator.validateDateRule(dateRule));
	}

	@Test
	public void testSequenceWithoutSteps() {
		DateRule sequenceRule = new DateRule("EMPTY_SEQUENCE");
		sequenceRule.setSequence(true);
		sequenceRule.setSteps(null);
		assertThrows(TradistaBusinessException.class, () -> validator.validateDateRule(sequenceRule));

		sequenceRule.setSteps(new ArrayList<>());
		assertThrows(TradistaBusinessException.class, () -> validator.validateDateRule(sequenceRule));
	}

	@Test
	public void testSequenceContainingItself() throws TradistaBusinessException {
		DateRule sequenceRule = new DateRule("SELF_CONTAINING_SEQUENCE");
		sequenceRule.setSequence(true);

		List<DateRule.Step> steps = new ArrayList<>();
		steps.add(new DateRule.Step(1, sequenceRule, Period.ofYears(1)));
		sequenceRule.setSteps(steps);

		assertThrows(TradistaBusinessException.class, () -> validator.validateDateRule(sequenceRule));
	}

	@Test
	public void testSequenceContainingSequenceSubRule() throws TradistaBusinessException {
		DateRule parentSequence = new DateRule("PARENT_SEQUENCE");
		parentSequence.setSequence(true);

		DateRule childSequence = new DateRule("CHILD_SEQUENCE");
		childSequence.setSequence(true);

		List<DateRule.Step> steps = new ArrayList<>();
		steps.add(new DateRule.Step(1, childSequence, Period.ofYears(1)));
		parentSequence.setSteps(steps);

		assertThrows(TradistaBusinessException.class, () -> validator.validateDateRule(parentSequence));
	}

	@Test
	public void testSequenceWithZeroDurationStep() throws TradistaBusinessException {
		DateRule sequenceRule = new DateRule("ZERO_DURATION_SEQUENCE");
		sequenceRule.setSequence(true);

		DateRule subRule = createValidStandardDateRule("SUB_RULE");

		List<DateRule.Step> steps = new ArrayList<>();
		steps.add(new DateRule.Step(1, subRule, Period.ZERO));
		sequenceRule.setSteps(steps);

		assertThrows(TradistaBusinessException.class, () -> validator.validateDateRule(sequenceRule));
	}

	@Test
	public void testSequenceWithNullDurationStep() throws TradistaBusinessException {
		DateRule sequenceRule = new DateRule("NULL_DURATION_SEQUENCE");
		sequenceRule.setSequence(true);

		DateRule subRule = createValidStandardDateRule("SUB_RULE");

		List<DateRule.Step> steps = new ArrayList<>();
		steps.add(new DateRule.Step(1, subRule, null));
		sequenceRule.setSteps(steps);

		assertThrows(TradistaBusinessException.class, () -> validator.validateDateRule(sequenceRule));
	}

	@Test
	public void testSequenceWithNullSubRule() {
		DateRule sequenceRule = new DateRule("NULL_SUBRULE_SEQUENCE");
		sequenceRule.setSequence(true);

		List<DateRule.Step> steps = new ArrayList<>();
		steps.add(new DateRule.Step(1, null, Period.ofYears(1)));
		sequenceRule.setSteps(steps);

		assertThrows(TradistaBusinessException.class, () -> validator.validateDateRule(sequenceRule));
	}

}
