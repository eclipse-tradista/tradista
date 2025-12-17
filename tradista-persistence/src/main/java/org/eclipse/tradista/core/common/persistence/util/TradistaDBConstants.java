package org.eclipse.tradista.core.common.persistence.util;

/********************************************************************************
 * Copyright (c) 2025 Olivier Asuncion
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

public final class TradistaDBConstants {

	public static final String SELECT = "SELECT ";
	public static final String FROM = " FROM ";
	public static final String WHERE = " WHERE ";
	public static final String AND = " AND ";
	public static final String IN = " IN ";
	public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
	public static final String MM_DD_YYYY = "MM/dd/yyyy";

	// Common field names.
	public static final String ID = "ID";
	public static final String TYPE = "TYPE";
	public static final String STATUS = "STATUS";
	public static final String NAME = "NAME";
	public static final String STATUS_ID = "STATUS_ID";
	public static final String PROCESSING_ORG_ID = "PROCESSING_ORG_ID";
	public static final String BOOK_ID = "BOOK_ID";
	public static final String CREATION_DATE = "CREATION_DATE";
	public static final String LAST_UPDATE_DATE = "LAST_UPDATE_DATE";

	private TradistaDBConstants() {
	}

}