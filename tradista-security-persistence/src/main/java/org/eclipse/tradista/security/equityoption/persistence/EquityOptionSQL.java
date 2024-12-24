package org.eclipse.tradista.security.equityoption.persistence;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
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

	public static long saveEquityOption(EquityOption equityOption) {
		long productId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveProduct = (equityOption.getId() == 0)
						? con.prepareStatement("INSERT INTO PRODUCT(CREATION_DATE, EXCHANGE_ID) VALUES (?, ?) ",
								Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement("UPDATE PRODUCT SET CREATION_DATE=?, EXCHANGE_ID=? WHERE ID=?");
				PreparedStatement stmtSaveEquityOption = (equityOption.getId() == 0) ? con.prepareStatement(
						"INSERT INTO EQUITY_OPTION(CODE, TYPE, STRIKE, EQUITY_ID, MATURITY_DATE, EQUITY_OPTION_CONTRACT_SPECIFICATION_ID, PRODUCT_ID) VALUES (?, ?, ?, ?, ?, ?, ?) ")
						: con.prepareStatement(
								"UPDATE EQUITY_OPTION SET CODE=?, TYPE=?, STRIKE=?, EQUITY_ID=?, MATURITY_DATE=?, EQUITY_OPTION_CONTRACT_SPECIFICATION_ID=? WHERE PRODUCT_ID=?")) {
			if (equityOption.getId() != 0) {
				stmtSaveProduct.setLong(3, equityOption.getId());
			}
			stmtSaveProduct.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
			stmtSaveProduct.setLong(2, equityOption.getExchange().getId());
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
			stmtSaveEquityOption.setDate(5, java.sql.Date.valueOf(equityOption.getMaturityDate()));
			stmtSaveEquityOption.setLong(6, equityOption.getEquityOptionContractSpecification().getId());
			stmtSaveEquityOption.setLong(7, productId);
			stmtSaveEquityOption.executeUpdate();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		equityOption.setId(productId);
		return productId;
	}

	public static Set<EquityOption> getEquityOptionsByCreationDate(LocalDate date) {
		Set<EquityOption> equityOptions = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquityOptionsByCreationDate = con
						.prepareStatement("SELECT * FROM EQUITY_OPTION, PRODUCT WHERE "
								+ "EQUITY_OPTION.PRODUCT_ID = PRODUCT.ID AND CREATION_DATE = ? ")) {
			stmtGetEquityOptionsByCreationDate.setDate(1, java.sql.Date.valueOf(date));
			try (ResultSet results = stmtGetEquityOptionsByCreationDate.executeQuery()) {
				while (results.next()) {
					EquityOption equityOption = new EquityOption(results.getString("code"),
							OptionTrade.Type.valueOf(results.getString("type")), results.getBigDecimal("strike"),
							results.getDate("maturity_date").toLocalDate(),
							EquityOptionContractSpecificationSQL.getEquityOptionContractSpecificationById(
									results.getLong("equity_option_contract_specification_id")));
					equityOption.setId(results.getLong("id"));
					equityOption.setUnderlying(EquitySQL.getEquityById(results.getLong("equity_id")));
					equityOption.setCreationDate(results.getDate("creation_date").toLocalDate());
					if (equityOptions == null) {
						equityOptions = new HashSet<EquityOption>();
					}
					equityOptions.add(equityOption);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return equityOptions;
	}

	public static Set<EquityOption> getEquityOptionsByCreationDate(LocalDate minDate, LocalDate maxDate) {
		Set<EquityOption> equityOptions = null;

		try (Connection con = TradistaDB.getConnection();
				Statement stmtGetEquityOptionsByCreationDate = con.createStatement()) {
			String dateQuery = null;
			String query = "SELECT * FROM EQUITY_OPTION, PRODUCT WHERE EQUITY_OPTION.PRODUCT_ID = PRODUCT.ID";
			if (minDate != null || maxDate != null) {
				if (minDate == null) {
					dateQuery = " AND CREATION_DATE <= '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(maxDate)
							+ "'";
				} else if (maxDate == null) {
					dateQuery = " AND CREATION_DATE >= '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(minDate)
							+ "'";
				} else {
					dateQuery = " AND CREATION_DATE BETWEEN '"
							+ DateTimeFormatter.ofPattern("MM/dd/yyyy").format(minDate) + "' AND '"
							+ DateTimeFormatter.ofPattern("MM/dd/yyyy").format(maxDate) + "'";
				}
				query += dateQuery;
			}

			try (ResultSet results = stmtGetEquityOptionsByCreationDate.executeQuery(query)) {

				while (results.next()) {
					EquityOption equityOption = new EquityOption(results.getString("code"),
							OptionTrade.Type.valueOf(results.getString("type")), results.getBigDecimal("strike"),
							results.getDate("maturity_date").toLocalDate(),
							EquityOptionContractSpecificationSQL.getEquityOptionContractSpecificationById(
									results.getLong("equity_option_contract_specification_id")));
					equityOption.setId(results.getLong("id"));
					equityOption.setUnderlying(EquitySQL.getEquityById(results.getLong("equity_id")));
					equityOption.setCreationDate(results.getDate("creation_date").toLocalDate());
					if (equityOptions == null) {
						equityOptions = new HashSet<EquityOption>();
					}
					equityOptions.add(equityOption);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return equityOptions;
	}

	public static Set<EquityOption> getAllEquityOptions() {
		Set<EquityOption> equityOptions = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllEquityOptions = con.prepareStatement(
						"SELECT * FROM EQUITY_OPTION, PRODUCT WHERE EQUITY_OPTION.PRODUCT_ID = PRODUCT.ID");
				ResultSet results = stmtGetAllEquityOptions.executeQuery()) {
			while (results.next()) {
				EquityOption equityOption = new EquityOption(results.getString("code"),
						OptionTrade.Type.valueOf(results.getString("type")), results.getBigDecimal("strike"),
						results.getDate("maturity_date").toLocalDate(),
						EquityOptionContractSpecificationSQL.getEquityOptionContractSpecificationById(
								results.getLong("equity_option_contract_specification_id")));
				equityOption.setId(results.getLong("id"));
				equityOption.setUnderlying(EquitySQL.getEquityById(results.getLong("equity_id")));
				equityOption.setCreationDate(results.getDate("creation_date").toLocalDate());
				if (equityOptions == null) {
					equityOptions = new HashSet<EquityOption>();
				}
				equityOptions.add(equityOption);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return equityOptions;
	}

	public static EquityOption getEquityOptionById(long id) {
		EquityOption equityOption = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquityOptionById = con.prepareStatement("SELECT * "
						+ "FROM EQUITY_OPTION, PRODUCT WHERE EQUITY_OPTION.PRODUCT_ID = PRODUCT.ID AND PRODUCT.ID = ?")) {
			stmtGetEquityOptionById.setLong(1, id);
			try (ResultSet results = stmtGetEquityOptionById.executeQuery()) {
				while (results.next()) {
					equityOption = new EquityOption(results.getString("code"),
							OptionTrade.Type.valueOf(results.getString("type")), results.getBigDecimal("strike"),
							results.getDate("maturity_date").toLocalDate(),
							EquityOptionContractSpecificationSQL.getEquityOptionContractSpecificationById(
									results.getLong("equity_option_contract_specification_id")));
					equityOption.setId(results.getLong("id"));
					equityOption.setUnderlying(EquitySQL.getEquityById(results.getLong("equity_id")));
					equityOption.setCreationDate(results.getDate("creation_date").toLocalDate());
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return equityOption;
	}

	public static Set<EquityOption> getEquityOptionsByCode(String code) {
		Set<EquityOption> equityOptions = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquityOptionByCode = con.prepareStatement("SELECT * "
						+ "FROM EQUITY_OPTION, PRODUCT WHERE EQUITY_OPTION.PRODUCT_ID = PRODUCT.ID AND EQUITY_OPTION.CODE = ?")) {
			stmtGetEquityOptionByCode.setString(1, code);
			try (ResultSet results = stmtGetEquityOptionByCode.executeQuery()) {
				while (results.next()) {
					EquityOption equityOption = new EquityOption(results.getString("code"),
							OptionTrade.Type.valueOf(results.getString("type")), results.getBigDecimal("strike"),
							results.getDate("maturity_date").toLocalDate(),
							EquityOptionContractSpecificationSQL.getEquityOptionContractSpecificationById(
									results.getLong("equity_option_contract_specification_id")));
					equityOption.setId(results.getLong("id"));
					equityOption.setUnderlying(EquitySQL.getEquityById(results.getLong("equity_id")));
					equityOption.setCreationDate(results.getDate("creation_date").toLocalDate());
					if (equityOptions == null) {
						equityOptions = new HashSet<EquityOption>();
					}
					equityOptions.add(equityOption);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return equityOptions;
	}

	public static EquityOption getEquityOptionByCodeTypeStrikeMaturityDateAndContractSpecificationName(String code,
			OptionTrade.Type type, BigDecimal strike, LocalDate maturityDate, String contractSpecificationName) {
		EquityOption equityOption = null;
		String sqlQuery = "SELECT * "
				+ "FROM EQUITY_OPTION, PRODUCT, EQUITY_OPTION_CONTRACT_SPECIFICATION WHERE EQUITY_OPTION.PRODUCT_ID = PRODUCT.ID AND EQUITY_OPTION.CODE = ? AND EQUITY_OPTION.TYPE = ? AND EQUITY_OPTION.MATURITY_DATE = ? "
				+ " AND EQUITY_OPTION.EQUITY_OPTION_CONTRACT_SPECIFICATION_ID = EQUITY_OPTION_CONTRACT_SPECIFICATION.ID AND EQUITY_OPTION_CONTRACT_SPECIFICATION.NAME = ?";
		if (strike != null) {
			sqlQuery += " AND EQUITY_OPTION.STRIKE = ?";
		}
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetEquityOptionByCodeTypeStrikeMaturityDateAndContractSpecificationName = con
						.prepareStatement(sqlQuery)) {
			stmtGetEquityOptionByCodeTypeStrikeMaturityDateAndContractSpecificationName.setString(1, code);
			stmtGetEquityOptionByCodeTypeStrikeMaturityDateAndContractSpecificationName.setString(2, type.name());
			stmtGetEquityOptionByCodeTypeStrikeMaturityDateAndContractSpecificationName.setDate(3,
					java.sql.Date.valueOf(maturityDate));
			stmtGetEquityOptionByCodeTypeStrikeMaturityDateAndContractSpecificationName.setString(4,
					contractSpecificationName);
			if (strike != null) {
				stmtGetEquityOptionByCodeTypeStrikeMaturityDateAndContractSpecificationName.setBigDecimal(5, strike);
			}
			try (ResultSet results = stmtGetEquityOptionByCodeTypeStrikeMaturityDateAndContractSpecificationName
					.executeQuery()) {
				while (results.next()) {
					equityOption = new EquityOption(results.getString("code"),
							OptionTrade.Type.valueOf(results.getString("type")), results.getBigDecimal("strike"),
							results.getDate("maturity_date").toLocalDate(),
							EquityOptionContractSpecificationSQL.getEquityOptionContractSpecificationById(
									results.getLong("equity_option_contract_specification_id")));
					equityOption.setId(results.getLong("id"));
					equityOption.setUnderlying(EquitySQL.getEquityById(results.getLong("equity_id")));
					equityOption.setCreationDate(results.getDate("creation_date").toLocalDate());
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return equityOption;
	}

}