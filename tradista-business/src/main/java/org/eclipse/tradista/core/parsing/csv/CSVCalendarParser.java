package org.eclipse.tradista.core.parsing.csv;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.csv.CSVRecord;

import org.eclipse.tradista.core.calendar.model.Calendar;
import org.eclipse.tradista.core.parsing.parser.TradistaObjectParser;

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
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

public class CSVCalendarParser extends TradistaObjectParser<Calendar, CSVRecord> {

	@Override
	public Calendar parse(CSVRecord csvRecord) {
		Calendar calendar = new Calendar(csvRecord.get(2));
		Set<LocalDate> holidays = new HashSet<LocalDate>();
		holidays.add(LocalDate.parse(csvRecord.get(1), DateTimeFormatter.ISO_DATE));
		calendar.setHolidays(holidays);
		return calendar;
	}

}
