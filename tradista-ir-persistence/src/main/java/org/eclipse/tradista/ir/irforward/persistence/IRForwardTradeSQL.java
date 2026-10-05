package org.eclipse.tradista.ir.irforward.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.FREQUENCY;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.MATURITY_DATE;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.ID_FIELD;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.TRADE_TABLE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.daycountconvention.persistence.DayCountConventionSQL;
import org.eclipse.tradista.core.index.persistence.IndexSQL;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.ir.irforward.model.IRForwardTrade;

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

public class IRForwardTradeSQL {

	public static final Field IRFORWARD_TRADE_ID_FIELD = new Field("IRFORWARD_TRADE_ID");
	public static final Field MATURITY_DATE_FIELD = new Field(MATURITY_DATE);
	public static final Field FREQUENCY_FIELD = new Field(FREQUENCY);
	public static final Field REFERENCE_RATE_INDEX_ID_FIELD = new Field("REFERENCE_RATE_INDEX_ID");
	public static final Field REFERENCE_RATE_INDEX_TENOR_FIELD = new Field("REFERENCE_RATE_INDEX_TENOR");
	public static final Field DAY_COUNT_CONVENTION_ID_FIELD = new Field("DAY_COUNT_CONVENTION_ID");
	public static final Field INTEREST_PAYMENT_FIELD = new Field("INTEREST_PAYMENT");
	public static final Field INTEREST_FIXING_FIELD = new Field("INTEREST_FIXING");

	private static final Field[] IRFORWARD_TRADE_FIELDS = { IRFORWARD_TRADE_ID_FIELD, MATURITY_DATE_FIELD,
			FREQUENCY_FIELD, REFERENCE_RATE_INDEX_ID_FIELD, REFERENCE_RATE_INDEX_TENOR_FIELD,
			DAY_COUNT_CONVENTION_ID_FIELD, INTEREST_PAYMENT_FIELD, INTEREST_FIXING_FIELD };

	private static final Field[] IRFORWARD_TRADE_FIELDS_FOR_INSERT = { MATURITY_DATE_FIELD, FREQUENCY_FIELD,
			REFERENCE_RATE_INDEX_ID_FIELD, REFERENCE_RATE_INDEX_TENOR_FIELD, DAY_COUNT_CONVENTION_ID_FIELD,
			INTEREST_PAYMENT_FIELD, INTEREST_FIXING_FIELD, IRFORWARD_TRADE_ID_FIELD };

	private static final Field[] IRFORWARD_TRADE_FIELDS_FOR_UPDATE = { MATURITY_DATE_FIELD, FREQUENCY_FIELD,
			REFERENCE_RATE_INDEX_ID_FIELD, REFERENCE_RATE_INDEX_TENOR_FIELD, DAY_COUNT_CONVENTION_ID_FIELD,
			INTEREST_PAYMENT_FIELD, INTEREST_FIXING_FIELD };

	public static final Table IRFORWARD_TRADE_TABLE = new Table("IRFORWARD_TRADE", IRFORWARD_TRADE_FIELDS);

	public static final Join TRADE_AND_IRFORWARD_TRADE_INNER_JOIN = Join.innerEq(TRADE_TABLE, ID_FIELD,
			IRFORWARD_TRADE_ID_FIELD);

	public static final String SQL_QUERY = TradistaDBUtil.buildSelectQuery(IRFORWARD_TRADE_TABLE,
			TRADE_AND_IRFORWARD_TRADE_INNER_JOIN);

