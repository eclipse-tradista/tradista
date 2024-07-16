package finance.tradista.core.calendar.model;

import org.apache.commons.lang3.StringUtils;

/********************************************************************************
 * Copyright (c) 2021 Olivier Asuncion
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

public final class BlankCalendar extends Calendar {

	private static final long serialVersionUID = 3329487877473708646L;

	private static final BlankCalendar instance = new BlankCalendar();

	private BlankCalendar() {
		super(StringUtils.EMPTY);
	}

	public static BlankCalendar getInstance() {
		return instance;
	}

}