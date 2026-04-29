package org.eclipse.tradista.security.gcrepo.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.BOOK_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.NAME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PROCESSING_ORG_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.persistence.BookSQL;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;
import org.eclipse.tradista.security.repo.model.AllocationConfiguration;;

/********************************************************************************
 * Copyright (c) 2024 Olivier Asuncion
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

public class AllocationConfigurationSQL {

	private static final Field NAME_FIELD = new Field(NAME);
	private static final Field PROCESSING_ORG_ID_FIELD = new Field(PROCESSING_ORG_ID);
	private static final Field ID_FIELD = new Field(ID);

	private static final Field BOOK_ID_FIELD = new Field(BOOK_ID);
	private static final Field ALLOCATION_CONFIGURATION_ID_FIELD = new Field("ALLOCATION_CONFIGURATION_ID");

	private static final Field[] ALLOCATION_CONFIGURATION_FIELDS = { NAME_FIELD, PROCESSING_ORG_ID_FIELD, ID_FIELD };
	private static final Field[] ALLOCATION_CONFIGURATION_FIELDS_FOR_INSERT_OR_UPDATE = { NAME_FIELD,
			PROCESSING_ORG_ID_FIELD };
	private static final Field[] ALLOCATION_CONFIGURATION_BOOK_FIELDS = { ALLOCATION_CONFIGURATION_ID_FIELD,
			BOOK_ID_FIELD };

	public static final Table ALLOCATION_CONFIGURATION_TABLE = new Table("ALLOCATION_CONFIGURATION",
			ALLOCATION_CONFIGURATION_FIELDS);
	public static final Table ALLOCATION_CONFIGURATION_BOOK_TABLE = new Table("ALLOCATION_CONFIGURATION_BOOK",
			ALLOCATION_CONFIGURATION_BOOK_FIELDS);

	public static long saveAllocationConfiguration(AllocationConfiguration allocationConfiguration) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveAllocationConfiguration = (allocationConfiguration.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, ALLOCATION_CONFIGURATION_TABLE,
								ALLOCATION_CONFIGURATION_FIELDS_FOR_INSERT_OR_UPDATE)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, ID_FIELD, ALLOCATION_CONFIGURATION_TABLE,
								ALLOCATION_CONFIGURATION_FIELDS_FOR_INSERT_OR_UPDATE);
				PreparedStatement stmtDeleteAllocationConfigurationBook = TradistaDBUtil.buildDeletePreparedStatement(
						con, ALLOCATION_CONFIGURATION_BOOK_TABLE, ALLOCATION_CONFIGURATION_ID_FIELD);
				PreparedStatement stmtSaveAllocationConfigurationBook = TradistaDBUtil.buildInsertPreparedStatement(con,
						ALLOCATION_CONFIGURATION_BOOK_TABLE, ALLOCATION_CONFIGURATION_BOOK_FIELDS)) {
			if (allocationConfiguration.getId() != 0) {
				stmtSaveAllocationConfiguration.setLong(3, allocationConfiguration.getId());
			}
			stmtSaveAllocationConfiguration.setString(1, allocationConfiguration.getName());
			if (allocationConfiguration.getProcessingOrg() == null) {
				stmtSaveAllocationConfiguration.setNull(2, Types.BIGINT);
			} else {
				stmtSaveAllocationConfiguration.setLong(2, allocationConfiguration.getProcessingOrg().getId());
			}
			stmtSaveAllocationConfiguration.executeUpdate();

			if (allocationConfiguration.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveAllocationConfiguration.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						allocationConfiguration.setId(generatedKeys.getLong(1));
					} else {
						throw new SQLException("Creating allocation configuration failed, no generated key obtained.");
					}
				}
			}
			if (allocationConfiguration.getId() != 0) {
				stmtDeleteAllocationConfigurationBook.setLong(1, allocationConfiguration.getId());
				stmtDeleteAllocationConfigurationBook.executeUpdate();
			}
			if (allocationConfiguration.getBooks() != null) {
				for (Book book : allocationConfiguration.getBooks()) {
					stmtSaveAllocationConfigurationBook.clearParameters();
					stmtSaveAllocationConfigurationBook.setLong(1, allocationConfiguration.getId());
					stmtSaveAllocationConfigurationBook.setLong(2, book.getId());
					stmtSaveAllocationConfigurationBook.addBatch();
				}
			}

			stmtSaveAllocationConfigurationBook.executeBatch();

		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return allocationConfiguration.getId();
	}

	private static AllocationConfiguration buildAllocationConfiguration(ResultSet results, Connection con)
			throws SQLException {
		long allocationConfigurationId = results.getLong(ID_FIELD.getName());

		long poId = results.getLong(PROCESSING_ORG_ID_FIELD.getName());
		LegalEntity po = null;
		if (poId > 0) {
			po = LegalEntitySQL.getLegalEntityById(poId);
		}
		AllocationConfiguration allocationConfiguration = new AllocationConfiguration(
				results.getString(NAME_FIELD.getName()), po);
		allocationConfiguration.setId(allocationConfigurationId);

		Set<Book> books = new HashSet<>();
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(ALLOCATION_CONFIGURATION_BOOK_TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, ALLOCATION_CONFIGURATION_ID_FIELD);
		try (PreparedStatement stmtGetBooks = con.prepareStatement(sql.toString())) {
			stmtGetBooks.setLong(1, allocationConfigurationId);
			try (ResultSet booksResults = stmtGetBooks.executeQuery()) {
				while (booksResults.next()) {
					long bookId = booksResults.getLong(BOOK_ID_FIELD.getName());
					Book book = BookSQL.getBookById(bookId);
					books.add(book);
				}
			}
		}
		if (!books.isEmpty()) {
			allocationConfiguration.setBooks(books);
		}

		return allocationConfiguration;
	}

	public static Set<AllocationConfiguration> getAllAllocationConfigurations() {
		Set<AllocationConfiguration> allocationConfigurations = null;
		StringBuilder sqlQuery = new StringBuilder(TradistaDBUtil.buildSelectQuery(ALLOCATION_CONFIGURATION_TABLE));
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(sqlQuery.toString())) {
			try (ResultSet results = stmt.executeQuery()) {
				while (results.next()) {
					if (allocationConfigurations == null) {
						allocationConfigurations = new HashSet<>();
					}
					allocationConfigurations.add(buildAllocationConfiguration(results, con));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return allocationConfigurations;
	}

	public static Set<AllocationConfiguration> getAllocationConfigurationsByPoId(long poId) {
		Set<AllocationConfiguration> allocationConfigurations = null;
		StringBuilder sqlQuery = new StringBuilder(TradistaDBUtil.buildSelectQuery(ALLOCATION_CONFIGURATION_TABLE));
		TradistaDBUtil.addParameterizedFilter(sqlQuery, PROCESSING_ORG_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(sqlQuery.toString())) {
			stmt.setLong(1, poId);
			try (ResultSet results = stmt.executeQuery()) {
				while (results.next()) {
					if (allocationConfigurations == null) {
						allocationConfigurations = new HashSet<>();
					}
					allocationConfigurations.add(buildAllocationConfiguration(results, con));
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return allocationConfigurations;
	}

	public static AllocationConfiguration getAllocationConfigurationById(long id) {
		AllocationConfiguration allocationConfiguration = null;
		StringBuilder sqlQuery = new StringBuilder(TradistaDBUtil.buildSelectQuery(ALLOCATION_CONFIGURATION_TABLE));
		TradistaDBUtil.addParameterizedFilter(sqlQuery, ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllocationConfigurationById = con.prepareStatement(sqlQuery.toString())) {
			stmtGetAllocationConfigurationById.setLong(1, id);
			try (ResultSet results = stmtGetAllocationConfigurationById.executeQuery()) {
				while (results.next()) {
					allocationConfiguration = buildAllocationConfiguration(results, con);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return allocationConfiguration;
	}

	public static AllocationConfiguration getAllocationConfigurationByNameAndPoId(String name, long poId) {
		AllocationConfiguration allocationConfiguration = null;
		StringBuilder sqlQuery = new StringBuilder(TradistaDBUtil.buildSelectQuery(ALLOCATION_CONFIGURATION_TABLE));
		TradistaDBUtil.addParameterizedFilter(sqlQuery, NAME_FIELD);
		if (poId > 0) {
			TradistaDBUtil.addParameterizedFilter(sqlQuery, PROCESSING_ORG_ID_FIELD);
		} else {
			TradistaDBUtil.addIsNullFilter(sqlQuery, PROCESSING_ORG_ID_FIELD);
		}
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllocationConfigurationByNameAndPoId = con
						.prepareStatement(sqlQuery.toString())) {
			stmtGetAllocationConfigurationByNameAndPoId.setString(1, name);
			if (poId > 0) {
				stmtGetAllocationConfigurationByNameAndPoId.setLong(2, poId);
			}
			try (ResultSet results = stmtGetAllocationConfigurationByNameAndPoId.executeQuery()) {
				while (results.next()) {
					allocationConfiguration = buildAllocationConfiguration(results, con);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return allocationConfiguration;
	}

}