	public static PreparedStatement getInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, IRFORWARD_TRADE_TABLE,
				IRFORWARD_TRADE_FIELDS_FOR_INSERT);
	}

	public static PreparedStatement getUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, IRFORWARD_TRADE_ID_FIELD, IRFORWARD_TRADE_TABLE,
				IRFORWARD_TRADE_FIELDS_FOR_UPDATE);
	}

	public static IRForwardTrade<Product> getTradeById(long id) {
		IRForwardTrade<Product> irforwardTrade = null;
		StringBuilder query = new StringBuilder(SQL_QUERY);
		TradistaDBUtil.addParameterizedFilter(query, IRFORWARD_TRADE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(query.toString())) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (irforwardTrade == null) {
						irforwardTrade = new IRForwardTrade.ConcreteBuilder<Product>()
								.creationTime(TradeSQL.getCreationTime(results)).build();
					}

					TradeSQL.setTradeCommonFields(irforwardTrade, results);
					java.sql.Date maturityDate = results.getDate(MATURITY_DATE_FIELD.getName());
					if (maturityDate != null) {
						irforwardTrade.setMaturityDate(maturityDate.toLocalDate());
					}
					String freq = results.getString(FREQUENCY_FIELD.getName());
					if (freq != null) {
						irforwardTrade.setFrequency(Tenor.valueOf(freq));
					}
					long indexId = results.getLong(REFERENCE_RATE_INDEX_ID_FIELD.getName());
					if (indexId != 0) {
						irforwardTrade.setReferenceRateIndex(IndexSQL.getIndexById(indexId));
					}
					String tenor = results.getString(REFERENCE_RATE_INDEX_TENOR_FIELD.getName());
					if (tenor != null) {
						irforwardTrade.setReferenceRateIndexTenor(Tenor.valueOf(tenor));
					}
					long dccId = results.getLong(DAY_COUNT_CONVENTION_ID_FIELD.getName());
					if (dccId != 0) {
						irforwardTrade.setDayCountConvention(DayCountConventionSQL.getDayCountConventionById(dccId));
					}
					String interestPayment = results.getString(INTEREST_PAYMENT_FIELD.getName());
					if (interestPayment != null) {
						irforwardTrade.setInterestPayment(InterestPayment.valueOf(interestPayment));
					}
					String interestFixing = results.getString(INTEREST_FIXING_FIELD.getName());
					if (interestFixing != null) {
						irforwardTrade.setInterestFixing(InterestPayment.valueOf(interestFixing));
					}
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return irforwardTrade;
	}

	public static void setPreparedStatementFields(IRForwardTrade<Product> trade,
			PreparedStatement stmtSaveIRForwardTrade, long tradeId) throws SQLException {
		if (trade.getMaturityDate() != null) {
			stmtSaveIRForwardTrade.setDate(1, java.sql.Date.valueOf(trade.getMaturityDate()));
		} else {
			stmtSaveIRForwardTrade.setNull(1, Types.DATE);
		}
		if (trade.getFrequency() != null) {
			stmtSaveIRForwardTrade.setString(2, trade.getFrequency().name());
		} else {
			stmtSaveIRForwardTrade.setNull(2, Types.VARCHAR);
		}
		if (trade.getReferenceRateIndex() != null) {
			stmtSaveIRForwardTrade.setLong(3, trade.getReferenceRateIndex().getId());
		} else {
			stmtSaveIRForwardTrade.setNull(3, Types.BIGINT);
		}
		if (trade.getReferenceRateIndexTenor() != null) {
			stmtSaveIRForwardTrade.setString(4, trade.getReferenceRateIndexTenor().name());
		} else {
			stmtSaveIRForwardTrade.setNull(4, Types.VARCHAR);
		}
		if (trade.getDayCountConvention() != null) {
			stmtSaveIRForwardTrade.setLong(5, trade.getDayCountConvention().getId());
		} else {
			stmtSaveIRForwardTrade.setNull(5, Types.BIGINT);
		}
		if (trade.getInterestPayment() != null) {
			stmtSaveIRForwardTrade.setString(6, trade.getInterestPayment().name());
		} else {
			stmtSaveIRForwardTrade.setNull(6, Types.VARCHAR);
		}
		if (trade.getInterestFixing() != null) {
			stmtSaveIRForwardTrade.setString(7, trade.getInterestFixing().name());
		} else {
			stmtSaveIRForwardTrade.setNull(7, Types.VARCHAR);
		}
		if (trade.getId() == 0) {
			stmtSaveIRForwardTrade.setLong(8, tradeId);
		} else {
			stmtSaveIRForwardTrade.setLong(8, trade.getId());
		}
	}

	public static long saveIRForwardTrade(IRForwardTrade<Product> trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? TradeSQL.getInsertStatement(con)
						: TradeSQL.getUpdateStatement(con);
				PreparedStatement stmtSaveIRForwardTrade = (trade.getId() == 0) ? getInsertStatement(con)
						: getUpdateStatement(con)) {
			TradeSQL.setPreparedStatementCommonFields(trade, stmtSaveTrade);
			stmtSaveTrade.executeUpdate();

			if (trade.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveTrade.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						tradeId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating trade failed, no generated key obtained.");
					}
				}
			} else {
				tradeId = trade.getId();
			}

			setPreparedStatementFields(trade, stmtSaveIRForwardTrade, tradeId);
			stmtSaveIRForwardTrade.executeUpdate();

		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		trade.setId(tradeId);
		return tradeId;
	}
}