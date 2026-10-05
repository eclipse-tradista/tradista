package org.eclipse.tradista.core.trade.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.MATURITY_DATE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.QUANTITY;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.TYPE;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.core.trade.model.VanillaOptionTrade;

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

public final class VanillaOptionTradeSQL {

	public static final Field VANILLA_OPTION_TRADE_ID_FIELD = new Field("VANILLA_OPTION_TRADE_ID");
	public static final Field STYLE_FIELD = new Field("STYLE");
	public static final Field TYPE_FIELD = new Field(TYPE);
	public static final Field STRIKE_FIELD = new Field("STRIKE");
	public static final Field MATURITY_DATE_FIELD = new Field(MATURITY_DATE);
	public static final Field EXERCISE_DATE_FIELD = new Field("EXERCISE_DATE");
	public static final Field UNDERLYING_TRADE_ID_FIELD = new Field("UNDERLYING_TRADE_ID");
	public static final Field SETTLEMENT_TYPE_FIELD = new Field("SETTLEMENT_TYPE");
	public static final Field SETTLEMENT_DATE_OFFSET_FIELD = new Field("SETTLEMENT_DATE_OFFSET");
	public static final Field QUANTITY_FIELD = new Field(QUANTITY);

	public static final Field[] VANILLA_OPTION_TRADE_FIELDS = { VANILLA_OPTION_TRADE_ID_FIELD, STYLE_FIELD, TYPE_FIELD,
			STRIKE_FIELD, MATURITY_DATE_FIELD, EXERCISE_DATE_FIELD, UNDERLYING_TRADE_ID_FIELD, SETTLEMENT_TYPE_FIELD,
			SETTLEMENT_DATE_OFFSET_FIELD, QUANTITY_FIELD };

	public static final Field[] VANILLA_OPTION_TRADE_FIELDS_FOR_INSERT = { STYLE_FIELD, TYPE_FIELD, STRIKE_FIELD,
			MATURITY_DATE_FIELD, EXERCISE_DATE_FIELD, UNDERLYING_TRADE_ID_FIELD, SETTLEMENT_TYPE_FIELD,
			SETTLEMENT_DATE_OFFSET_FIELD, QUANTITY_FIELD, VANILLA_OPTION_TRADE_ID_FIELD };

	public static final Field[] VANILLA_OPTION_TRADE_FIELDS_FOR_UPDATE = { STYLE_FIELD, TYPE_FIELD, STRIKE_FIELD,
			MATURITY_DATE_FIELD, EXERCISE_DATE_FIELD, UNDERLYING_TRADE_ID_FIELD, SETTLEMENT_TYPE_FIELD,
			SETTLEMENT_DATE_OFFSET_FIELD, QUANTITY_FIELD };

	public static final Table VANILLA_OPTION_TRADE_TABLE = new Table("VANILLA_OPTION_TRADE",
			VANILLA_OPTION_TRADE_FIELDS);

	public static final Join TRADE_AND_VANILLA_OPTION_TRADE_INNER_JOIN = Join.innerEq(TradeSQL.TRADE_TABLE,
			TradeSQL.ID_FIELD, VANILLA_OPTION_TRADE_ID_FIELD);

	private VanillaOptionTradeSQL() {
	}

	public static PreparedStatement getInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, VANILLA_OPTION_TRADE_TABLE,
				VANILLA_OPTION_TRADE_FIELDS_FOR_INSERT);
	}

	public static PreparedStatement getUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, VANILLA_OPTION_TRADE_ID_FIELD,
				VANILLA_OPTION_TRADE_TABLE, VANILLA_OPTION_TRADE_FIELDS_FOR_UPDATE);
	}

	public static void setPreparedStatementVanillaOptionFields(VanillaOptionTrade<?> trade, PreparedStatement stmt,
			long underlyingId, long tradeId, BigDecimal quantity) throws SQLException {
		stmt.setString(1, trade.getStyle().name());
		stmt.setString(2, trade.getType().name());
		stmt.setBigDecimal(3, trade.getStrike());
		stmt.setDate(4, Date.valueOf(trade.getMaturityDate()));
		LocalDate exerciseDate = trade.getExerciseDate();
		if (exerciseDate != null) {
			stmt.setDate(5, Date.valueOf(exerciseDate));
		} else {
			stmt.setNull(5, Types.DATE);
		}
		stmt.setLong(6, underlyingId);
		stmt.setString(7, trade.getSettlementType().name());
		stmt.setInt(8, trade.getSettlementDateOffset());
		if (quantity != null) {
			stmt.setBigDecimal(9, quantity);
		} else {
			stmt.setNull(9, Types.DECIMAL);
		}
		stmt.setLong(10, tradeId);
	}

	public static void setPreparedStatementVanillaOptionFields(VanillaOptionTrade<?> trade, PreparedStatement stmt,
			long underlyingId, long tradeId) throws SQLException {
		setPreparedStatementVanillaOptionFields(trade, stmt, underlyingId, tradeId, null);
	}

	public static void setVanillaOptionTradeCommonFields(VanillaOptionTrade<?> trade, ResultSet rs)
			throws SQLException {
		trade.setStyle(VanillaOptionTrade.Style.valueOf(rs.getString(STYLE_FIELD.getName())));
		trade.setType(OptionTrade.Type.valueOf(rs.getString(TYPE_FIELD.getName())));
		trade.setStrike(rs.getBigDecimal(STRIKE_FIELD.getName()));
		trade.setSettlementType(OptionTrade.SettlementType.valueOf(rs.getString(SETTLEMENT_TYPE_FIELD.getName())));
		trade.setSettlementDateOffset(rs.getInt(SETTLEMENT_DATE_OFFSET_FIELD.getName()));
		Date maturityDate = null;
		try {
			maturityDate = rs.getDate(MATURITY_DATE_FIELD.getName());
		} catch (SQLException _) {
			try {
				maturityDate = rs.getDate("option_maturity_date");
			} catch (SQLException _) {
			}
		}
		if (maturityDate != null) {
			trade.setMaturityDate(maturityDate.toLocalDate());
		}
		Date exerciseDate = rs.getDate(EXERCISE_DATE_FIELD.getName());
		if (exerciseDate != null) {
			trade.setExerciseDate(exerciseDate.toLocalDate());
		}
	}

}