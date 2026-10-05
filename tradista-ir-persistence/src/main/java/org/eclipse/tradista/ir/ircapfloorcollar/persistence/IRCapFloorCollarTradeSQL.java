package org.eclipse.tradista.ir.ircapfloorcollar.persistence;

import static org.eclipse.tradista.core.trade.persistence.TradeSQL.ID_FIELD;
import static org.eclipse.tradista.core.trade.persistence.TradeSQL.TRADE_TABLE;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.eclipse.tradista.core.book.persistence.BookSQL;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.currency.persistence.CurrencySQL;
import org.eclipse.tradista.core.daycountconvention.persistence.DayCountConventionSQL;
import org.eclipse.tradista.core.index.persistence.IndexSQL;
import org.eclipse.tradista.core.interestpayment.model.InterestPayment;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.core.trade.persistence.TradeSQL;
import org.eclipse.tradista.ir.ircapfloorcollar.model.IRCapFloorCollarTrade;
import org.eclipse.tradista.ir.irforward.model.IRForwardTrade;
import org.eclipse.tradista.ir.irforward.persistence.IRForwardTradeSQL;

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

public class IRCapFloorCollarTradeSQL {

	public static final Field IRCAP_FLOOR_COLLAR_TRADE_ID_FIELD = new Field("IRCAP_FLOOR_COLLAR_TRADE_ID");
	public static final Field CAP_STRIKE_FIELD = new Field("CAP_STRIKE");
	public static final Field FLOOR_STRIKE_FIELD = new Field("FLOOR_STRIKE");
	public static final Field IRFORWARD_TRADE_ID_FIELD = new Field("IRFORWARD_TRADE_ID");

	private static final Field[] IRCAP_FLOOR_COLLAR_TRADE_FIELDS = { IRCAP_FLOOR_COLLAR_TRADE_ID_FIELD,
			CAP_STRIKE_FIELD, FLOOR_STRIKE_FIELD, IRFORWARD_TRADE_ID_FIELD };

	private static final Field[] IRCAP_FLOOR_COLLAR_TRADE_FIELDS_FOR_INSERT = { CAP_STRIKE_FIELD, FLOOR_STRIKE_FIELD,
			IRFORWARD_TRADE_ID_FIELD, IRCAP_FLOOR_COLLAR_TRADE_ID_FIELD };

	private static final Field[] IRCAP_FLOOR_COLLAR_TRADE_FIELDS_FOR_UPDATE = { CAP_STRIKE_FIELD, FLOOR_STRIKE_FIELD,
			IRFORWARD_TRADE_ID_FIELD };

	public static final Table IRCAP_FLOOR_COLLAR_TRADE_TABLE = new Table("IRCAP_FLOOR_COLLAR_TRADE",
			IRCAP_FLOOR_COLLAR_TRADE_FIELDS);

	public static final Join TRADE_AND_IRCAP_FLOOR_COLLAR_TRADE_INNER_JOIN = Join.innerEq(TRADE_TABLE, ID_FIELD,
			IRCAP_FLOOR_COLLAR_TRADE_ID_FIELD);

	public static final String SQL_QUERY = TradistaDBUtil.buildSelectQuery(IRCAP_FLOOR_COLLAR_TRADE_TABLE,
			TRADE_AND_IRCAP_FLOOR_COLLAR_TRADE_INNER_JOIN);

