package org.eclipse.tradista.core.marketdata.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.DATE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.NAME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PROCESSING_ORG_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.QUOTE_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.QUOTE_SET_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;

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

public final class CurveSQL {

	public static final String ALGORITHM = "ALGORITHM";
	public static final String INTERPOLATOR = "INTERPOLATOR";
	public static final String INSTANCE = "INSTANCE";
	public static final String QUOTE_DATE = "QUOTE_DATE";
	public static final String RATE = "RATE";
	public static final String CURVE_ID = "CURVE_ID";

	public static final Field ID_FIELD = new Field(ID);
	public static final Field NAME_FIELD = new Field(NAME);
	public static final Field ALGORITHM_FIELD = new Field(ALGORITHM);
	public static final Field INTERPOLATOR_FIELD = new Field(INTERPOLATOR);
	public static final Field INSTANCE_FIELD = new Field(INSTANCE);
	public static final Field QUOTE_DATE_FIELD = new Field(QUOTE_DATE);
	public static final Field QUOTE_SET_ID_FIELD = new Field(QUOTE_SET_ID);
	public static final Field PROCESSING_ORG_ID_FIELD = new Field(PROCESSING_ORG_ID);

	private static final Field[] CURVE_FIELDS = { NAME_FIELD, ALGORITHM_FIELD, INTERPOLATOR_FIELD, INSTANCE_FIELD,
			QUOTE_DATE_FIELD, QUOTE_SET_ID_FIELD, PROCESSING_ORG_ID_FIELD, ID_FIELD };

	private static final Field[] CURVE_FIELDS_FOR_INSERT_OR_UPDATE = { NAME_FIELD, ALGORITHM_FIELD, INTERPOLATOR_FIELD,
			INSTANCE_FIELD, QUOTE_DATE_FIELD, QUOTE_SET_ID_FIELD, PROCESSING_ORG_ID_FIELD };

	public static final Table CURVE_TABLE = new Table("CURVE", CURVE_FIELDS);

	public static final Field CURVE_POINT_CURVE_ID_FIELD = new Field(CURVE_ID);
	public static final Field CURVE_QUOTE_CURVE_ID_FIELD = new Field(CURVE_ID);
	public static final Field DATE_FIELD = new Field(DATE);
	public static final Field RATE_FIELD = new Field(RATE);

	private static final Field[] CURVE_POINT_FIELDS = { CURVE_POINT_CURVE_ID_FIELD, DATE_FIELD, RATE_FIELD };

	public static final Table CURVE_POINT_TABLE = new Table("CURVE_POINT", CURVE_POINT_FIELDS);

	private static final Field QUOTE_ID_FIELD = new Field(QUOTE_ID);

	private static final Field[] CURVE_QUOTE_FIELDS = { CURVE_QUOTE_CURVE_ID_FIELD, QUOTE_ID_FIELD };

	private static final Table CURVE_QUOTE_TABLE = new Table("CURVE_QUOTE", CURVE_QUOTE_FIELDS);

	public static PreparedStatement getInsertCurvePreparedStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, CURVE_TABLE, CURVE_FIELDS_FOR_INSERT_OR_UPDATE);
	}

	public static PreparedStatement getUpdateCurvePreparedStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, ID_FIELD, CURVE_TABLE,
				CURVE_FIELDS_FOR_INSERT_OR_UPDATE);
	}

	public static PreparedStatement getInsertCurvePointPreparedStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, CURVE_POINT_TABLE, CURVE_POINT_FIELDS);
	}

	public static PreparedStatement getInsertCurveQuotePreparedStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, CURVE_QUOTE_TABLE, CURVE_QUOTE_FIELDS);
	}

	public static PreparedStatement getDeleteCurvePointsByCurveIdPreparedStatement(Connection con) {
		return TradistaDBUtil.buildDeletePreparedStatement(con, CURVE_POINT_TABLE, CURVE_POINT_CURVE_ID_FIELD);
	}

	public static PreparedStatement getDeleteCurveQuotesByCurveIdPreparedStatement(Connection con) {
		return TradistaDBUtil.buildDeletePreparedStatement(con, CURVE_QUOTE_TABLE, CURVE_QUOTE_CURVE_ID_FIELD);
	}

	public static String getCurvePointSelectQuery() {
		return TradistaDBUtil.buildSelectQuery(CURVE_POINT_TABLE);
	}

	private CurveSQL() {
	}

}