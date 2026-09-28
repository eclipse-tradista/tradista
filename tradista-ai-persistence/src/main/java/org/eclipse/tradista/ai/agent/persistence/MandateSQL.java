package org.eclipse.tradista.ai.agent.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.BOOK_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.CREATION_TIME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.CURRENCY_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.END_DATE;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.LAST_UPDATE_TIME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.NAME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PRODUCT_TYPE;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tradista.ai.agent.model.Mandate;
import org.eclipse.tradista.ai.agent.model.Mandate.Allocation;
import org.eclipse.tradista.ai.agent.model.Mandate.RiskLevel;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Join;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.currency.service.CurrencyBusinessDelegate;

/********************************************************************************
 * Copyright (c) 2017 Olivier Asuncion
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

public class MandateSQL {

	private static final Field ID_FIELD = new Field(ID);
	private static final Field NAME_FIELD = new Field(NAME);
	private static final Field ACCEPTED_RISK_LEVEL_FIELD = new Field("ACCEPTED_RISK_LEVEL");
	private static final Field CREATION_TIME_FIELD = new Field(CREATION_TIME);
	private static final Field LAST_UPDATE_TIME_FIELD = new Field(LAST_UPDATE_TIME);
	private static final Field START_DATE_FIELD = new Field("START_DATE");
	private static final Field END_DATE_FIELD = new Field(END_DATE);
	private static final Field INITIAL_CASH_AMOUNT_FIELD = new Field("INITIAL_CASH_AMOUNT");
	private static final Field INITIAL_CASH_CURRENCY_FIELD = new Field("INITIAL_CASH_CURRENCY");
	private static final Field BOOK_ID_FIELD = new Field(BOOK_ID);

	private static final Field[] MANDATE_FIELDS = { ID_FIELD, NAME_FIELD, ACCEPTED_RISK_LEVEL_FIELD,
			CREATION_TIME_FIELD, LAST_UPDATE_TIME_FIELD, START_DATE_FIELD, END_DATE_FIELD, INITIAL_CASH_AMOUNT_FIELD,
			INITIAL_CASH_CURRENCY_FIELD, BOOK_ID_FIELD };

	public static final Table MANDATE_TABLE = new Table("MANDATE", MANDATE_FIELDS);

	private static final Field[] FIELDS_FOR_INSERT = { NAME_FIELD, ACCEPTED_RISK_LEVEL_FIELD, CREATION_TIME_FIELD,
			LAST_UPDATE_TIME_FIELD, START_DATE_FIELD, END_DATE_FIELD, INITIAL_CASH_AMOUNT_FIELD,
			INITIAL_CASH_CURRENCY_FIELD, BOOK_ID_FIELD };

	private static final Field[] FIELDS_FOR_UPDATE = { NAME_FIELD, ACCEPTED_RISK_LEVEL_FIELD, LAST_UPDATE_TIME_FIELD,
			START_DATE_FIELD, END_DATE_FIELD, INITIAL_CASH_AMOUNT_FIELD, INITIAL_CASH_CURRENCY_FIELD, BOOK_ID_FIELD };

	private static final Field MANDATE_ID_ALLOCATION_FIELD = new Field("MANDATE_ID");
	private static final Field PRODUCT_TYPE_FIELD = new Field(PRODUCT_TYPE);
	private static final Field MIN_ALLOCATION_FIELD = new Field("MIN_ALLOCATION");
	private static final Field MAX_ALLOCATION_FIELD = new Field("MAX_ALLOCATION");

	private static final Field[] PRODUCT_TYPE_ALLOCATION_FIELDS = { MANDATE_ID_ALLOCATION_FIELD, PRODUCT_TYPE_FIELD,
			MIN_ALLOCATION_FIELD, MAX_ALLOCATION_FIELD };

	public static final Table MANDATE_PRODUCT_TYPE_ALLOCATION_TABLE = new Table("MANDATE_PRODUCT_TYPE_ALLOCATION",
			PRODUCT_TYPE_ALLOCATION_FIELDS);

	private static final Field CURRENCY_ALLOCATION_MANDATE_ID_FIELD = new Field("MANDATE_ID");
	private static final Field CURRENCY_ALLOCATION_CURRENCY_ID_FIELD = new Field(CURRENCY_ID);
	private static final Field CURRENCY_ALLOCATION_MIN_ALLOCATION_FIELD = new Field("MIN_ALLOCATION");
	private static final Field CURRENCY_ALLOCATION_MAX_ALLOCATION_FIELD = new Field("MAX_ALLOCATION");

	private static final Field[] CURRENCY_ALLOCATION_FIELDS = { CURRENCY_ALLOCATION_MANDATE_ID_FIELD,
			CURRENCY_ALLOCATION_CURRENCY_ID_FIELD, CURRENCY_ALLOCATION_MIN_ALLOCATION_FIELD,
			CURRENCY_ALLOCATION_MAX_ALLOCATION_FIELD };

	public static final Table MANDATE_CURRENCY_ALLOCATION_TABLE = new Table("MANDATE_CURRENCY_ALLOCATION",
			CURRENCY_ALLOCATION_FIELDS);

	private static final Field CURRENCY_ID_FIELD = new Field(ID);
	private static final Field CURRENCY_ISO_CODE_FIELD = new Field("ISO_CODE");
	private static final Field[] CURRENCY_FIELDS = { CURRENCY_ID_FIELD, CURRENCY_ISO_CODE_FIELD };
	public static final Table CURRENCY_TABLE = new Table("CURRENCY", CURRENCY_FIELDS);

	public static long saveMandate(Mandate mandate) {
		long mandateId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveMandate = (mandate.getId() != 0)
						? TradistaDBUtil.buildUpdatePreparedStatement(con, ID_FIELD, MANDATE_TABLE, FIELDS_FOR_UPDATE)
						: TradistaDBUtil.buildInsertPreparedStatement(con, MANDATE_TABLE, FIELDS_FOR_INSERT);
				PreparedStatement stmtDeleteProductTypeAllocation = (mandate.getId() != 0)
						? TradistaDBUtil.buildDeletePreparedStatement(con, MANDATE_PRODUCT_TYPE_ALLOCATION_TABLE,
								MANDATE_ID_ALLOCATION_FIELD)
						: null;
				PreparedStatement stmtSaveProductTypeAllocation = (mandate.getProductTypeAllocations() != null
						&& !mandate.getProductTypeAllocations().isEmpty())
								? TradistaDBUtil.buildInsertPreparedStatement(con,
										MANDATE_PRODUCT_TYPE_ALLOCATION_TABLE, PRODUCT_TYPE_ALLOCATION_FIELDS)
								: null;
				PreparedStatement stmtDeleteCurrencyAllocation = (mandate.getId() != 0)
						? TradistaDBUtil.buildDeletePreparedStatement(con, MANDATE_CURRENCY_ALLOCATION_TABLE,
								CURRENCY_ALLOCATION_MANDATE_ID_FIELD)
						: null;
				PreparedStatement stmtSaveCurrencyAllocation = (mandate.getCurrencyAllocations() != null
						&& !mandate.getCurrencyAllocations().isEmpty())
								? TradistaDBUtil.buildInsertPreparedStatement(con, MANDATE_CURRENCY_ALLOCATION_TABLE,
										CURRENCY_ALLOCATION_FIELDS)
								: null;) {

			if (mandate.getId() != 0) {
				stmtDeleteProductTypeAllocation.setLong(1, mandate.getId());
				stmtDeleteProductTypeAllocation.executeUpdate();
				stmtDeleteCurrencyAllocation.setLong(1, mandate.getId());
				stmtDeleteCurrencyAllocation.executeUpdate();
			}

			CurrencyBusinessDelegate currencyBusinessDelegate = new CurrencyBusinessDelegate();
			if (mandate.getId() != 0) {
				stmtSaveMandate.setString(1, mandate.getName());
				stmtSaveMandate.setString(2, mandate.getAcceptedRiskLevel().name());
				stmtSaveMandate.setTimestamp(3, Timestamp.from(Instant.now()));
				stmtSaveMandate.setDate(4, Date.valueOf(mandate.getStartDate()));
				stmtSaveMandate.setDate(5, Date.valueOf(mandate.getEndDate()));
				stmtSaveMandate.setBigDecimal(6, mandate.getInitialCashAmount());
				stmtSaveMandate.setString(7, mandate.getInitialCashCurrency().toString());
				stmtSaveMandate.setLong(8, mandate.getBook().getId());
				stmtSaveMandate.setLong(9, mandate.getId());
			} else {
				stmtSaveMandate.setString(1, mandate.getName());
				stmtSaveMandate.setString(2, mandate.getAcceptedRiskLevel().name());
				stmtSaveMandate.setTimestamp(3,
						Timestamp.from((mandate.getCreationTime() != null) ? mandate.getCreationTime() : Instant.now()));
				stmtSaveMandate.setTimestamp(4,
						Timestamp.from((mandate.getLastUpdateTime() != null) ? mandate.getLastUpdateTime() : Instant.now()));
				stmtSaveMandate.setDate(5, Date.valueOf(mandate.getStartDate()));
				stmtSaveMandate.setDate(6, Date.valueOf(mandate.getEndDate()));
				stmtSaveMandate.setBigDecimal(7, mandate.getInitialCashAmount());
				stmtSaveMandate.setString(8, mandate.getInitialCashCurrency().toString());
				stmtSaveMandate.setLong(9, mandate.getBook().getId());
			}
			stmtSaveMandate.executeUpdate();

			if (mandate.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveMandate.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						mandateId = generatedKeys.getLong(1);
						mandate.setId(mandateId);
					} else {
						throw new SQLException("Creating mandate failed, no generated key obtained.");
					}
				}
			} else {
				mandateId = mandate.getId();
			}

			if (mandate.getProductTypeAllocations() != null && !mandate.getProductTypeAllocations().isEmpty()) {
				for (Map.Entry<String, Allocation> entry : mandate.getProductTypeAllocations().entrySet()) {
					stmtSaveProductTypeAllocation.clearParameters();
					stmtSaveProductTypeAllocation.setLong(1, mandateId);
					stmtSaveProductTypeAllocation.setString(2, entry.getKey());
					stmtSaveProductTypeAllocation.setShort(3, entry.getValue().getMinAllocation());
					stmtSaveProductTypeAllocation.setShort(4, entry.getValue().getMaxAllocation());
					stmtSaveProductTypeAllocation.addBatch();
				}
				stmtSaveProductTypeAllocation.executeBatch();
			}

			if (mandate.getCurrencyAllocations() != null && !mandate.getCurrencyAllocations().isEmpty()) {
				for (Map.Entry<String, Allocation> entry : mandate.getCurrencyAllocations().entrySet()) {
					stmtSaveCurrencyAllocation.clearParameters();
					stmtSaveCurrencyAllocation.setLong(1, mandateId);
					try {
						stmtSaveCurrencyAllocation.setLong(2,
								currencyBusinessDelegate.getCurrencyByIsoCode(entry.getKey()).getId());
					} catch (TradistaBusinessException _) {
						// Should not appear here.
					}
					stmtSaveCurrencyAllocation.setShort(3, entry.getValue().getMinAllocation());
					stmtSaveCurrencyAllocation.setShort(4, entry.getValue().getMaxAllocation());
					stmtSaveCurrencyAllocation.addBatch();
				}
				stmtSaveCurrencyAllocation.executeBatch();
			}

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		mandate.setId(mandateId);
		return mandateId;
	}

	public static Mandate getMandateById(long id) {
		Mandate mandate = null;
		StringBuilder sqlMandate = new StringBuilder(TradistaDBUtil.buildSelectQuery(MANDATE_TABLE));
		TradistaDBUtil.addParameterizedFilter(sqlMandate, ID_FIELD);

		StringBuilder sqlProductAlloc = new StringBuilder(
				TradistaDBUtil.buildSelectQuery(MANDATE_PRODUCT_TYPE_ALLOCATION_TABLE));
		TradistaDBUtil.addParameterizedFilter(sqlProductAlloc, MANDATE_ID_ALLOCATION_FIELD);

		StringBuilder sqlCurrencyAlloc = new StringBuilder(TradistaDBUtil.buildSelectQuery(
				MANDATE_CURRENCY_ALLOCATION_TABLE,
				Join.innerEq(CURRENCY_TABLE, CURRENCY_ALLOCATION_CURRENCY_ID_FIELD, CURRENCY_ID_FIELD)));
		TradistaDBUtil.addParameterizedFilter(sqlCurrencyAlloc, CURRENCY_ALLOCATION_MANDATE_ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetMandateById = con.prepareStatement(sqlMandate.toString());
				PreparedStatement stmtGetProductTypeAllocationsByMandateId = con
						.prepareStatement(sqlProductAlloc.toString());
				PreparedStatement stmtGetCurrencyAllocationsByMandateId = con
						.prepareStatement(sqlCurrencyAlloc.toString())) {
			Map<String, Allocation> allocations = new HashMap<>();
			stmtGetMandateById.setLong(1, id);
			try (ResultSet results = stmtGetMandateById.executeQuery()) {
				while (results.next()) {
					Mandate.Builder builder = new Mandate.Builder(results.getString(NAME_FIELD.getName()));
					builder.id(results.getLong(ID_FIELD.getName()));
					builder.acceptedRiskLevel(
							RiskLevel.valueOf(results.getString(ACCEPTED_RISK_LEVEL_FIELD.getName())));
					Timestamp creationTime = results.getTimestamp(CREATION_TIME_FIELD.getName());
					if (creationTime != null) {
						builder.creationTime(creationTime.toInstant());
					}
					Timestamp lastUpdateTime = results.getTimestamp(LAST_UPDATE_TIME_FIELD.getName());
					if (lastUpdateTime != null) {
						builder.lastUpdateTime(lastUpdateTime.toInstant());
					}
					builder.startDate(results.getDate(START_DATE_FIELD.getName()).toLocalDate());
					builder.endDate(results.getDate(END_DATE_FIELD.getName()).toLocalDate());
					builder.initialCashAmount(results.getBigDecimal(INITIAL_CASH_AMOUNT_FIELD.getName()));
					try {
						builder.initialCashCurrency(new CurrencyBusinessDelegate()
								.getCurrencyByIsoCode(results.getString(INITIAL_CASH_CURRENCY_FIELD.getName())));
						builder.book(new BookBusinessDelegate()
								.getBookById(results.getLong(BOOK_ID_FIELD.getName())));
					} catch (TradistaBusinessException _) {
						// Should not appear at this stage
					}
					mandate = builder.build();
				}

				if (mandate == null) {
					return null;
				}
			}

			stmtGetProductTypeAllocationsByMandateId.setLong(1, id);
			try (ResultSet results = stmtGetProductTypeAllocationsByMandateId.executeQuery()) {
				while (results.next()) {
					Allocation alloc = mandate.new Allocation();
					alloc.setMinAllocation(results.getShort(MIN_ALLOCATION_FIELD.getName()));
					alloc.setMaxAllocation(results.getShort(MAX_ALLOCATION_FIELD.getName()));
					allocations.put(results.getString(PRODUCT_TYPE_FIELD.getName()), alloc);
				}

				mandate.setProductTypeAllocations(allocations);
			}

			stmtGetCurrencyAllocationsByMandateId.setLong(1, id);
			try (ResultSet results = stmtGetCurrencyAllocationsByMandateId.executeQuery()) {

				allocations = new HashMap<>();

				while (results.next()) {
					Allocation alloc = mandate.new Allocation();
					alloc.setMinAllocation(results.getShort(CURRENCY_ALLOCATION_MIN_ALLOCATION_FIELD.getName()));
					alloc.setMaxAllocation(results.getShort(CURRENCY_ALLOCATION_MAX_ALLOCATION_FIELD.getName()));
					allocations.put(results.getString(CURRENCY_ISO_CODE_FIELD.getName()), alloc);
				}

				mandate.setCurrencyAllocations(allocations);
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return mandate;
	}

	public static Mandate getMandateByName(String name) {
		Mandate mandate = null;
		StringBuilder sqlMandate = new StringBuilder(TradistaDBUtil.buildSelectQuery(MANDATE_TABLE));
		TradistaDBUtil.addParameterizedFilter(sqlMandate, NAME_FIELD);

		StringBuilder sqlProductAlloc = new StringBuilder(
				TradistaDBUtil.buildSelectQuery(MANDATE_PRODUCT_TYPE_ALLOCATION_TABLE));
		TradistaDBUtil.addParameterizedFilter(sqlProductAlloc, MANDATE_ID_ALLOCATION_FIELD);

		StringBuilder sqlCurrencyAlloc = new StringBuilder(TradistaDBUtil.buildSelectQuery(
				MANDATE_CURRENCY_ALLOCATION_TABLE,
				Join.innerEq(CURRENCY_TABLE, CURRENCY_ALLOCATION_CURRENCY_ID_FIELD, CURRENCY_ID_FIELD)));
		TradistaDBUtil.addParameterizedFilter(sqlCurrencyAlloc, CURRENCY_ALLOCATION_MANDATE_ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetMandateByName = con.prepareStatement(sqlMandate.toString());
				PreparedStatement stmtGetProductTypeAllocationsByMandateId = con
						.prepareStatement(sqlProductAlloc.toString());
				PreparedStatement stmtGetCurrencyAllocationsByMandateId = con
						.prepareStatement(sqlCurrencyAlloc.toString())) {
			Map<String, Allocation> allocations = new HashMap<>();
			stmtGetMandateByName.setString(1, name);
			try (ResultSet results = stmtGetMandateByName.executeQuery()) {
				while (results.next()) {
					Mandate.Builder builder = new Mandate.Builder(results.getString(NAME_FIELD.getName()));
					builder.id(results.getLong(ID_FIELD.getName()));
					builder.acceptedRiskLevel(
							RiskLevel.valueOf(results.getString(ACCEPTED_RISK_LEVEL_FIELD.getName())));
					Timestamp creationTime = results.getTimestamp(CREATION_TIME_FIELD.getName());
					if (creationTime != null) {
						builder.creationTime(creationTime.toInstant());
					}
					Timestamp lastUpdateTime = results.getTimestamp(LAST_UPDATE_TIME_FIELD.getName());
					if (lastUpdateTime != null) {
						builder.lastUpdateTime(lastUpdateTime.toInstant());
					}
					builder.startDate(results.getDate(START_DATE_FIELD.getName()).toLocalDate());
					builder.endDate(results.getDate(END_DATE_FIELD.getName()).toLocalDate());
					builder.initialCashAmount(results.getBigDecimal(INITIAL_CASH_AMOUNT_FIELD.getName()));
					try {
						builder.initialCashCurrency(new CurrencyBusinessDelegate()
								.getCurrencyByIsoCode(results.getString(INITIAL_CASH_CURRENCY_FIELD.getName())));
						builder.book(new BookBusinessDelegate()
								.getBookById(results.getLong(BOOK_ID_FIELD.getName())));
					} catch (TradistaBusinessException _) {
						// Should not appear at this stage
					}
					mandate = builder.build();
				}

				if (mandate == null) {
					return null;
				}
			}

			stmtGetProductTypeAllocationsByMandateId.setLong(1, mandate.getId());
			try (ResultSet results = stmtGetProductTypeAllocationsByMandateId.executeQuery()) {
				while (results.next()) {
					Allocation alloc = mandate.new Allocation();
					alloc.setMinAllocation(results.getShort(MIN_ALLOCATION_FIELD.getName()));
					alloc.setMaxAllocation(results.getShort(MAX_ALLOCATION_FIELD.getName()));
					allocations.put(results.getString(PRODUCT_TYPE_FIELD.getName()), alloc);
				}

				mandate.setProductTypeAllocations(allocations);
			}
			stmtGetCurrencyAllocationsByMandateId.setLong(1, mandate.getId());
			try (ResultSet results = stmtGetCurrencyAllocationsByMandateId.executeQuery()) {

				allocations = new HashMap<>();

				while (results.next()) {
					Allocation alloc = mandate.new Allocation();
					alloc.setMinAllocation(results.getShort(CURRENCY_ALLOCATION_MIN_ALLOCATION_FIELD.getName()));
					alloc.setMaxAllocation(results.getShort(CURRENCY_ALLOCATION_MAX_ALLOCATION_FIELD.getName()));
					allocations.put(results.getString(CURRENCY_ISO_CODE_FIELD.getName()), alloc);
				}

				mandate.setCurrencyAllocations(allocations);
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return mandate;
	}

}