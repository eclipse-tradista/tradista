package org.eclipse.tradista.security.bond.persistence;

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
import org.eclipse.tradista.core.currency.persistence.CurrencySQL;
import org.eclipse.tradista.core.exchange.persistence.ExchangeSQL;
import org.eclipse.tradista.core.index.persistence.IndexSQL;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.security.bond.model.Bond;

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

public class BondSQL {

	public static long saveBond(Bond bond) {
		long productId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveProduct = (bond.getId() == 0)
						? con.prepareStatement("INSERT INTO PRODUCT(CREATION_DATE, EXCHANGE_ID) VALUES (?, ?) ",
								Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement("UPDATE PRODUCT SET CREATION_DATE=?, EXCHANGE_ID=? WHERE ID=? ");
				PreparedStatement stmtSaveSecurity = (bond.getId() == 0) ? con.prepareStatement(
						"INSERT INTO SECURITY(ISSUER_ID, ISIN, CURRENCY_ID, ISSUE_DATE, ISSUE_PRICE, PRODUCT_ID) VALUES (?, ?, ?, ?, ?, ?) ")
						: con.prepareStatement(
								"UPDATE SECURITY SET ISSUER_ID=?, ISIN=?, CURRENCY_ID=?, ISSUE_DATE=?, ISSUE_PRICE=? WHERE PRODUCT_ID=?");
				PreparedStatement stmtSaveBond = (bond.getId() == 0) ? con.prepareStatement(
						"INSERT INTO BOND(COUPON, PRINCIPAL, MATURITY_DATE, DATED_DATE, COUPON_TYPE, COUPON_FREQUENCY, REDEMPTION_PRICE, REDEMPTION_CURRENCY_ID, REFERENCE_RATE_INDEX_ID, CAP, FLOOR, SPREAD, LEVERAGE_FACTOR, PRODUCT_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ")
						: con.prepareStatement(
								"UPDATE BOND SET COUPON=?, PRINCIPAL=?, MATURITY_DATE=?, DATED_DATE=?, COUPON_TYPE=?, COUPON_FREQUENCY=?, REDEMPTION_PRICE=?, REDEMPTION_CURRENCY_ID=?, REFERENCE_RATE_INDEX_ID=?, CAP=?, FLOOR=?, SPREAD=?, LEVERAGE_FACTOR=? WHERE PRODUCT_ID=?")) {
			if (bond.getId() != 0) {
				stmtSaveProduct.setLong(3, bond.getId());
			}
			stmtSaveProduct.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
			stmtSaveProduct.setLong(2, bond.getExchange().getId());
			stmtSaveProduct.executeUpdate();

			if (bond.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveProduct.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						productId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating product failed, no generated key obtained.");
					}
				}
			} else {
				productId = bond.getId();
			}
			stmtSaveSecurity.setLong(1, bond.getIssuerId());
			stmtSaveSecurity.setString(2, bond.getIsin());
			stmtSaveSecurity.setLong(3, bond.getCurrencyId());
			stmtSaveSecurity.setDate(4, java.sql.Date.valueOf(bond.getIssueDate()));
			stmtSaveSecurity.setBigDecimal(5, bond.getIssuePrice());
			stmtSaveSecurity.setLong(6, productId);
			stmtSaveSecurity.executeUpdate();

