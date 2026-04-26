package org.eclipse.tradista.core.marketdata.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.NAME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PROCESSING_ORG_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.QUOTE_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.QUOTE_SET_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.SURFACE_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.TYPE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.marketdata.model.VolatilitySurface;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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

public class SurfaceSQL {

	public static final String ALGORITHM = "ALGORITHM";
	public static final String INTERPOLATOR = "INTERPOLATOR";
	public static final String INSTANCE = "INSTANCE";
	public static final String QUOTE_DATE = "QUOTE_DATE";

	public static final Field NAME_FIELD = new Field(NAME);
	public static final Field ID_FIELD = new Field(ID);
	public static final Field TYPE_FIELD = new Field(TYPE);
	public static final Field ALGORITHM_FIELD = new Field(ALGORITHM);
	public static final Field INTERPOLATOR_FIELD = new Field(INTERPOLATOR);
	public static final Field INSTANCE_FIELD = new Field(INSTANCE);
	public static final Field QUOTE_DATE_FIELD = new Field(QUOTE_DATE);
	public static final Field QUOTE_SET_ID_FIELD = new Field(QUOTE_SET_ID);
	public static final Field PROCESSING_ORG_ID_FIELD = new Field(PROCESSING_ORG_ID);

	private static final Field[] ALL_FIELDS = { NAME_FIELD, ALGORITHM_FIELD, INTERPOLATOR_FIELD, INSTANCE_FIELD,
			QUOTE_DATE_FIELD, TYPE_FIELD, QUOTE_SET_ID_FIELD, PROCESSING_ORG_ID_FIELD, ID_FIELD };

	private static final Field[] FIELDS_FOR_INSERT_OR_UPDATE = { NAME_FIELD, ALGORITHM_FIELD, INTERPOLATOR_FIELD,
			INSTANCE_FIELD, QUOTE_DATE_FIELD, TYPE_FIELD, QUOTE_SET_ID_FIELD, PROCESSING_ORG_ID_FIELD };

	public static final Table VOLATILITY_SURFACE_TABLE = new Table("VOLATILITY_SURFACE", ALL_FIELDS);

	public static final Field SURFACE_ID_FIELD = new Field(SURFACE_ID);
	public static final Field QUOTE_ID_FIELD = new Field(QUOTE_ID);

	private static final Field[] QUOTE_FIELDS = { SURFACE_ID_FIELD, QUOTE_ID_FIELD };

	public static final Table VOLATILITY_SURFACE_QUOTE_TABLE = new Table("VOLATILITY_SURFACE_QUOTE", QUOTE_FIELDS);

	public static boolean surfaceExists(VolatilitySurface<?, ?, ?> surface, String type) {
		boolean exists = false;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(VOLATILITY_SURFACE_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		if (surface.getProcessingOrg() == null) {
			TradistaDBUtil.addIsNullFilter(sql, PROCESSING_ORG_ID_FIELD);
		} else {
			TradistaDBUtil.addParameterizedFilter(sql, PROCESSING_ORG_ID_FIELD);
		}
		TradistaDBUtil.addParameterizedFilter(sql, TYPE_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetSurfaceByName = con.prepareStatement(sql.toString())) {
			int i = 1;
			stmtGetSurfaceByName.setString(i++, surface.getName());
			if (surface.getProcessingOrg() != null) {
				stmtGetSurfaceByName.setLong(i++, surface.getProcessingOrg().getId());
			}
			stmtGetSurfaceByName.setString(i++, type);
			try (ResultSet results = stmtGetSurfaceByName.executeQuery()) {
				if (results.next()) {
					exists = true;
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return exists;
	}

	public static PreparedStatement getInsertPreparedStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, VOLATILITY_SURFACE_TABLE, FIELDS_FOR_INSERT_OR_UPDATE);
	}

	public static PreparedStatement getUpdatePreparedStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, ID_FIELD, VOLATILITY_SURFACE_TABLE,
				FIELDS_FOR_INSERT_OR_UPDATE);
	}

	public static PreparedStatement getQuoteInsertPreparedStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, VOLATILITY_SURFACE_QUOTE_TABLE, QUOTE_FIELDS);
	}

	public static PreparedStatement getQuoteDeletePreparedStatement(Connection con) {
		return TradistaDBUtil.buildDeletePreparedStatement(con, VOLATILITY_SURFACE_QUOTE_TABLE, SURFACE_ID_FIELD);
	}

}