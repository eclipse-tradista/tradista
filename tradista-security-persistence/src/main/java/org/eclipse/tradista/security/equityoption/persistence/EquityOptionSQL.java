package org.eclipse.tradista.security.equityoption.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.CODE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.CREATION_TIME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.MATURITY_DATE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.NAME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PRODUCT_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.TYPE;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.product.persistence.ProductSQL;
import org.eclipse.tradista.core.trade.model.OptionTrade;
import org.eclipse.tradista.security.equity.persistence.EquitySQL;
import org.eclipse.tradista.security.equityoption.model.EquityOption;

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

public class EquityOptionSQL {

	private static final Field CREATION_TIME_FIELD = new Field(CREATION_TIME);

	private static final Field EQUITY_OPTION_PRODUCT_ID_FIELD = new Field(PRODUCT_ID);
	private static final Field CODE_FIELD = new Field(CODE);
	private static final Field TYPE_FIELD = new Field(TYPE);
	private static final Field STRIKE_FIELD = new Field("STRIKE");
	private static final Field EQUITY_ID_FIELD = new Field("EQUITY_ID");
	private static final Field MATURITY_DATE_FIELD = new Field(MATURITY_DATE);
	private static final Field EQUITY_OPTION_CONTRACT_SPECIFICATION_ID_FIELD = new Field(
			"EQUITY_OPTION_CONTRACT_SPECIFICATION_ID");

	private static final Field[] EQUITY_OPTION_FIELDS = { CODE_FIELD, TYPE_FIELD, STRIKE_FIELD, EQUITY_ID_FIELD,
			MATURITY_DATE_FIELD, EQUITY_OPTION_CONTRACT_SPECIFICATION_ID_FIELD, EQUITY_OPTION_PRODUCT_ID_FIELD };
	public static final Table EQUITY_OPTION_TABLE = new Table("EQUITY_OPTION", EQUITY_OPTION_FIELDS);

	private static final Field[] EQUITY_OPTION_FIELDS_FOR_UPDATE = { CODE_FIELD, TYPE_FIELD, STRIKE_FIELD,
			EQUITY_ID_FIELD, MATURITY_DATE_FIELD, EQUITY_OPTION_CONTRACT_SPECIFICATION_ID_FIELD };

	private static final Field CONTRACT_SPEC_ID_FIELD = new Field(ID);
	private static final Field CONTRACT_SPEC_NAME_FIELD = new Field(NAME);
	private static final Field[] CONTRACT_SPEC_FIELDS = { CONTRACT_SPEC_ID_FIELD, CONTRACT_SPEC_NAME_FIELD };
	public static final Table EQUITY_OPTION_CONTRACT_SPECIFICATION_TABLE = new Table(
			"EQUITY_OPTION_CONTRACT_SPECIFICATION", CONTRACT_SPEC_FIELDS);

	private static final Join EQUITY_OPTION_PRODUCT_JOIN = Join.innerEq(ProductSQL.PRODUCT_TABLE,
			EQUITY_OPTION_PRODUCT_ID_FIELD, ProductSQL.ID_FIELD);

	private static final String BASE_SELECT_QUERY = TradistaDBUtil.buildSelectQuery(EQUITY_OPTION_TABLE,
			EQUITY_OPTION_PRODUCT_JOIN);