	public static PreparedStatement getInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, IRCAP_FLOOR_COLLAR_TRADE_TABLE,
				IRCAP_FLOOR_COLLAR_TRADE_FIELDS_FOR_INSERT);
	}

	public static PreparedStatement getUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, IRCAP_FLOOR_COLLAR_TRADE_ID_FIELD,
				IRCAP_FLOOR_COLLAR_TRADE_TABLE, IRCAP_FLOOR_COLLAR_TRADE_FIELDS_FOR_UPDATE);
	}

	public static IRCapFloorCollarTrade getTradeById(long id) {
		IRCapFloorCollarTrade irCapFloorCollarTrade = null;
		StringBuilder query = new StringBuilder(SQL_QUERY);
		TradistaDBUtil.addParameterizedFilter(query, IRCAP_FLOOR_COLLAR_TRADE_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTradeById = con.prepareStatement(query.toString())) {
			stmtGetTradeById.setLong(1, id);
			try (ResultSet results = stmtGetTradeById.executeQuery()) {
				while (results.next()) {
					if (irCapFloorCollarTrade == null) {
						irCapFloorCollarTrade = IRCapFloorCollarTrade.of(TradeSQL.getCreationTime(results));
					}

					TradeSQL.setTradeCommonFields(irCapFloorCollarTrade, results);
					irCapFloorCollarTrade.setCapStrike(results.getBigDecimal(CAP_STRIKE_FIELD.getName()));
					irCapFloorCollarTrade.setFloorStrike(results.getBigDecimal(FLOOR_STRIKE_FIELD.getName()));

					// Building the IRForward
					IRForwardTrade<Product> irForward = IRForwardTradeSQL
							.getTradeById(results.getLong(IRFORWARD_TRADE_ID_FIELD.getName()));
					irCapFloorCollarTrade.setIrForwardTrade(irForward);

				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return irCapFloorCollarTrade;
	}

	public static IRCapFloorCollarTrade getTrade(ResultSet rs) {

		if (rs == null) {
			throw new TradistaTechnicalException("ResultSet cannot be null.");
		}

		IRCapFloorCollarTrade irCapFloorCollarTrade = null;
		try {
			if (rs.getLong(IRCAP_FLOOR_COLLAR_TRADE_ID_FIELD.getName()) == 0) {
				return null;
			}
			irCapFloorCollarTrade = IRCapFloorCollarTrade.of(TradeSQL.getCreationTime(rs));
			irCapFloorCollarTrade.setCapStrike(rs.getBigDecimal(CAP_STRIKE_FIELD.getName()));
			irCapFloorCollarTrade.setFloorStrike(rs.getBigDecimal(FLOOR_STRIKE_FIELD.getName()));

			// Commmon fields
			TradeSQL.setTradeCommonFields(irCapFloorCollarTrade, rs);

			// Building the IRForward
			java.sql.Timestamp undCreationTime = rs.getTimestamp("UND_IRFORWARD_CREATION_TIME");
			IRForwardTrade.ConcreteBuilder<Product> undBuilder = new IRForwardTrade.ConcreteBuilder<>();
			if (undCreationTime != null) {
				undBuilder.creationTime(undCreationTime.toInstant());
			}
			IRForwardTrade<Product> irForward = undBuilder.build();
			irForward.setId(rs.getLong("UND_IRFORWARD_ID"));
			irForward.setAmount(rs.getBigDecimal("UND_IRFORWARD_AMOUNT"));
			irForward.setBook(BookSQL.getBookById(rs.getLong("UND_IRFORWARD_BOOK_ID")));
			irForward.setBuySell(rs.getBoolean("UND_IRFORWARD_BUY_SELL"));
			irForward.setCurrency(CurrencySQL.getCurrencyById(rs.getLong("UND_IRFORWARD_CURRENCY_ID")));
			irForward.setFrequency(Tenor.valueOf(rs.getString("fwd_frequency")));
			java.sql.Date maturityDate = rs.getDate("fwd_maturity_date");
			if (maturityDate != null) {
				irForward.setMaturityDate(maturityDate.toLocalDate());
			}
			irForward.setDayCountConvention(
					DayCountConventionSQL.getDayCountConventionById(rs.getLong("fwd_day_count_convention_id")));
			irForward.setReferenceRateIndex(IndexSQL.getIndexById(rs.getLong("fwd_reference_rate_index_id")));
			irForward.setReferenceRateIndexTenor(Tenor.valueOf(rs.getString("fwd_reference_rate_index_tenor")));
			irForward.setInterestPayment(InterestPayment.valueOf(rs.getString("fwd_interest_payment")));
			irForward.setInterestFixing(InterestPayment.valueOf(rs.getString("fwd_interest_fixing")));
			Date undTradeDate = rs.getDate("und_irforward_trade_date");
			if (undTradeDate != null) {
				irForward.setTradeDate(undTradeDate.toLocalDate());
			}
			Date undSettlementDate = rs.getDate("und_irforward_settlement_date");
			if (undSettlementDate != null) {
				irForward.setSettlementDate(undSettlementDate.toLocalDate());
			}
			irForward.setCounterparty(irCapFloorCollarTrade.getCounterparty());

			irCapFloorCollarTrade.setIrForwardTrade(irForward);
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}

		return irCapFloorCollarTrade;
	}

	public static long saveIRCapFloorCollarTrade(IRCapFloorCollarTrade trade) {
		long tradeId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTrade = (trade.getId() == 0) ? TradeSQL.getInsertStatement(con)
						: TradeSQL.getUpdateStatement(con);
				PreparedStatement stmtSaveIRCapFloorCollarTrade = (trade.getId() == 0) ? getInsertStatement(con)
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

			// IR Forward saving
			long irForwardId = IRForwardTradeSQL.saveIRForwardTrade(trade.getIrForwardTrade());

			if (trade.getCapStrike() == null) {
				stmtSaveIRCapFloorCollarTrade.setNull(1, Types.DECIMAL);
			} else {
				stmtSaveIRCapFloorCollarTrade.setBigDecimal(1, trade.getCapStrike());
			}
			if (trade.getFloorStrike() == null) {
				stmtSaveIRCapFloorCollarTrade.setNull(2, Types.DECIMAL);
			} else {
				stmtSaveIRCapFloorCollarTrade.setBigDecimal(2, trade.getFloorStrike());
			}
			stmtSaveIRCapFloorCollarTrade.setLong(3, irForwardId);
			stmtSaveIRCapFloorCollarTrade.setLong(4, tradeId);
			stmtSaveIRCapFloorCollarTrade.executeUpdate();

		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		trade.setId(tradeId);
		return tradeId;
	}

}