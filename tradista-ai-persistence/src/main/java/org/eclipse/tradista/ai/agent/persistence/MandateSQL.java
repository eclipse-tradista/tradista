package org.eclipse.tradista.ai.agent.persistence;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tradista.ai.agent.model.Mandate;
import org.eclipse.tradista.ai.agent.model.Mandate.Allocation;
import org.eclipse.tradista.ai.agent.model.Mandate.RiskLevel;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
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

	public static long saveMandate(Mandate mandate) {
		long mandateId = 0;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveMandate = (mandate.getId() != 0) ? con.prepareStatement(
						"UPDATE MANDATE SET NAME = ?, ACCEPTED_RISK_LEVEL = ?, CREATION_DATETIME = ?,  START_DATE = ?, END_DATE = ?, INITIAL_CASH_AMOUNT = ?, INITIAL_CASH_CURRENCY = ?, BOOK_ID = ? WHERE ID = ? ")
						: con.prepareStatement(
								"INSERT INTO MANDATE(NAME, ACCEPTED_RISK_LEVEL, CREATION_DATETIME, START_DATE, END_DATE, INITIAL_CASH_AMOUNT, INITIAL_CASH_CURRENCY, BOOK_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ",
								Statement.RETURN_GENERATED_KEYS);
				PreparedStatement stmtDeleteProductTypeAllocation = (mandate.getId() != 0)
						? con.prepareStatement("DELETE FROM MANDATE_PRODUCT_TYPE_ALLOCATION WHERE MANDATE_ID = ?")
						: null;
				PreparedStatement stmtSaveProductTypeAllocation = (mandate.getProductTypeAllocations() != null
						&& !mandate.getProductTypeAllocations().isEmpty()) ? con.prepareStatement(
								"INSERT INTO MANDATE_PRODUCT_TYPE_ALLOCATION(MANDATE_ID, PRODUCT_TYPE, MIN_ALLOCATION, MAX_ALLOCATION) VALUES (?, ?, ?, ?)")
								: null;
				PreparedStatement stmtDeleteCurrencyAllocation = (mandate.getId() != 0)
						? con.prepareStatement("DELETE FROM MANDATE_CURRENCY_ALLOCATION WHERE MANDATE_ID = ?")
						: null;
				PreparedStatement stmtSaveCurrencyAllocation = (mandate.getProductTypeAllocations() != null
						&& !mandate.getProductTypeAllocations().isEmpty()) ? con.prepareStatement(
								"INSERT INTO MANDATE_CURRENCY_ALLOCATION(MANDATE_ID, CURRENCY_ID, MIN_ALLOCATION, MAX_ALLOCATION) VALUES (?, ?, ?, ?)")
								: null;) {

			if (mandate.getId() != 0) {
				stmtDeleteProductTypeAllocation.setLong(1, mandate.getId());
				stmtDeleteProductTypeAllocation.executeUpdate();
				stmtDeleteCurrencyAllocation.setLong(1, mandate.getId());
				stmtDeleteCurrencyAllocation.executeUpdate();
			}

			CurrencyBusinessDelegate currencyBusinessDelegate = new CurrencyBusinessDelegate();
			if (mandate.getId() != 0) {
				stmtSaveMandate.setLong(9, mandate.getId());
			}
			stmtSaveMandate.setString(1, mandate.getName());
			stmtSaveMandate.setString(2, mandate.getAcceptedRiskLevel().name());
			stmtSaveMandate.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
			stmtSaveMandate.setDate(4, Date.valueOf(mandate.getStartDate()));
			stmtSaveMandate.setDate(5, Date.valueOf(mandate.getEndDate()));
			stmtSaveMandate.setBigDecimal(6, mandate.getInitialCashAmount());
			stmtSaveMandate.setString(7, mandate.getInitialCashCurrency().toString());
			stmtSaveMandate.setLong(8, mandate.getBook().getId());
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
					} catch (TradistaBusinessException abe) {
						// Should not appear here.
					}
					stmtSaveCurrencyAllocation.setShort(3, entry.getValue().getMinAllocation());
					stmtSaveCurrencyAllocation.setShort(4, entry.getValue().getMaxAllocation());
					stmtSaveCurrencyAllocation.addBatch();
				}
				stmtSaveCurrencyAllocation.executeBatch();
			}

		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		mandate.setId(mandateId);
		return mandateId;
	}

	public static Mandate getMandateById(long id) {
		Mandate mandate = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetMandateById = con.prepareStatement("SELECT * FROM MANDATE WHERE ID = ?");
				PreparedStatement stmtGetProductTypeAllocationsByMandateId = con
						.prepareStatement("SELECT * FROM MANDATE_PRODUCT_TYPE_ALLOCATION WHERE MANDATE_ID = ?");
				PreparedStatement stmtGetCurrencyAllocationsByMandateId = con.prepareStatement(
						"SELECT * FROM MANDATE_CURRENCY_ALLOCATION, CURRENCY WHERE MANDATE_ID = ? AND MANDATE_CURRENCY_ALLOCATION.CURRENCY_ID = CURRENCY.ID")) {
			Map<String, Allocation> allocations = new HashMap<String, Allocation>();
			stmtGetMandateById.setLong(1, id);
			try (ResultSet results = stmtGetMandateById.executeQuery()) {
				while (results.next()) {
					mandate = new Mandate(results.getString("name"));
					mandate.setId(results.getLong("id"));
					mandate.setAcceptedRiskLevel(RiskLevel.valueOf(results.getString("accepted_risk_level")));
					mandate.setCreationDateTime(results.getTimestamp("creation_datetime").toLocalDateTime());
					mandate.setStartDate(results.getDate("start_date").toLocalDate());
					mandate.setEndDate(results.getDate("end_date").toLocalDate());
					mandate.setInitialCashAmount(results.getBigDecimal("initial_cash_amount"));
					try {
						mandate.setInitialCashCurrency(new CurrencyBusinessDelegate()
								.getCurrencyByIsoCode(results.getString("initial_cash_currency")));
						mandate.setBook(new BookBusinessDelegate().getBookById(results.getLong("book_id")));
					} catch (TradistaBusinessException abe) {
						// Should not appear at this stage
					}
				}

				if (mandate == null) {
					return null;
				}
			}

			stmtGetProductTypeAllocationsByMandateId.setLong(1, id);
			try (ResultSet results = stmtGetProductTypeAllocationsByMandateId.executeQuery()) {
				while (results.next()) {
					Allocation alloc = mandate.new Allocation();
					alloc.setMinAllocation(results.getShort("min_allocation"));
					alloc.setMaxAllocation(results.getShort("max_allocation"));
					allocations.put(results.getString("product_type"), alloc);
				}

				mandate.setProductTypeAllocations(allocations);
			}

			stmtGetCurrencyAllocationsByMandateId.setLong(1, id);
			try (ResultSet results = stmtGetCurrencyAllocationsByMandateId.executeQuery()) {

				allocations = new HashMap<String, Allocation>();

				while (results.next()) {
					Allocation alloc = mandate.new Allocation();
					alloc.setMinAllocation(results.getShort("min_allocation"));
					alloc.setMaxAllocation(results.getShort("max_allocation"));
					allocations.put(results.getString("iso_code"), alloc);
				}

				mandate.setCurrencyAllocations(allocations);
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return mandate;
	}

	public static Mandate getMandateByName(String name) {
		Mandate mandate = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetMandateByName = con.prepareStatement("SELECT * FROM MANDATE WHERE NAME = ?");
				PreparedStatement stmtGetProductTypeAllocationsByMandateId = con
						.prepareStatement("SELECT * FROM MANDATE_PRODUCT_TYPE_ALLOCATION WHERE MANDATE_ID = ?");
				PreparedStatement stmtGetCurrencyAllocationsByMandateId = con.prepareStatement(
						"SELECT * FROM MANDATE_CURRENCY_ALLOCATION, CURRENCY WHERE MANDATE_ID = ? AND MANDATE_CURRENCY_ALLOCATION.CURRENCY_ID = CURRENCY.ID")) {
			Map<String, Allocation> allocations = new HashMap<String, Allocation>();
			stmtGetMandateByName.setString(1, name);
			try (ResultSet results = stmtGetMandateByName.executeQuery()) {
				while (results.next()) {
					mandate = new Mandate(results.getString("name"));
					mandate.setId(results.getLong("id"));
					mandate.setAcceptedRiskLevel(RiskLevel.valueOf(results.getString("accepted_risk_level")));
					mandate.setCreationDateTime(results.getTimestamp("creation_datetime").toLocalDateTime());
					mandate.setStartDate(results.getDate("start_date").toLocalDate());
					mandate.setEndDate(results.getDate("end_date").toLocalDate());
					mandate.setInitialCashAmount(results.getBigDecimal("initial_cash_amount"));
					try {
						mandate.setInitialCashCurrency(new CurrencyBusinessDelegate()
								.getCurrencyByIsoCode(results.getString("initial_cash_currency")));
						mandate.setBook(new BookBusinessDelegate().getBookById(results.getLong("book_id")));
					} catch (TradistaBusinessException abe) {
						// Should not appear at this stage
					}
				}

				if (mandate == null) {
					return null;
				}
			}

			stmtGetProductTypeAllocationsByMandateId.setLong(1, mandate.getId());
			try (ResultSet results = stmtGetProductTypeAllocationsByMandateId.executeQuery()) {
				while (results.next()) {
					Allocation alloc = mandate.new Allocation();
					alloc.setMinAllocation(results.getShort("min_allocation"));
					alloc.setMaxAllocation(results.getShort("max_allocation"));
					allocations.put(results.getString("product_type"), alloc);
				}
			}
			stmtGetCurrencyAllocationsByMandateId.setLong(1, mandate.getId());
			try (ResultSet results = stmtGetCurrencyAllocationsByMandateId.executeQuery()) {

				allocations = new HashMap<String, Allocation>();

				while (results.next()) {
					Allocation alloc = mandate.new Allocation();
					alloc.setMinAllocation(results.getShort("min_allocation"));
					alloc.setMaxAllocation(results.getShort("max_allocation"));
					allocations.put(results.getString("iso_code"), alloc);
				}

				mandate.setCurrencyAllocations(allocations);
			}
		} catch (SQLException sqle) {
			// TODO Manage logs
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return mandate;
	}

}