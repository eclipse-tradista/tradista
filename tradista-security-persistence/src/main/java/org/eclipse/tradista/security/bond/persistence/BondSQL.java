package org.eclipse.tradista.security.bond.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.CREATION_TIME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.MATURITY_DATE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PRINCIPAL;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PRODUCT_ID;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.currency.persistence.CurrencySQL;
import org.eclipse.tradista.core.exchange.persistence.ExchangeSQL;
import org.eclipse.tradista.core.index.persistence.IndexSQL;
import org.eclipse.tradista.core.product.persistence.ProductSQL;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.security.bond.model.Bond;
import org.eclipse.tradista.security.common.persistence.SecuritySQL;

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

	private static final Field CREATION_TIME_FIELD = new Field(CREATION_TIME);

	private static final Field BOND_PRODUCT_ID_FIELD = new Field(PRODUCT_ID);
	private static final Field COUPON_FIELD = new Field("COUPON");
	private static final Field MATURITY_DATE_FIELD = new Field(MATURITY_DATE);
	private static final Field PRINCIPAL_FIELD = new Field(PRINCIPAL);
	private static final Field DATED_DATE_FIELD = new Field("DATED_DATE");
	private static final Field COUPON_TYPE_FIELD = new Field("COUPON_TYPE");
	private static final Field COUPON_FREQUENCY_FIELD = new Field("COUPON_FREQUENCY");
	private static final Field REDEMPTION_PRICE_FIELD = new Field("REDEMPTION_PRICE");
	private static final Field REDEMPTION_CURRENCY_ID_FIELD = new Field("REDEMPTION_CURRENCY_ID");
	private static final Field REFERENCE_RATE_INDEX_ID_FIELD = new Field("REFERENCE_RATE_INDEX_ID");
	private static final Field CAP_FIELD = new Field("CAP");
	private static final Field FLOOR_FIELD = new Field("FLOOR");
	private static final Field SPREAD_FIELD = new Field("SPREAD");
	private static final Field LEVERAGE_FACTOR_FIELD = new Field("LEVERAGE_FACTOR");

	private static final Field[] BOND_FIELDS = { COUPON_FIELD, PRINCIPAL_FIELD, MATURITY_DATE_FIELD, DATED_DATE_FIELD,
			COUPON_TYPE_FIELD, COUPON_FREQUENCY_FIELD, REDEMPTION_PRICE_FIELD, REDEMPTION_CURRENCY_ID_FIELD,
			REFERENCE_RATE_INDEX_ID_FIELD, CAP_FIELD, FLOOR_FIELD, SPREAD_FIELD, LEVERAGE_FACTOR_FIELD,
			BOND_PRODUCT_ID_FIELD };
	public static final Table BOND_TABLE = new Table("BOND", BOND_FIELDS);

	private static final Field[] BOND_FIELDS_FOR_UPDATE = { COUPON_FIELD, PRINCIPAL_FIELD, MATURITY_DATE_FIELD,
			DATED_DATE_FIELD, COUPON_TYPE_FIELD, COUPON_FREQUENCY_FIELD, REDEMPTION_PRICE_FIELD,
			REDEMPTION_CURRENCY_ID_FIELD, REFERENCE_RATE_INDEX_ID_FIELD, CAP_FIELD, FLOOR_FIELD, SPREAD_FIELD,
			LEVERAGE_FACTOR_FIELD };

	private static final Join BOND_PRODUCT_JOIN = Join.innerEq(ProductSQL.PRODUCT_TABLE, BOND_PRODUCT_ID_FIELD,
			ProductSQL.ID_FIELD);
	private static final Join BOND_SECURITY_JOIN = Join.innerEq(SecuritySQL.SECURITY_TABLE, BOND_PRODUCT_ID_FIELD,
			SecuritySQL.SECURITY_PRODUCT_ID_FIELD);

	private static final String BASE_SELECT_QUERY = TradistaDBUtil.buildSelectQuery(BOND_TABLE, BOND_PRODUCT_JOIN,
			BOND_SECURITY_JOIN);

	public static long saveBond(Bond bond) {
		long productId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveProduct = (bond.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, ProductSQL.PRODUCT_TABLE,
								ProductSQL.PRODUCT_FIELDS_FOR_INSERT)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, ProductSQL.ID_FIELD,
								ProductSQL.PRODUCT_TABLE, ProductSQL.PRODUCT_FIELDS_FOR_UPDATE);
				PreparedStatement stmtSaveSecurity = (bond.getId() == 0) ? SecuritySQL.getInsertStatement(con)
						: SecuritySQL.getUpdateStatement(con);
				PreparedStatement stmtSaveBond = (bond.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, BOND_TABLE, BOND_FIELDS)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, BOND_PRODUCT_ID_FIELD, BOND_TABLE,
								BOND_FIELDS_FOR_UPDATE)) {
			if (bond.getId() == 0) {
				stmtSaveProduct.setTimestamp(1, Timestamp.from(bond.getCreationTime()));
				stmtSaveProduct.setTimestamp(2, Timestamp.from(bond.getLastUpdateTime()));
				stmtSaveProduct.setLong(3, bond.getExchange().getId());
			} else {
				stmtSaveProduct.setTimestamp(1, Timestamp.from(Instant.now()));
				stmtSaveProduct.setLong(2, bond.getExchange().getId());
				stmtSaveProduct.setLong(3, bond.getId());
			}
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
			SecuritySQL.setPreparedStatementSecurityFields(bond, stmtSaveSecurity, productId);
			stmtSaveSecurity.executeUpdate();

			stmtSaveBond.setBigDecimal(1, bond.getCoupon());
			stmtSaveBond.setBigDecimal(2, bond.getPrincipal());
			stmtSaveBond.setDate(3, Date.valueOf(bond.getMaturityDate()));
			stmtSaveBond.setDate(4, Date.valueOf(bond.getDatedDate()));
			stmtSaveBond.setString(5, bond.getCouponType());
			stmtSaveBond.setString(6, bond.getCouponFrequency().name());
			if (bond.getRedemptionPrice() != null) {
				stmtSaveBond.setBigDecimal(7, bond.getRedemptionPrice());
				stmtSaveBond.setLong(8, bond.getRedemptionCurrencyId());
			} else {
				stmtSaveBond.setNull(7, Types.BIGINT);
				stmtSaveBond.setNull(8, Types.BIGINT);
			}
			if (bond.getReferenceRateIndex() != null) {
				stmtSaveBond.setLong(9, bond.getReferenceRateIndex().getId());
			} else {
				stmtSaveBond.setNull(9, Types.BIGINT);
			}
			stmtSaveBond.setBigDecimal(10, bond.getCap());
			stmtSaveBond.setBigDecimal(11, bond.getFloor());
			stmtSaveBond.setBigDecimal(12, bond.getSpread());
			stmtSaveBond.setBigDecimal(13, bond.getLeverageFactor());
			stmtSaveBond.setLong(14, productId);
			stmtSaveBond.executeUpdate();

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		bond.setId(productId);
		return productId;
	}

	public static Set<Bond> getBondsByCreationDate(LocalDate date) {
		Set<Bond> bonds = null;
		StringBuilder sql = new StringBuilder(BASE_SELECT_QUERY);
		TradistaDBUtil.addParameterizedFilter(sql, CREATION_TIME_FIELD, true);
		TradistaDBUtil.addParameterizedFilter(sql, CREATION_TIME_FIELD, false);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetBondsByCreationDate = con.prepareStatement(sql.toString())) {
			stmtGetBondsByCreationDate.setTimestamp(1, Timestamp.valueOf(date.atStartOfDay()));
			stmtGetBondsByCreationDate.setTimestamp(2, Timestamp.valueOf(date.atTime(23, 59, 59, 999999999)));
			try (ResultSet results = stmtGetBondsByCreationDate.executeQuery()) {
				while (results.next()) {
					if (bonds == null) {
						bonds = new HashSet<>();
					}
					bonds.add(buildBond(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bonds;
	}

	public static Set<Bond> getBondsByDates(LocalDate minCreationDate, LocalDate maxCreationDate,
			LocalDate minMaturityDate, LocalDate maxMaturityDate) {
		Set<Bond> bonds = null;
		StringBuilder sql = new StringBuilder(BASE_SELECT_QUERY);
		if (minCreationDate != null) {
			TradistaDBUtil.addFilter(sql, CREATION_TIME_FIELD, minCreationDate.atStartOfDay(), true);
		}
		if (maxCreationDate != null) {
			TradistaDBUtil.addFilter(sql, CREATION_TIME_FIELD, maxCreationDate.atTime(23, 59, 59), false);
		}
		if (minMaturityDate != null) {
			TradistaDBUtil.addFilter(sql, MATURITY_DATE_FIELD, minMaturityDate, true);
		}
		if (maxMaturityDate != null) {
			TradistaDBUtil.addFilter(sql, MATURITY_DATE_FIELD, maxMaturityDate, false);
		}

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(sql.toString());
				ResultSet results = stmt.executeQuery()) {
			while (results.next()) {
				if (bonds == null) {
					bonds = new HashSet<>();
				}
				bonds.add(buildBond(results));
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bonds;
	}

	public static Set<Bond> getAllBonds() {
		Set<Bond> bonds = null;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllBonds = con.prepareStatement(BASE_SELECT_QUERY);
				ResultSet results = stmtGetAllBonds.executeQuery()) {
			while (results.next()) {
				if (bonds == null) {
					bonds = new HashSet<>();
				}
				bonds.add(buildBond(results));
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bonds;
	}

	public static Bond getBondById(long id) {
		Bond bond = null;
		StringBuilder sql = new StringBuilder(BASE_SELECT_QUERY);
		TradistaDBUtil.addParameterizedFilter(sql, ProductSQL.ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetBondById = con.prepareStatement(sql.toString())) {
			stmtGetBondById.setLong(1, id);
			try (ResultSet results = stmtGetBondById.executeQuery()) {
				while (results.next()) {
					bond = buildBond(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bond;
	}

	public static Set<Bond> getBondsByMaturityDate(LocalDate minDate, LocalDate maxDate) {
		Set<Bond> bonds = null;
		StringBuilder sql = new StringBuilder(BASE_SELECT_QUERY);
		if (minDate != null) {
			TradistaDBUtil.addFilter(sql, MATURITY_DATE_FIELD, minDate, true);
		}
		if (maxDate != null) {
			TradistaDBUtil.addFilter(sql, MATURITY_DATE_FIELD, maxDate, false);
		}

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(sql.toString());
				ResultSet results = stmt.executeQuery()) {
			while (results.next()) {
				if (bonds == null) {
					bonds = new HashSet<>();
				}
				bonds.add(buildBond(results));
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bonds;
	}

	public static Set<Bond> getBondsByIsin(String isin) {
		Set<Bond> bonds = null;
		StringBuilder sql = new StringBuilder(BASE_SELECT_QUERY);
		TradistaDBUtil.addParameterizedFilter(sql, SecuritySQL.ISIN_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetBondByIsin = con.prepareStatement(sql.toString())) {
			stmtGetBondByIsin.setString(1, isin);
			try (ResultSet results = stmtGetBondByIsin.executeQuery()) {
				while (results.next()) {
					if (bonds == null) {
						bonds = new HashSet<>();
					}
					bonds.add(buildBond(results));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bonds;
	}

	public static Bond getBondByIsinAndExchangeCode(String isin, String exchangeCode) {
		Bond bond = null;
		StringBuilder sql = new StringBuilder(
				TradistaDBUtil.buildSelectQuery(BOND_TABLE, BOND_PRODUCT_JOIN, BOND_SECURITY_JOIN,
						Join.innerEq(ExchangeSQL.EXCHANGE_TABLE, ProductSQL.EXCHANGE_ID_FIELD, ExchangeSQL.ID_FIELD)));
		TradistaDBUtil.addParameterizedFilter(sql, SecuritySQL.ISIN_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, ExchangeSQL.CODE_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetBondByIsinAndExchangeCode = con.prepareStatement(sql.toString())) {
			stmtGetBondByIsinAndExchangeCode.setString(1, isin);
			stmtGetBondByIsinAndExchangeCode.setString(2, exchangeCode);
			try (ResultSet results = stmtGetBondByIsinAndExchangeCode.executeQuery()) {
				while (results.next()) {
					bond = buildBond(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return bond;
	}

	private static Bond buildBond(ResultSet results) throws SQLException {
		Bond bond = new Bond(ExchangeSQL.getExchangeById(results.getLong(ProductSQL.EXCHANGE_ID_FIELD.getName())),
				results.getString(SecuritySQL.ISIN_FIELD.getName()));
		bond.setId(results.getLong(ProductSQL.ID_FIELD.getName()));
		bond.setCoupon(results.getBigDecimal(COUPON_FIELD.getName()));
		bond.setMaturityDate(results.getDate(MATURITY_DATE_FIELD.getName()).toLocalDate());
		bond.setPrincipal(results.getBigDecimal(PRINCIPAL_FIELD.getName()));
		Timestamp creationTimestamp = results.getTimestamp(CREATION_TIME_FIELD.getName());
		if (creationTimestamp != null) {
			bond.setCreationDate(creationTimestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
		}
		bond.setDatedDate(results.getDate(DATED_DATE_FIELD.getName()).toLocalDate());
		bond.setCouponType(results.getString(COUPON_TYPE_FIELD.getName()));
		bond.setCouponFrequency(Tenor.valueOf(results.getString(COUPON_FREQUENCY_FIELD.getName())));
		bond.setRedemptionPrice(results.getBigDecimal(REDEMPTION_PRICE_FIELD.getName()));
		bond.setRedemptionCurrency(
				CurrencySQL.getCurrencyById(results.getLong(REDEMPTION_CURRENCY_ID_FIELD.getName())));
		SecuritySQL.setSecurityCommonFields(bond, results);
		long referenceRateIndexId = results.getLong(REFERENCE_RATE_INDEX_ID_FIELD.getName());
		if (referenceRateIndexId > 0) {
			bond.setReferenceRateIndex(IndexSQL.getIndexById(referenceRateIndexId));
			bond.setSpread(results.getBigDecimal(SPREAD_FIELD.getName()));
			bond.setLeverageFactor(results.getBigDecimal(LEVERAGE_FACTOR_FIELD.getName()));
			bond.setCap(results.getBigDecimal(CAP_FIELD.getName()));
			bond.setFloor(results.getBigDecimal(FLOOR_FIELD.getName()));
		}
		return bond;
	}

}