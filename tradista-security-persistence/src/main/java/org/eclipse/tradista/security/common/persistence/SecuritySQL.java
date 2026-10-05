package org.eclipse.tradista.security.common.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.CURRENCY_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PRODUCT_ID;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.currency.persistence.CurrencySQL;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.security.common.model.Security;

/********************************************************************************
 * Copyright (c) 2014 Olivier Asuncion
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

public final class SecuritySQL {

	public static final Field SECURITY_PRODUCT_ID_FIELD = new Field(PRODUCT_ID);
	public static final Field ISSUER_ID_FIELD = new Field("ISSUER_ID");
	public static final Field ISIN_FIELD = new Field("ISIN");
	public static final Field CURRENCY_ID_FIELD = new Field(CURRENCY_ID);
	public static final Field ISSUE_DATE_FIELD = new Field("ISSUE_DATE");
	public static final Field ISSUE_PRICE_FIELD = new Field("ISSUE_PRICE");

	public static final Field[] SECURITY_FIELDS = { ISSUER_ID_FIELD, ISIN_FIELD, CURRENCY_ID_FIELD, ISSUE_DATE_FIELD,
			ISSUE_PRICE_FIELD, SECURITY_PRODUCT_ID_FIELD };
	public static final Table SECURITY_TABLE = new Table("SECURITY", SECURITY_FIELDS);

	public static final Field[] SECURITY_FIELDS_FOR_UPDATE = { ISSUER_ID_FIELD, ISIN_FIELD, CURRENCY_ID_FIELD,
			ISSUE_DATE_FIELD, ISSUE_PRICE_FIELD };

	private SecuritySQL() {
	}

	public static PreparedStatement getInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, SECURITY_TABLE, SECURITY_FIELDS);
	}

	public static PreparedStatement getUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, SECURITY_PRODUCT_ID_FIELD, SECURITY_TABLE,
				SECURITY_FIELDS_FOR_UPDATE);
	}

	public static void setPreparedStatementSecurityFields(Security security, PreparedStatement stmtSaveSecurity,
			long productId) throws SQLException {
		stmtSaveSecurity.setLong(1, security.getIssuerId());
		stmtSaveSecurity.setString(2, security.getIsin());
		stmtSaveSecurity.setLong(3, security.getCurrencyId());
		stmtSaveSecurity.setDate(4, Date.valueOf(security.getIssueDate()));
		stmtSaveSecurity.setBigDecimal(5, security.getIssuePrice());
		stmtSaveSecurity.setLong(6, productId);
	}

	public static void setSecurityCommonFields(Security security, ResultSet results) throws SQLException {
		security.setIssuer(LegalEntitySQL.getLegalEntityById(results.getLong(ISSUER_ID_FIELD.getName())));
		security.setIssueDate(results.getDate(ISSUE_DATE_FIELD.getName()).toLocalDate());
		security.setIssuePrice(results.getBigDecimal(ISSUE_PRICE_FIELD.getName()));
		security.setCurrency(CurrencySQL.getCurrencyById(results.getLong(CURRENCY_ID_FIELD.getName())));
	}

}