package org.eclipse.tradista.core.book.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.DESCRIPTION;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.NAME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PROCESSING_ORG_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;
import org.eclipse.tradista.core.legalentity.persistence.LegalEntitySQL;

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

public class BookSQL {

	private static final Field NAME_FIELD = new Field(NAME);
	private static final Field DESCRIPTION_FIELD = new Field(DESCRIPTION);
	private static final Field PROCESSING_ORG_ID_FIELD = new Field(PROCESSING_ORG_ID);
	private static final Field ID_FIELD = new Field(ID);

	private static final Field[] FIELDS = { NAME_FIELD, DESCRIPTION_FIELD, PROCESSING_ORG_ID_FIELD, ID_FIELD };
	private static final Field[] FIELDS_FOR_INSERT = { NAME_FIELD, DESCRIPTION_FIELD, PROCESSING_ORG_ID_FIELD };

	public static final Table TABLE = new Table("BOOK", FIELDS);

	public static Book getBookById(long id) {
		Book book = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetBookById = con.prepareStatement(sql.toString())) {
			stmtGetBookById.setLong(1, id);
			try (ResultSet results = stmtGetBookById.executeQuery()) {
				while (results.next()) {
					book = buildBook(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return book;
	}

	public static boolean bookExists(String name, long poId) {
		boolean exists = false;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(ID_FIELD, TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, PROCESSING_ORG_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtBookExists = con.prepareStatement(sql.toString())) {
			stmtBookExists.setString(1, name);
			stmtBookExists.setLong(2, poId);
			try (ResultSet results = stmtBookExists.executeQuery()) {
				if (results.next()) {
					exists = true;
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return exists;
	}

	public static Set<Book> getAllBooks() {
		Set<Book> books = null;
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllBooks = con.prepareStatement(TradistaDBUtil.buildSelectQuery(TABLE));
				ResultSet results = stmtGetAllBooks.executeQuery()) {
			while (results.next()) {
				Book book = buildBook(results);
				if (books == null) {
					books = new HashSet<>();
				}
				books.add(book);
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return books;
	}

	public static long saveBook(Book book) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveBook = (book.getId() == 0)
						? TradistaDBUtil.buildInsertPreparedStatement(con, TABLE, FIELDS_FOR_INSERT)
						: TradistaDBUtil.buildUpdatePreparedStatement(con, ID_FIELD, TABLE, FIELDS_FOR_INSERT)) {
			if (book.getId() != 0) {
				stmtSaveBook.setLong(4, book.getId());
			}
			stmtSaveBook.setString(1, book.getName());
			stmtSaveBook.setString(2, book.getDescription());
			stmtSaveBook.setLong(3, book.getProcessingOrg().getId());
			stmtSaveBook.executeUpdate();
			if (book.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveBook.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						book.setId(generatedKeys.getLong(1));
					} else {
						throw new SQLException("Creating book failed, no generated key obtained.");
					}
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return book.getId();
	}

	public static Book getBookByName(String name) {
		Book book = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetBookByName = con.prepareStatement(sql.toString())) {
			stmtGetBookByName.setString(1, name);
			try (ResultSet results = stmtGetBookByName.executeQuery()) {
				while (results.next()) {
					book = buildBook(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return book;
	}

	public static Set<Book> getBooksByPoId(long poId) {
		Set<Book> books = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, PROCESSING_ORG_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(sql.toString())) {
			stmt.setLong(1, poId);
			try (ResultSet results = stmt.executeQuery()) {
				while (results.next()) {
					Book book = buildBook(results);
					if (books == null) {
						books = new HashSet<>();
					}
					books.add(book);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return books;
	}

	private static Book buildBook(ResultSet results) throws SQLException {
		LegalEntity processingOrg = LegalEntitySQL
				.getLegalEntityById(results.getLong(PROCESSING_ORG_ID_FIELD.getName()));
		Book book = new Book(results.getString(NAME_FIELD.getName()), processingOrg);
		book.setId(results.getLong(ID_FIELD.getName()));
		book.setDescription(results.getString(DESCRIPTION_FIELD.getName()));
		return book;
	}

	public static Book getBookByNameAndPoId(String name, long poId) {
		Book book = null;
		StringBuilder sql = new StringBuilder(TradistaDBUtil.buildSelectQuery(TABLE));
		TradistaDBUtil.addParameterizedFilter(sql, NAME_FIELD);
		TradistaDBUtil.addParameterizedFilter(sql, PROCESSING_ORG_ID_FIELD);
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(sql.toString())) {
			stmt.setString(1, name);
			stmt.setLong(2, poId);
			try (ResultSet results = stmt.executeQuery()) {
				if (results.next()) {
					book = buildBook(results);
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
		return book;
	}

}