			stmtSaveBond.setBigDecimal(1, bond.getCoupon());
			stmtSaveBond.setBigDecimal(2, bond.getPrincipal());
			stmtSaveBond.setDate(3, java.sql.Date.valueOf(bond.getMaturityDate()));
			stmtSaveBond.setDate(4, java.sql.Date.valueOf(bond.getDatedDate()));
			stmtSaveBond.setString(5, bond.getCouponType());
			stmtSaveBond.setString(6, bond.getCouponFrequency().name());
			if (bond.getRedemptionPrice() != null) {
				stmtSaveBond.setBigDecimal(7, bond.getRedemptionPrice());
				stmtSaveBond.setLong(8, bond.getRedemptionCurrencyId());
			} else {
				stmtSaveBond.setNull(7, java.sql.Types.BIGINT);
				stmtSaveBond.setNull(8, java.sql.Types.BIGINT);
			}
			if (bond.getReferenceRateIndex() != null) {
				stmtSaveBond.setLong(9, bond.getReferenceRateIndex().getId());
			} else {
				stmtSaveBond.setNull(9, java.sql.Types.BIGINT);
			}
			stmtSaveBond.setBigDecimal(10, bond.getCap());
			stmtSaveBond.setBigDecimal(11, bond.getFloor());
			stmtSaveBond.setBigDecimal(12, bond.getSpread());
			stmtSaveBond.setBigDecimal(13, bond.getLeverageFactor());
			stmtSaveBond.setLong(14, productId);
			stmtSaveBond.executeUpdate();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		bond.setId(productId);
		return productId;
	}

	public static Set<Bond> getBondsByCreationDate(LocalDate date) {
		Set<Bond> bonds = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetBondsByCreationDate = con
						.prepareStatement("SELECT BOND.PRODUCT_ID ID, SECURITY.ISIN, BOND.COUPON COUPON,"
								+ "BOND.MATURITY_DATE MATURITY_DATE, BOND.PRINCIPAL PRINCIPAL,"
								+ "BOND.DATED_DATE DATED_DATE, BOND.COUPON_TYPE COUPON_TYPE, BOND.COUPON_FREQUENCY COUPON_FREQUENCY, BOND.REDEMPTION_PRICE REDEMPTION_PRICE, "
								+ "BOND.REDEMPTION_CURRENCY_ID, PRODUCT.CREATION_DATE CREATION_DATE, SECURITY.CURRENCY_ID CURRENCY_ID, "
								+ "SECURITY.ISSUER_ID ISSUER_ID, SECURITY.ISSUE_DATE ISSUE_DATE, SECURITY.ISSUE_PRICE, PRODUCT.EXCHANGE_ID, REFERENCE_RATE_INDEX_ID, CAP, FLOOR, SPREAD, LEVERAGE_FACTOR "
								+ "FROM BOND, PRODUCT, SECURITY WHERE "
								+ "SECURITY.PRODUCT_ID = PRODUCT.ID AND BOND.PRODUCT_ID = PRODUCT.ID AND CREATION_DATE = ? ")) {
			stmtGetBondsByCreationDate.setDate(1, java.sql.Date.valueOf(date));
			try (ResultSet results = stmtGetBondsByCreationDate.executeQuery()) {
				while (results.next()) {
					Bond bond = new Bond(ExchangeSQL.getExchangeById(results.getLong("exchange_id")),
							results.getString("isin"));
					bond.setId(results.getInt("id"));
					bond.setCoupon(results.getBigDecimal("coupon"));
					bond.setMaturityDate(results.getDate("maturity_date").toLocalDate());
					bond.setPrincipal(results.getBigDecimal("principal"));
					bond.setCreationDate(results.getDate("creation_date").toLocalDate());
					bond.setDatedDate(results.getDate("dated_date").toLocalDate());
					bond.setCouponType(results.getString("coupon_type"));
					bond.setCouponFrequency(Tenor.valueOf(results.getString("coupon_frequency")));
					bond.setRedemptionPrice(results.getBigDecimal("redemption_price"));
					bond.setRedemptionCurrency(CurrencySQL.getCurrencyById(results.getLong("redemption_currency_id")));
					bond.setIssuer(LegalEntitySQL.getLegalEntityById(results.getLong("issuer_id")));
					bond.setIssueDate(results.getDate("issue_date").toLocalDate());
					bond.setIssuePrice(results.getBigDecimal("issue_price"));
					bond.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					long referenceRateIndexId = results.getLong("reference_rate_index_id");
					if (referenceRateIndexId > 0) {
						bond.setReferenceRateIndex(IndexSQL.getIndexById(referenceRateIndexId));
						bond.setSpread(results.getBigDecimal("spread"));
						bond.setLeverageFactor(results.getBigDecimal("leverage_factor"));
						bond.setCap(results.getBigDecimal("cap"));
						bond.setFloor(results.getBigDecimal("floor"));
					}
					if (bonds == null) {
						bonds = new HashSet<Bond>();
					}
					bonds.add(bond);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return bonds;
	}

	public static Set<Bond> getBondsByDates(LocalDate minCreationDate, LocalDate maxCreationDate,
			LocalDate minMaturityDate, LocalDate maxMaturityDate) {
		Set<Bond> bonds = null;

		try (Connection con = TradistaDB.getConnection(); Statement stmt = con.createStatement()) {
			String query = "SELECT BOND.PRODUCT_ID ID, SECURITY.ISIN, BOND.COUPON COUPON,"
					+ "BOND.MATURITY_DATE MATURITY_DATE, BOND.PRINCIPAL PRINCIPAL,"
					+ "BOND.DATED_DATE DATED_DATE, BOND.COUPON_TYPE COUPON_TYPE, BOND.COUPON_FREQUENCY COUPON_FREQUENCY, BOND.REDEMPTION_PRICE REDEMPTION_PRICE, "
					+ "BOND.REDEMPTION_CURRENCY_ID, PRODUCT.CREATION_DATE CREATION_DATE, SECURITY.CURRENCY_ID CURRENCY_ID, "
					+ "SECURITY.ISSUER_ID ISSUER_ID, SECURITY.ISSUE_DATE ISSUE_DATE, SECURITY.ISSUE_PRICE, PRODUCT.EXCHANGE_ID, REFERENCE_RATE_INDEX_ID, CAP, FLOOR, SPREAD, LEVERAGE_FACTOR "
					+ "FROM BOND, PRODUCT, SECURITY WHERE BOND.PRODUCT_ID = SECURITY.PRODUCT_ID"
					+ " AND SECURITY.PRODUCT_ID = PRODUCT.ID";
			if (minCreationDate != null || maxCreationDate != null || minMaturityDate != null
					|| maxMaturityDate != null) {
				if (minCreationDate != null) {
					query += " AND CREATION_DATE >= '"
							+ DateTimeFormatter.ofPattern("MM/dd/yyyy").format(minCreationDate) + "'";
				}
				if (maxCreationDate != null) {
					query += " AND CREATION_DATE <= '"
							+ DateTimeFormatter.ofPattern("MM/dd/yyyy").format(maxCreationDate) + "'";
				}
				if (minMaturityDate != null) {
					query += " AND MATURITY_DATE >= '"
							+ DateTimeFormatter.ofPattern("MM/dd/yyyy").format(minMaturityDate) + "'";
				}
				if (maxMaturityDate != null) {
					query += " AND MATURITY_DATE <= '"
							+ DateTimeFormatter.ofPattern("MM/dd/yyyy").format(maxMaturityDate) + "'";
				}
			}
			try (ResultSet results = stmt.executeQuery(query)) {
				while (results.next()) {
					Bond bond = new Bond(ExchangeSQL.getExchangeById(results.getLong("exchange_id")),
							results.getString("isin"));
					bond.setId(results.getInt("id"));
					bond.setCoupon(results.getBigDecimal("coupon"));
					bond.setMaturityDate(results.getDate("maturity_date").toLocalDate());
					bond.setPrincipal(results.getBigDecimal("principal"));
					bond.setCreationDate(results.getDate("creation_date").toLocalDate());
					bond.setDatedDate(results.getDate("dated_date").toLocalDate());
					bond.setCouponType(results.getString("coupon_type"));
					bond.setCouponFrequency(Tenor.valueOf(results.getString("coupon_frequency")));
					bond.setRedemptionPrice(results.getBigDecimal("redemption_price"));
					bond.setRedemptionCurrency(CurrencySQL.getCurrencyById(results.getLong("redemption_currency_id")));
					bond.setIssuer(LegalEntitySQL.getLegalEntityById(results.getLong("issuer_id")));
					bond.setIssueDate(results.getDate("issue_date").toLocalDate());
					bond.setIssuePrice(results.getBigDecimal("issue_price"));
					bond.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					long referenceRateIndexId = results.getLong("reference_rate_index_id");
					if (referenceRateIndexId > 0) {
						bond.setReferenceRateIndex(IndexSQL.getIndexById(referenceRateIndexId));
						bond.setSpread(results.getBigDecimal("spread"));
						bond.setLeverageFactor(results.getBigDecimal("leverage_factor"));
						bond.setCap(results.getBigDecimal("cap"));
						bond.setFloor(results.getBigDecimal("floor"));
					}
					if (bonds == null) {
						bonds = new HashSet<Bond>();
					}
					bonds.add(bond);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return bonds;
	}

	public static Set<Bond> getAllBonds() {
		Set<Bond> bonds = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllBonds = con
						.prepareStatement("SELECT BOND.PRODUCT_ID ID, BOND.COUPON COUPON, SECURITY.ISIN, "
								+ "BOND.MATURITY_DATE MATURITY_DATE, BOND.PRINCIPAL PRINCIPAL,"
								+ "BOND.DATED_DATE DATED_DATE, BOND.COUPON_TYPE COUPON_TYPE, BOND.COUPON_FREQUENCY COUPON_FREQUENCY, BOND.REDEMPTION_PRICE REDEMPTION_PRICE, "
								+ "BOND.REDEMPTION_CURRENCY_ID, PRODUCT.CREATION_DATE CREATION_DATE, SECURITY.CURRENCY_ID CURRENCY_ID, "
								+ "SECURITY.ISSUER_ID ISSUER_ID, SECURITY.ISSUE_DATE ISSUE_DATE, SECURITY.ISSUE_PRICE, PRODUCT.EXCHANGE_ID, REFERENCE_RATE_INDEX_ID, CAP, FLOOR, SPREAD, LEVERAGE_FACTOR "
								+ "FROM BOND, PRODUCT, SECURITY WHERE BOND.PRODUCT_ID = SECURITY.PRODUCT_ID"
								+ " AND SECURITY.PRODUCT_ID = PRODUCT.ID");
				ResultSet results = stmtGetAllBonds.executeQuery()) {
			while (results.next()) {
				if (bonds == null) {
					bonds = new HashSet<Bond>();
				}
				Bond bond = new Bond(ExchangeSQL.getExchangeById(results.getLong("exchange_id")),
						results.getString("isin"));
				bond.setId(results.getLong("id"));
				bond.setCoupon(results.getBigDecimal("coupon"));
				bond.setMaturityDate(results.getDate("maturity_date").toLocalDate());
				bond.setPrincipal(results.getBigDecimal("principal"));
				bond.setCreationDate(results.getDate("creation_date").toLocalDate());
				bond.setDatedDate(results.getDate("dated_date").toLocalDate());
				bond.setCouponType(results.getString("coupon_type"));
				bond.setCouponFrequency(Tenor.valueOf(results.getString("coupon_frequency")));
				bond.setRedemptionPrice(results.getBigDecimal("redemption_price"));
				bond.setRedemptionCurrency(CurrencySQL.getCurrencyById(results.getLong("redemption_currency_id")));
				bond.setIssuer(LegalEntitySQL.getLegalEntityById(results.getLong("issuer_id")));
				bond.setIssueDate(results.getDate("issue_date").toLocalDate());
				bond.setIssuePrice(results.getBigDecimal("issue_price"));
				bond.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
				long referenceRateIndexId = results.getLong("reference_rate_index_id");
				if (referenceRateIndexId > 0) {
					bond.setReferenceRateIndex(IndexSQL.getIndexById(referenceRateIndexId));
					bond.setSpread(results.getBigDecimal("spread"));
					bond.setLeverageFactor(results.getBigDecimal("leverage_factor"));
					bond.setCap(results.getBigDecimal("cap"));
					bond.setFloor(results.getBigDecimal("floor"));
				}
				bonds.add(bond);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return bonds;
	}

	public static Bond getBondById(long id) {
		Bond bond = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetBondById = con
						.prepareStatement("SELECT BOND.PRODUCT_ID ID, BOND.COUPON COUPON, SECURITY.ISIN ISIN, "
								+ "BOND.MATURITY_DATE MATURITY_DATE, BOND.PRINCIPAL PRINCIPAL,"
								+ "BOND.DATED_DATE DATED_DATE, BOND.COUPON_TYPE COUPON_TYPE, BOND.COUPON_FREQUENCY COUPON_FREQUENCY, BOND.REDEMPTION_PRICE REDEMPTION_PRICE, "
								+ "BOND.REDEMPTION_CURRENCY_ID, PRODUCT.CREATION_DATE CREATION_DATE, SECURITY.CURRENCY_ID CURRENCY_ID, "
								+ "SECURITY.ISSUER_ID ISSUER_ID, SECURITY.ISSUE_DATE ISSUE_DATE, SECURITY.ISSUE_PRICE, PRODUCT.EXCHANGE_ID, REFERENCE_RATE_INDEX_ID, CAP, FLOOR, SPREAD, LEVERAGE_FACTOR "
								+ "FROM BOND, PRODUCT, SECURITY WHERE BOND.PRODUCT_ID = SECURITY.PRODUCT_ID"
								+ " AND SECURITY.PRODUCT_ID = PRODUCT.ID AND PRODUCT.ID = ?")) {
			stmtGetBondById.setLong(1, id);
			try (ResultSet results = stmtGetBondById.executeQuery()) {
				while (results.next()) {
					bond = new Bond(ExchangeSQL.getExchangeById(results.getLong("exchange_id")),
							results.getString("isin"));
					bond.setId(results.getLong("id"));
					bond.setCoupon(results.getBigDecimal("coupon"));
					bond.setMaturityDate(results.getDate("maturity_date").toLocalDate());
					bond.setPrincipal(results.getBigDecimal("principal"));
					bond.setCreationDate(results.getDate("creation_date").toLocalDate());
					bond.setDatedDate(results.getDate("dated_date").toLocalDate());
					bond.setCouponType(results.getString("coupon_type"));
					bond.setCouponFrequency(Tenor.valueOf(results.getString("coupon_frequency")));
					bond.setRedemptionPrice(results.getBigDecimal("redemption_price"));
					bond.setRedemptionCurrency(CurrencySQL.getCurrencyById(results.getLong("redemption_currency_id")));
					bond.setIssuer(LegalEntitySQL.getLegalEntityById(results.getLong("issuer_id")));
					bond.setIssueDate(results.getDate("issue_date").toLocalDate());
					bond.setIssuePrice(results.getBigDecimal("issue_price"));
					bond.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					long referenceRateIndexId = results.getLong("reference_rate_index_id");
					if (referenceRateIndexId > 0) {
						bond.setReferenceRateIndex(IndexSQL.getIndexById(referenceRateIndexId));
						bond.setSpread(results.getBigDecimal("spread"));
						bond.setLeverageFactor(results.getBigDecimal("leverage_factor"));
						bond.setCap(results.getBigDecimal("cap"));
						bond.setFloor(results.getBigDecimal("floor"));
					}
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return bond;
	}

	public static Set<Bond> getBondsByMaturityDate(LocalDate minDate, LocalDate maxDate) {
		Set<Bond> bonds = null;

		try (Connection con = TradistaDB.getConnection(); Statement stmt = con.createStatement()) {
			String dateQuery = null;
			String query = "SELECT BOND.PRODUCT_ID ID, SECURITY.ISIN, BOND.COUPON COUPON,"
					+ "BOND.MATURITY_DATE MATURITY_DATE, BOND.PRINCIPAL PRINCIPAL,"
					+ "BOND.DATED_DATE DATED_DATE, BOND.COUPON_TYPE COUPON_TYPE, BOND.COUPON_FREQUENCY COUPON_FREQUENCY, BOND.REDEMPTION_PRICE REDEMPTION_PRICE, "
					+ "BOND.REDEMPTION_CURRENCY_ID, PRODUCT.CREATION_DATE CREATION_DATE, SECURITY.CURRENCY_ID CURRENCY_ID, "
					+ "SECURITY.ISSUER_ID ISSUER_ID, SECURITY.ISSUE_DATE ISSUE_DATE, SECURITY.ISSUE_PRICE, PRODUCT.EXCHANGE_ID, REFERENCE_RATE_INDEX_ID, CAP, FLOOR, SPREAD, LEVERAGE_FACTOR "
					+ "FROM BOND, PRODUCT, SECURITY WHERE BOND.PRODUCT_ID = SECURITY.PRODUCT_ID"
					+ " AND SECURITY.PRODUCT_ID = PRODUCT.ID";
			if (minDate == null) {
				dateQuery = " AND MATURITY_DATE <= '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(maxDate) + "'";
			} else if (maxDate == null) {
				dateQuery = " AND MATURITY_DATE >= '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(minDate) + "'";
			} else {
				dateQuery = " AND MATURITY_DATE BETWEEN '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(minDate)
						+ "' AND '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(maxDate) + "'";
			}
			query += dateQuery;

			try (ResultSet results = stmt.executeQuery(query)) {
				while (results.next()) {
					Bond bond = new Bond(ExchangeSQL.getExchangeById(results.getLong("exchange_id")),
							results.getString("isin"));
					bond.setId(results.getInt("id"));
					bond.setCoupon(results.getBigDecimal("coupon"));
					bond.setMaturityDate(results.getDate("maturity_date").toLocalDate());
					bond.setPrincipal(results.getBigDecimal("principal"));
					bond.setCreationDate(results.getDate("creation_date").toLocalDate());
					bond.setDatedDate(results.getDate("dated_date").toLocalDate());
					bond.setCouponType(results.getString("coupon_type"));
					bond.setCouponFrequency(Tenor.valueOf(results.getString("coupon_frequency")));
					bond.setRedemptionPrice(results.getBigDecimal("redemption_price"));
					bond.setRedemptionCurrency(CurrencySQL.getCurrencyById(results.getLong("redemption_currency_id")));
					bond.setIssuer(LegalEntitySQL.getLegalEntityById(results.getLong("issuer_id")));
					bond.setIssueDate(results.getDate("issue_date").toLocalDate());
					bond.setIssuePrice(results.getBigDecimal("issue_price"));
					bond.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					long referenceRateIndexId = results.getLong("reference_rate_index_id");
					if (referenceRateIndexId > 0) {
						bond.setReferenceRateIndex(IndexSQL.getIndexById(referenceRateIndexId));
						bond.setSpread(results.getBigDecimal("spread"));
						bond.setLeverageFactor(results.getBigDecimal("leverage_factor"));
						bond.setCap(results.getBigDecimal("cap"));
						bond.setFloor(results.getBigDecimal("floor"));
					}
					if (bonds == null) {
						bonds = new HashSet<Bond>();
					}
					bonds.add(bond);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return bonds;
	}

	public static Set<Bond> getBondsByIsin(String isin) {
		Set<Bond> bonds = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetBondByIsin = con
						.prepareStatement("SELECT BOND.PRODUCT_ID ID, BOND.COUPON COUPON, SECURITY.ISIN, "
								+ "BOND.MATURITY_DATE MATURITY_DATE, BOND.PRINCIPAL PRINCIPAL,"
								+ "BOND.DATED_DATE DATED_DATE, BOND.COUPON_TYPE COUPON_TYPE, BOND.COUPON_FREQUENCY COUPON_FREQUENCY, BOND.REDEMPTION_PRICE REDEMPTION_PRICE, "
								+ "BOND.REDEMPTION_CURRENCY_ID, PRODUCT.CREATION_DATE CREATION_DATE, SECURITY.CURRENCY_ID CURRENCY_ID, "
								+ "SECURITY.ISSUER_ID ISSUER_ID, SECURITY.ISSUE_DATE ISSUE_DATE, SECURITY.ISSUE_PRICE, PRODUCT.EXCHANGE_ID, REFERENCE_RATE_INDEX_ID, CAP, FLOOR, SPREAD, LEVERAGE_FACTOR "
								+ "FROM BOND, PRODUCT, SECURITY WHERE BOND.PRODUCT_ID = SECURITY.PRODUCT_ID"
								+ " AND SECURITY.PRODUCT_ID = PRODUCT.ID AND SECURITY.ISIN = ?")) {
			stmtGetBondByIsin.setString(1, isin);
			try (ResultSet results = stmtGetBondByIsin.executeQuery()) {
				while (results.next()) {
					Bond bond = new Bond(ExchangeSQL.getExchangeById(results.getLong("exchange_id")),
							results.getString("isin"));
					bond.setId(results.getLong("id"));
					bond.setCoupon(results.getBigDecimal("coupon"));
					bond.setMaturityDate(results.getDate("maturity_date").toLocalDate());
					bond.setPrincipal(results.getBigDecimal("principal"));
					bond.setCreationDate(results.getDate("creation_date").toLocalDate());
					bond.setDatedDate(results.getDate("dated_date").toLocalDate());
					bond.setCouponType(results.getString("coupon_type"));
					bond.setCouponFrequency(Tenor.valueOf(results.getString("coupon_frequency")));
					bond.setRedemptionPrice(results.getBigDecimal("redemption_price"));
					bond.setRedemptionCurrency(CurrencySQL.getCurrencyById(results.getLong("redemption_currency_id")));
					bond.setIssuer(LegalEntitySQL.getLegalEntityById(results.getLong("issuer_id")));
					bond.setIssueDate(results.getDate("issue_date").toLocalDate());
					bond.setIssuePrice(results.getBigDecimal("issue_price"));
					bond.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					long referenceRateIndexId = results.getLong("reference_rate_index_id");
					if (referenceRateIndexId > 0) {
						bond.setReferenceRateIndex(IndexSQL.getIndexById(referenceRateIndexId));
						bond.setSpread(results.getBigDecimal("spread"));
						bond.setLeverageFactor(results.getBigDecimal("leverage_factor"));
						bond.setCap(results.getBigDecimal("cap"));
						bond.setFloor(results.getBigDecimal("floor"));
					}
					if (bonds == null) {
						bonds = new HashSet<Bond>();
					}
					bonds.add(bond);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return bonds;
	}

	public static Bond getBondByIsinAndExchangeCode(String isin, String exchangeCode) {
		Bond bond = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetBondByIsinAndExchangeCode = con
						.prepareStatement("SELECT BOND.PRODUCT_ID ID, BOND.COUPON COUPON, SECURITY.ISIN, "
								+ "BOND.MATURITY_DATE MATURITY_DATE, BOND.PRINCIPAL PRINCIPAL,"
								+ "BOND.DATED_DATE DATED_DATE, BOND.COUPON_TYPE COUPON_TYPE, BOND.COUPON_FREQUENCY COUPON_FREQUENCY, BOND.REDEMPTION_PRICE REDEMPTION_PRICE, "
								+ "BOND.REDEMPTION_CURRENCY_ID, PRODUCT.CREATION_DATE CREATION_DATE, SECURITY.CURRENCY_ID CURRENCY_ID, "
								+ "SECURITY.ISSUER_ID ISSUER_ID, SECURITY.ISSUE_DATE ISSUE_DATE, SECURITY.ISSUE_PRICE, PRODUCT.EXCHANGE_ID, REFERENCE_RATE_INDEX_ID, CAP, FLOOR, SPREAD, LEVERAGE_FACTOR "
								+ "FROM BOND, PRODUCT, SECURITY, EXCHANGE WHERE BOND.PRODUCT_ID = SECURITY.PRODUCT_ID"
								+ " AND SECURITY.PRODUCT_ID = PRODUCT.ID AND SECURITY.ISIN = ?"
								+ " AND PRODUCT.EXCHANGE_ID = EXCHANGE.ID AND EXCHANGE.CODE = ?")) {
			stmtGetBondByIsinAndExchangeCode.setString(1, isin);
			stmtGetBondByIsinAndExchangeCode.setString(2, exchangeCode);
			try (ResultSet results = stmtGetBondByIsinAndExchangeCode.executeQuery()) {
				while (results.next()) {
					bond = new Bond(ExchangeSQL.getExchangeById(results.getLong("exchange_id")),
							results.getString("isin"));
					bond.setId(results.getInt("id"));
					bond.setCoupon(results.getBigDecimal("coupon"));
					bond.setMaturityDate(results.getDate("maturity_date").toLocalDate());
					bond.setPrincipal(results.getBigDecimal("principal"));
					bond.setCreationDate(results.getDate("creation_date").toLocalDate());
					bond.setDatedDate(results.getDate("dated_date").toLocalDate());
					bond.setCouponType(results.getString("coupon_type"));
					bond.setCouponFrequency(Tenor.valueOf(results.getString("coupon_frequency")));
					bond.setRedemptionPrice(results.getBigDecimal("redemption_price"));
					bond.setRedemptionCurrency(CurrencySQL.getCurrencyById(results.getLong("redemption_currency_id")));
					bond.setIssuer(LegalEntitySQL.getLegalEntityById(results.getLong("issuer_id")));
					bond.setIssueDate(results.getDate("issue_date").toLocalDate());
					bond.setIssuePrice(results.getBigDecimal("issue_price"));
					bond.setCurrency(CurrencySQL.getCurrencyById(results.getLong("currency_id")));
					long referenceRateIndexId = results.getLong("reference_rate_index_id");
					if (referenceRateIndexId > 0) {
						bond.setReferenceRateIndex(IndexSQL.getIndexById(referenceRateIndexId));
						bond.setSpread(results.getBigDecimal("spread"));
						bond.setLeverageFactor(results.getBigDecimal("leverage_factor"));
						bond.setCap(results.getBigDecimal("cap"));
						bond.setFloor(results.getBigDecimal("floor"));
					}
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return bond;
	}

}