	public static long saveEquityOption(EquityOption equityOption) {
		long productId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveProduct = (equityOption.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, ProductSQL.PRODUCT_TABLE,
								ProductSQL.PRODUCT_FIELDS_FOR_INSERT)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, ProductSQL.ID_FIELD,
								ProductSQL.PRODUCT_TABLE, ProductSQL.PRODUCT_FIELDS_FOR_UPDATE);
				PreparedStatement stmtSaveEquityOption = (equityOption.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, EQUITY_OPTION_TABLE, EQUITY_OPTION_FIELDS)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, EQUITY_OPTION_PRODUCT_ID_FIELD,
								EQUITY_OPTION_TABLE, EQUITY_OPTION_FIELDS_FOR_UPDATE)) {
			if (equityOption.getId() == 0) {
				stmtSaveProduct.setTimestamp(1, Timestamp.from(equityOption.getCreationTime()));
				stmtSaveProduct.setTimestamp(2, Timestamp.from(equityOption.getLastUpdateTime()));
				stmtSaveProduct.setLong(3, equityOption.getExchange().getId());
			} else {
				stmtSaveProduct.setTimestamp(1, Timestamp.from(Instant.now()));
				stmtSaveProduct.setLong(2, equityOption.getExchange().getId());
				stmtSaveProduct.setLong(3, equityOption.getId());
			}
			stmtSaveProduct.executeUpdate();

			if (equityOption.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveProduct.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						productId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating equity option failed, no generated key obtained.");
					}
				}
			} else {
				productId = equityOption.getId();
			}

			stmtSaveEquityOption.setString(1, equityOption.getCode());
			stmtSaveEquityOption.setString(2, equityOption.getType().name());
			stmtSaveEquityOption.setBigDecimal(3, equityOption.getStrike());
			stmtSaveEquityOption.setLong(4, equityOption.getUnderlying().getId());
			stmtSaveEquityOption.setDate(5, Date.valueOf(equityOption.getMaturityDate()));
			stmtSaveEquityOption.setLong(6, equityOption.getEquityOptionContractSpecification().getId());
			stmtSaveEquityOption.setLong(7, productId);
			stmtSaveEquityOption.executeUpdate();

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		equityOption.setId(productId);
		return productId;
	}

	public static Set<EquityOption> getEquityOptionsByCreationDate(LocalDate date) {
		Set<EquityOption> equityOptions = null;
		StringBuilder sql = new StringBuilder(BASE_SELECT_QUERY);
		TradistaDBUtil.addParameterizedFilter(sql, CREATION_TIME_FIELD, true);
		TradistaDBUtil.addParameterizedFilter(sql, CREATION_TIME_FIELD, false);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquityOptionsByCreationDate = con.prepareStatement(sql.toString())) {
			stmtGetEquityOptionsByCreationDate.setTimestamp(1, Timestamp.valueOf(date.atStartOfDay()));
			stmtGetEquityOptionsByCreationDate.setTimestamp(2, Timestamp.valueOf(date.atTime(23, 59, 59, 999999999)));
			try (ResultSet results = stmtGetEquityOptionsByCreationDate.executeQuery()) {
				while (results.next()) {
					if (equityOptions == null) {
						equityOptions = new HashSet<>();
					}
					equityOptions.add(buildEquityOption(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return equityOptions;
	}

	public static Set<EquityOption> getEquityOptionsByCreationDate(LocalDate minDate, LocalDate maxDate) {
		Set<EquityOption> equityOptions = null;
		StringBuilder sql = new StringBuilder(BASE_SELECT_QUERY);
		if (minDate != null) {
			TradistaDBUtil.addFilter(sql, CREATION_TIME_FIELD, minDate.atStartOfDay(), true);
		}
		if (maxDate != null) {
			TradistaDBUtil.addFilter(sql, CREATION_TIME_FIELD, maxDate.atTime(23, 59, 59), false);
		}

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquityOptionsByCreationDate = con.prepareStatement(sql.toString());
				ResultSet results = stmtGetEquityOptionsByCreationDate.executeQuery()) {
			while (results.next()) {
				if (equityOptions == null) {
					equityOptions = new HashSet<>();
				}
				equityOptions.add(buildEquityOption(results));
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return equityOptions;
	}

	public static Set<EquityOption> getAllEquityOptions() {
		Set<EquityOption> equityOptions = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllEquityOptions = con.prepareStatement(BASE_SELECT_QUERY);
				ResultSet results = stmtGetAllEquityOptions.executeQuery()) {
			while (results.next()) {
				if (equityOptions == null) {
					equityOptions = new HashSet<>();
				}
				equityOptions.add(buildEquityOption(results));
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return equityOptions;
	}

	public static EquityOption getEquityOptionById(long id) {
		EquityOption equityOption = null;
		StringBuilder sql = new StringBuilder(BASE_SELECT_QUERY);
		TradistaDBUtil.addParameterizedFilter(sql, ProductSQL.ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquityOptionById = con.prepareStatement(sql.toString())) {
			stmtGetEquityOptionById.setLong(1, id);
			try (ResultSet results = stmtGetEquityOptionById.executeQuery()) {
				while (results.next()) {
					equityOption = buildEquityOption(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return equityOption;
	}

	public static Set<EquityOption> getEquityOptionsByCode(String code) {
		Set<EquityOption> equityOptions = null;
		StringBuilder sql = new StringBuilder(BASE_SELECT_QUERY);
		TradistaDBUtil.addParameterizedFilter(sql, CODE_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquityOptionByCode = con.prepareStatement(sql.toString())) {
			stmtGetEquityOptionByCode.setString(1, code);
			try (ResultSet results = stmtGetEquityOptionByCode.executeQuery()) {
				while (results.next()) {
					if (equityOptions == null) {
						equityOptions = new HashSet<>();
					}
					equityOptions.add(buildEquityOption(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return equityOptions;
	}

	public static EquityOption getEquityOptionByCodeTypeStrikeMaturityDateAndContractSpecificationName(String code,
			OptionTrade.Type type, BigDecimal strike, LocalDate maturityDate, String contractSpecificationName) {
		EquityOption equityOption = null;
		Join contractSpecJoin = Join.innerEq(EQUITY_OPTION_CONTRACT_SPECIFICATION_TABLE,
				EQUITY_OPTION_CONTRACT_SPECIFICATION_ID_FIELD, CONTRACT_SPEC_ID_FIELD);
		StringBuilder sql = new StringBuilder(
				TradistaDBUtil.buildSelectQuery(EQUITY_OPTION_TABLE, EQUITY_OPTION_PRODUCT_JOIN, contractSpecJoin));
		TradistaDBUtil.addParameterizedFilter(sql, CODE_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, TYPE_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, MATURITY_DATE_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, CONTRACT_SPEC_NAME_FIELD);
		if (strike != null) {
			TradistaDBUtil.addParameterizedFilter(sql, STRIKE_FIELD);
		}

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(sql.toString())) {
			stmt.setString(1, code);
			stmt.setString(2, type.name());
			stmt.setDate(3, Date.valueOf(maturityDate));
			stmt.setString(4, contractSpecificationName);
			if (strike != null) {
				stmt.setBigDecimal(5, strike);
			}
			try (ResultSet results = stmt.executeQuery()) {
				while (results.next()) {
					equityOption = buildEquityOption(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return equityOption;
	}

	private static EquityOption buildEquityOption(ResultSet results) throws SQLException {
		EquityOption.Builder builder = EquityOption
				.builder(results.getString(CODE_FIELD.getName()),
						OptionTrade.Type.valueOf(results.getString(TYPE_FIELD.getName())),
						results.getBigDecimal(STRIKE_FIELD.getName()),
						results.getDate(MATURITY_DATE_FIELD.getName()).toLocalDate(),
						EquityOptionContractSpecificationSQL.getEquityOptionContractSpecificationById(
								results.getLong(EQUITY_OPTION_CONTRACT_SPECIFICATION_ID_FIELD.getName())))
				.id(results.getLong(ProductSQL.ID_FIELD.getName()))
				.underlying(EquitySQL.getEquityById(results.getLong(EQUITY_ID_FIELD.getName())));
		Timestamp creationTimestamp = results.getTimestamp(CREATION_TIME_FIELD.getName());
		if (creationTimestamp != null) {
			builder.creationTime(creationTimestamp.toInstant());
		}
		return builder.build();
	}

}