package finance.tradista.security.gcrepo.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import finance.tradista.core.book.model.Book;
import finance.tradista.core.book.persistence.BookSQL;
import finance.tradista.core.common.exception.TradistaTechnicalException;
import finance.tradista.core.common.persistence.db.TradistaDB;
import finance.tradista.core.legalentity.persistence.LegalEntitySQL;
import finance.tradista.security.gcrepo.model.AllocationConfiguration;

/*
 * Copyright 2024 Olivier Asuncion
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.    */

public class AllocationConfigurationSQL {

	private static final String NAME = "NAME";
	private static final String ID = "ID";
	private static final String PROCESSING_ORG_ID = "PROCESSING_ORG_ID";
	private static final String BOOK_ID = "BOOK_ID";

	public static long saveAllocationConfiguration(AllocationConfiguration allocationConfiguration) {
		long allocationConfigurationId = 0;

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveAllocationConfiguration = (allocationConfiguration.getId() == 0)
						? con.prepareStatement(
								"INSERT INTO ALLOCATION_CONFIGURATION(NAME, PROCESSING_ORG_ID) VALUES (?, ?) ",
								Statement.RETURN_GENERATED_KEYS)
						: con.prepareStatement(
								"UPDATE ALLOCATION_CONFIGURATION SET NAME=?, PROCESSING_ORG_ID=? WHERE ID=? ");
				PreparedStatement stmtDeleteAllocationConfigurationBook = con.prepareStatement(
						"DELETE FROM ALLOCATION_CONFIGURATION_BOOK WHERE ALLOCATION_CONFIGURATION_ID = ? ");
				PreparedStatement stmtSaveAllocationConfigurationBook = con
						.prepareStatement("INSERT INTO ALLOCATION_CONFIGURATION_BOOK VALUES (?, ?) ");) {
			if (allocationConfiguration.getId() != 0) {
				stmtSaveAllocationConfiguration.setLong(3, allocationConfiguration.getId());
			}
			stmtSaveAllocationConfiguration.setString(1, allocationConfiguration.getName());
			stmtSaveAllocationConfiguration.setLong(2, allocationConfiguration.getProcessingOrg().getId());
			stmtSaveAllocationConfiguration.executeUpdate();

			if (allocationConfiguration.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveAllocationConfiguration.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						allocationConfigurationId = generatedKeys.getLong(1);
					} else {
						throw new SQLException("Creating allocation configuration failed, no generated key obtained.");
					}
				}
			} else {
				allocationConfigurationId = allocationConfiguration.getId();
			}
			if (allocationConfiguration.getId() != 0) {
				stmtDeleteAllocationConfigurationBook.setLong(1, allocationConfiguration.getId());
				stmtDeleteAllocationConfigurationBook.executeUpdate();
			}
			if (allocationConfiguration.getBooks() != null) {
				for (Book book : allocationConfiguration.getBooks()) {
					stmtSaveAllocationConfigurationBook.clearParameters();
					stmtSaveAllocationConfigurationBook.setLong(1, allocationConfigurationId);
					stmtSaveAllocationConfigurationBook.setLong(2, book.getId());
					stmtSaveAllocationConfigurationBook.addBatch();
				}
			}

			stmtSaveAllocationConfigurationBook.executeBatch();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		allocationConfiguration.setId(allocationConfigurationId);
		return allocationConfigurationId;
	}

	public static Set<AllocationConfiguration> getAllAllocationConfigurations() {
		Set<AllocationConfiguration> allocationConfigurations = null;
		Map<Long, AllocationConfiguration> allocationConfigurationsMap = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllAllocationConfigurations = con.prepareStatement(
						"SELECT ALLOCATION_CONFIGURATION.NAME, ALLOCATION_CONFIGURATION.ID, ALLOCATION_CONFIGURATION.PROCESSING_ORG_ID, ALLOCATION_CONFIGURATION_BOOK.BOOK_ID FROM ALLOCATION_CONFIGURATION LEFT OUTER JOIN ALLOCATION_CONFIGURATION_BOOK ON ALLOCATION_CONFIGURATION.ID = ALLOCATION_CONFIGURATION_BOOK.ALLOCATION_CONFIGURATION_ID");
				ResultSet results = stmtGetAllAllocationConfigurations.executeQuery()) {
			while (results.next()) {
				if (allocationConfigurationsMap == null) {
					allocationConfigurationsMap = new HashMap<>();
				}
				long allocationConfigurationId = results.getLong(ID);

				AllocationConfiguration allocationConfiguration;
				if (allocationConfigurationsMap.containsKey(allocationConfigurationId)) {
					allocationConfiguration = allocationConfigurationsMap.get(allocationConfigurationId);
				} else {
					allocationConfiguration = new AllocationConfiguration(results.getString(NAME),
							LegalEntitySQL.getLegalEntityById(results.getLong(PROCESSING_ORG_ID)));
					allocationConfiguration.setId(allocationConfigurationId);
				}
				Long bookId = results.getLong(BOOK_ID);
				if (bookId != 0) {
					Set<Book> books;
					if (allocationConfiguration.getBooks() == null) {
						books = new HashSet<>();
					} else {
						books = allocationConfiguration.getBooks();
					}

					Book book = BookSQL.getBookById(bookId);
					books.add(book);
					allocationConfiguration.setBooks(books);
				}
				allocationConfigurationsMap.put(allocationConfigurationId, allocationConfiguration);

			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		if (allocationConfigurationsMap != null) {
			allocationConfigurations = new HashSet<>(allocationConfigurationsMap.values());
		}
		return allocationConfigurations;
	}

	public static AllocationConfiguration getAllocationConfigurationById(long id) {
		AllocationConfiguration allocationConfiguration = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllocationConfigurationById = con.prepareStatement(
						"SELECT ALLOCATION_CONFIGURATION.NAME, ALLOCATION_CONFIGURATION.ID, ALLOCATION_CONFIGURATION.PROCESSING_ORG_ID, ALLOCATION_CONFIGURATION_BOOK.BOOK_ID FROM ALLOCATION_CONFIGURATION LEFT OUTER JOIN ALLOCATION_CONFIGURATION_BOOK ON ALLOCATION_CONFIGURATION.ID = ALLOCATION_CONFIGURATION_BOOK.ALLOCATION_CONFIGURATION_ID WHERE ALLOCATION_CONFIGURATION.ID = ?");) {
			stmtGetAllocationConfigurationById.setLong(1, id);
			ResultSet results = stmtGetAllocationConfigurationById.executeQuery();
			while (results.next()) {
				if (allocationConfiguration == null) {
					allocationConfiguration = new AllocationConfiguration(results.getString(NAME),
							LegalEntitySQL.getLegalEntityById(results.getLong(PROCESSING_ORG_ID)));
					allocationConfiguration.setId(results.getLong(ID));
				}
				Long bookId = results.getLong(BOOK_ID);
				if (bookId != 0) {
					Set<Book> books;
					if (allocationConfiguration.getBooks() == null) {
						books = new HashSet<>();
					} else {
						books = allocationConfiguration.getBooks();
					}

					books.add(BookSQL.getBookById(bookId));
					allocationConfiguration.setBooks(books);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return allocationConfiguration;
	}

	public static AllocationConfiguration getAllocationConfigurationByName(String name) {
		AllocationConfiguration allocationConfiguration = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllocationConfigurationByName = con.prepareStatement(
						"SELECT ALLOCATION_CONFIGURATION.NAME, ALLOCATION_CONFIGURATION.ID, ALLOCATION_CONFIGURATION.PROCESSING_ORG_ID, ALLOCATION_CONFIGURATION_BOOK.BOOK_ID FROM ALLOCATION_CONFIGURATION LEFT OUTER JOIN ALLOCATION_CONFIGURATION_BOOK ON ALLOCATION_CONFIGURATION.ID = ALLOCATION_CONFIGURATION_BOOK.ALLOCATION_CONFIGURATION_ID WHERE ALLOCATION_CONFIGURATION.NAME = ?");) {
			stmtGetAllocationConfigurationByName.setString(1, name);
			ResultSet results = stmtGetAllocationConfigurationByName.executeQuery();
			while (results.next()) {
				if (allocationConfiguration == null) {
					allocationConfiguration = new AllocationConfiguration(results.getString(NAME),
							LegalEntitySQL.getLegalEntityById(results.getLong(PROCESSING_ORG_ID)));
					allocationConfiguration.setId(results.getLong(ID));
				}
				Long bookId = results.getLong(BOOK_ID);
				if (bookId != 0) {
					Set<Book> books;
					if (allocationConfiguration.getBooks() == null) {
						books = new HashSet<>();
					} else {
						books = allocationConfiguration.getBooks();
					}
					books.add(BookSQL.getBookById(bookId));
					allocationConfiguration.setBooks(books);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return allocationConfiguration;
	}

	public static Object getAllocationConfigurationByNameAndPoId(String name, long id) {
		AllocationConfiguration allocationConfiguration = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllocationConfigurationByName = con.prepareStatement(
						"SELECT ALLOCATION_CONFIGURATION.NAME, ALLOCATION_CONFIGURATION.ID, ALLOCATION_CONFIGURATION.PROCESSING_ORG_ID, ALLOCATION_CONFIGURATION_BOOK.BOOK_ID FROM ALLOCATION_CONFIGURATION LEFT OUTER JOIN ALLOCATION_CONFIGURATION_BOOK ON ALLOCATION_CONFIGURATION.ID = ALLOCATION_CONFIGURATION_BOOK.ALLOCATION_CONFIGURATION_ID WHERE ALLOCATION_CONFIGURATION.NAME = ? AND ALLOCATION_CONFIGURATION.PROCESSING_ORG_ID = ?");) {
			stmtGetAllocationConfigurationByName.setString(1, name);
			stmtGetAllocationConfigurationByName.setLong(2, id);
			ResultSet results = stmtGetAllocationConfigurationByName.executeQuery();
			while (results.next()) {
				if (allocationConfiguration == null) {
					allocationConfiguration = new AllocationConfiguration(results.getString(NAME),
							LegalEntitySQL.getLegalEntityById(results.getLong(PROCESSING_ORG_ID)));
					allocationConfiguration.setId(results.getLong(ID));
				}
				Long bookId = results.getLong(BOOK_ID);
				if (bookId != 0) {
					Set<Book> books;
					if (allocationConfiguration.getBooks() == null) {
						books = new HashSet<>();
					} else {
						books = allocationConfiguration.getBooks();
					}
					books.add(BookSQL.getBookById(bookId));
					allocationConfiguration.setBooks(books);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
		return allocationConfiguration;
	}

}