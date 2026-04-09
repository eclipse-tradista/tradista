package org.eclipse.tradista.core.transfer.persistence;

import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.BOOK_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.CREATION_DATETIME;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.CURRENCY_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.PRODUCT_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.QUANTITY;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.STATUS;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.TRADE_ID;
import static org.eclipse.tradista.core.common.persistence.util.TradistaDBConstants.TYPE;

import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.common.persistence.util.Field;
import org.eclipse.tradista.core.common.persistence.util.Table;
import org.eclipse.tradista.core.common.persistence.util.TradistaDBUtil;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.service.CurrencyBusinessDelegate;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.product.service.ProductBusinessDelegate;
import org.eclipse.tradista.core.status.constants.StatusConstants;
import org.eclipse.tradista.core.trade.model.Trade;
import org.eclipse.tradista.core.trade.service.TradeBusinessDelegate;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.ProductTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.Transfer.Direction;
import org.eclipse.tradista.core.transfer.model.Transfer.Status;
import org.eclipse.tradista.core.transfer.model.Transfer.Type;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
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

public class TransferSQL {

	public static final Field ID_FIELD = new Field(ID);

	private static final Field TYPE_FIELD = new Field(TYPE);

	private static final Field STATUS_FIELD = new Field(STATUS);

	private static final Field DIRECTION_FIELD = new Field("DIRECTION");

	private static final Field QUANTITY_FIELD = new Field(QUANTITY);

	private static final Field PURPOSE_FIELD = new Field("PURPOSE");

	private static final Field TRADE_ID_FIELD = new Field(TRADE_ID);

	private static final Field BOOK_ID_FIELD = new Field(BOOK_ID);

	private static final Field CREATION_DATETIME_FIELD = new Field(CREATION_DATETIME);

	private static final Field FIXING_DATETIME_FIELD = new Field("FIXING_DATETIME");

	private static final Field SETTLEMENT_DATE_FIELD = new Field("SETTLEMENT_DATE");

	private static final Field CURRENCY_ID_FIELD = new Field(CURRENCY_ID);

	private static final Field PRODUCT_ID_FIELD = new Field(PRODUCT_ID);

	private static final Field[] TRANSFER_FIELDS = new Field[] { ID_FIELD, TYPE_FIELD, STATUS_FIELD, DIRECTION_FIELD,
			QUANTITY_FIELD, PURPOSE_FIELD, TRADE_ID_FIELD, BOOK_ID_FIELD, CREATION_DATETIME_FIELD,
			FIXING_DATETIME_FIELD, SETTLEMENT_DATE_FIELD, CURRENCY_ID_FIELD, PRODUCT_ID_FIELD };

	private static final Field[] TRANSFER_FIELDS_FOR_INSERT = new Field[] { TYPE_FIELD, STATUS_FIELD, DIRECTION_FIELD,
			QUANTITY_FIELD, PURPOSE_FIELD, TRADE_ID_FIELD, BOOK_ID_FIELD, CREATION_DATETIME_FIELD,
			FIXING_DATETIME_FIELD, SETTLEMENT_DATE_FIELD, CURRENCY_ID_FIELD, PRODUCT_ID_FIELD };

	private static final Field[] TRANSFER_FIELDS_FOR_UPDATE = new Field[] { TYPE_FIELD, STATUS_FIELD, DIRECTION_FIELD,
			QUANTITY_FIELD, PURPOSE_FIELD, TRADE_ID_FIELD, BOOK_ID_FIELD, CREATION_DATETIME_FIELD,
			FIXING_DATETIME_FIELD, SETTLEMENT_DATE_FIELD, CURRENCY_ID_FIELD, PRODUCT_ID_FIELD };

	public static final Table TRANSFER_TABLE = new Table("TRANSFER", ID, TRANSFER_FIELDS);

	private static ConfigurationBusinessDelegate configurationBusinessDelegate = new ConfigurationBusinessDelegate();

	public static PreparedStatement getInsertStatement(Connection con) {
		return TradistaDBUtil.buildInsertPreparedStatement(con, TRANSFER_TABLE, TRANSFER_FIELDS_FOR_INSERT);
	}

	public static PreparedStatement getUpdateStatement(Connection con) {
		return TradistaDBUtil.buildUpdatePreparedStatement(con, ID_FIELD, TRANSFER_TABLE, TRANSFER_FIELDS_FOR_UPDATE);
	}

	public static Set<Transfer> getAllTransfers() {
		Set<Transfer> transfers = null;
		CurrencyBusinessDelegate currencyBusinessDelegate = new CurrencyBusinessDelegate();
		ProductBusinessDelegate productBusinessDelegate = new ProductBusinessDelegate();
		TradeBusinessDelegate tradeBusinessDelegate = new TradeBusinessDelegate();
		BookBusinessDelegate bookBusinessDelegate = new BookBusinessDelegate();

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllTransfers = con
						.prepareStatement(TradistaDBUtil.buildSelectQuery(TRANSFER_TABLE));
				ResultSet results = stmtGetAllTransfers.executeQuery()) {
			while (results.next()) {
				if (transfers == null) {
					transfers = new HashSet<>();
				}
				Transfer transfer = createTransfer(currencyBusinessDelegate, productBusinessDelegate,
						tradeBusinessDelegate, bookBusinessDelegate, results);
				transfers.add(transfer);
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return transfers;
	}

	private static Transfer createTransfer(CurrencyBusinessDelegate currencyBusinessDelegate,
			ProductBusinessDelegate productBusinessDelegate, TradeBusinessDelegate tradeBusinessDelegate,
			BookBusinessDelegate bookBusinessDelegate, ResultSet results)
			throws SQLException, TradistaBusinessException {
		Transfer transfer;
		Transfer.Type type = Transfer.Type.valueOf(results.getString(TYPE_FIELD.getName()));
		Book book = bookBusinessDelegate.getBookById(results.getLong(BOOK_ID_FIELD.getName()));
		Product product = null;
		long productId = results.getLong(PRODUCT_ID_FIELD.getName());
		if (productId > 0) {
			product = productBusinessDelegate.getProductById(productId);
		}
		Trade<?> trade = null;
		long tradeId = results.getLong(TRADE_ID_FIELD.getName());
		if (tradeId > 0) {
			trade = tradeBusinessDelegate.getTradeById(tradeId, true);
		}
		if (type.equals(Transfer.Type.CASH)) {
			Currency currency = currencyBusinessDelegate.getCurrencyById(results.getLong(CURRENCY_ID_FIELD.getName()));
			transfer = new CashTransfer(book, product,
					TransferPurpose.valueOf(results.getString(PURPOSE_FIELD.getName())),
					results.getDate(SETTLEMENT_DATE_FIELD.getName()).toLocalDate(), trade, currency);
			((CashTransfer) transfer).setAmount(results.getBigDecimal(QUANTITY_FIELD.getName()));
		} else {
			transfer = new ProductTransfer(book, product,
					TransferPurpose.valueOf(results.getString(PURPOSE_FIELD.getName())),
					results.getDate(SETTLEMENT_DATE_FIELD.getName()).toLocalDate(), trade);
			((ProductTransfer) transfer).setQuantity(results.getBigDecimal(QUANTITY_FIELD.getName()));
		}
		transfer.setId(results.getLong(ID_FIELD.getName()));
		transfer.setStatus(Transfer.Status.valueOf(results.getString(STATUS_FIELD.getName())));
		String direction = results.getString(DIRECTION_FIELD.getName());
		if (direction != null) {
			transfer.setDirection(Transfer.Direction.valueOf(direction));
		}
		transfer.setCreationDateTime(results.getTimestamp(CREATION_DATETIME_FIELD.getName()).toLocalDateTime());
		Timestamp fixingTimestamp = results.getTimestamp(FIXING_DATETIME_FIELD.getName());
		if (fixingTimestamp != null) {
			transfer.setFixingDateTime(fixingTimestamp.toLocalDateTime());
		}
		return transfer;
	}

	public static long saveTransfer(Transfer transfer) {

		short scale = configurationBusinessDelegate.getScale();
		RoundingMode roundingMode = configurationBusinessDelegate.getRoundingMode();

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTransfer = (transfer.getId() != 0) ? getUpdateStatement(con)
						: getInsertStatement(con)) {

			setPreparedStatementFields(transfer, scale, roundingMode, stmtSaveTransfer);
			stmtSaveTransfer.executeUpdate();

			if (transfer.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveTransfer.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						transfer.setId(generatedKeys.getLong(1));
					} else {
						throw new SQLException("Creating transfer failed, no generated key obtained.");
					}
				}
			}
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}

		return transfer.getId();
	}

	private static void setPreparedStatementFields(Transfer transfer, short scale, RoundingMode roundingMode,
			PreparedStatement stmtSaveTransfer) throws SQLException {
		if (transfer.getId() != 0) {
			stmtSaveTransfer.setLong(13, transfer.getId());
		}
		stmtSaveTransfer.setString(1, transfer.getType().name());
		stmtSaveTransfer.setString(2, transfer.getStatus().name());
		Transfer.Direction direction = transfer.getDirection();
		if (direction != null) {
			stmtSaveTransfer.setString(3, direction.name());
		} else {
			stmtSaveTransfer.setNull(3, java.sql.Types.VARCHAR);
		}
		stmtSaveTransfer.setBigDecimal(4,
				transfer.getType().equals(Transfer.Type.CASH)
						? (((CashTransfer) transfer).getAmount() == null ? null
								: ((CashTransfer) transfer).getAmount().setScale(scale, roundingMode))
						: (((ProductTransfer) transfer).getQuantity() == null ? null
								: ((ProductTransfer) transfer).getQuantity().setScale(scale, roundingMode)));
		stmtSaveTransfer.setString(5, transfer.getPurpose().name());
		Trade<?> trade = transfer.getTrade();
		if (trade != null) {
			stmtSaveTransfer.setLong(6, trade.getId());
		} else {
			stmtSaveTransfer.setNull(6, java.sql.Types.BIGINT);
		}
		stmtSaveTransfer.setLong(7, transfer.getBook().getId());
		stmtSaveTransfer.setTimestamp(8, Timestamp.valueOf(transfer.getCreationDateTime()));
		LocalDateTime fixingDateTime = transfer.getFixingDateTime();
		if (fixingDateTime != null) {
			stmtSaveTransfer.setTimestamp(9, Timestamp.valueOf(fixingDateTime));
		} else {
			stmtSaveTransfer.setNull(9, java.sql.Types.TIMESTAMP);
		}
		stmtSaveTransfer.setDate(10, Date.valueOf(transfer.getSettlementDate()));

		if (transfer.getType().equals(Transfer.Type.CASH)) {
			stmtSaveTransfer.setLong(11, ((CashTransfer) transfer).getCurrency().getId());
			Product product = transfer.getProduct();
			if (product != null) {
				stmtSaveTransfer.setLong(12, product.getId());
			} else {
				stmtSaveTransfer.setNull(12, java.sql.Types.BIGINT);
			}
		} else {
			stmtSaveTransfer.setNull(11, java.sql.Types.BIGINT);
			stmtSaveTransfer.setLong(12, transfer.getProduct().getId());
		}
	}

	public static void saveTransfers(List<Transfer> transfers) {
		short scale = configurationBusinessDelegate.getScale();
		RoundingMode roundingMode = configurationBusinessDelegate.getRoundingMode();
		if (transfers != null && !transfers.isEmpty()) {
			try (Connection con = TradistaDB.getConnection();
					PreparedStatement stmtUpdateTransfer = getUpdateStatement(con);
					PreparedStatement stmtSaveTransfer = getInsertStatement(con)) {
				for (Transfer transfer : transfers) {
					if (transfer.getId() != 0) {
						setPreparedStatementFields(transfer, scale, roundingMode, stmtUpdateTransfer);
						stmtUpdateTransfer.addBatch();

					} else {
						setPreparedStatementFields(transfer, scale, roundingMode, stmtSaveTransfer);
						stmtSaveTransfer.addBatch();
					}
				}

				stmtSaveTransfer.executeBatch();
				stmtUpdateTransfer.executeBatch();

			} catch (SQLException sqle) {
				throw new TradistaTechnicalException(sqle);
			}
		}
	}

	public static void deleteTransfer(long transferId) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteTransfer = TradistaDBUtil.buildDeletePreparedStatement(con, TRANSFER_TABLE,
						ID_FIELD)) {
			stmtDeleteTransfer.setLong(1, transferId);
			stmtDeleteTransfer.executeUpdate();
		} catch (SQLException sqle) {
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static Transfer getTransferById(long transferId) {

		Transfer transfer = null;
		CurrencyBusinessDelegate currencyBusinessDelegate = new CurrencyBusinessDelegate();
		ProductBusinessDelegate productBusinessDelegate = new ProductBusinessDelegate();
		TradeBusinessDelegate tradeBusinessDelegate = new TradeBusinessDelegate();
		BookBusinessDelegate bookBusinessDelegate = new BookBusinessDelegate();

		StringBuilder sqlQuery = new StringBuilder(TradistaDBUtil.buildSelectQuery(TRANSFER_TABLE));
		TradistaDBUtil.addParameterizedFilter(sqlQuery, ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTransferById = con.prepareStatement(sqlQuery.toString())) {
			stmtGetTransferById.setLong(1, transferId);
			try (ResultSet results = stmtGetTransferById.executeQuery()) {
				while (results.next()) {
					transfer = createTransfer(currencyBusinessDelegate, productBusinessDelegate, tradeBusinessDelegate,
							bookBusinessDelegate, results);
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return transfer;
	}

	public static List<Transfer> getTransfersByTradeIdAndPurpose(long tradeId, TransferPurpose purpose,
			boolean includeCancel) {
		List<Transfer> transfers = null;
		CurrencyBusinessDelegate currencyBusinessDelegate = new CurrencyBusinessDelegate();
		ProductBusinessDelegate productBusinessDelegate = new ProductBusinessDelegate();
		TradeBusinessDelegate tradeBusinessDelegate = new TradeBusinessDelegate();
		BookBusinessDelegate bookBusinessDelegate = new BookBusinessDelegate();

		StringBuilder sqlQuery = new StringBuilder(TradistaDBUtil.buildSelectQuery(TRANSFER_TABLE));
		TradistaDBUtil.addParameterizedFilter(sqlQuery, TRADE_ID_FIELD);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, PURPOSE_FIELD);

		if (!includeCancel) {
			TradistaDBUtil.addFilter(true, sqlQuery, STATUS_FIELD, StatusConstants.CANCELED);
		}
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTransferByTradeIdAndPurpose = con.prepareStatement(sqlQuery.toString())) {
			stmtGetTransferByTradeIdAndPurpose.setLong(1, tradeId);
			stmtGetTransferByTradeIdAndPurpose.setString(2, purpose.name());
			try (ResultSet results = stmtGetTransferByTradeIdAndPurpose.executeQuery()) {
				while (results.next()) {
					if (transfers == null) {
						transfers = new ArrayList<>();
					}
					Transfer transfer = createTransfer(currencyBusinessDelegate, productBusinessDelegate,
							tradeBusinessDelegate, bookBusinessDelegate, results);
					transfers.add(transfer);
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return transfers;
	}

	public static List<Transfer> getTransfersByTradeId(long tradeId) {
		List<Transfer> transfers = null;
		CurrencyBusinessDelegate currencyBusinessDelegate = new CurrencyBusinessDelegate();
		ProductBusinessDelegate productBusinessDelegate = new ProductBusinessDelegate();
		TradeBusinessDelegate tradeBusinessDelegate = new TradeBusinessDelegate();
		BookBusinessDelegate bookBusinessDelegate = new BookBusinessDelegate();

		StringBuilder sqlQuery = new StringBuilder(TradistaDBUtil.buildSelectQuery(TRANSFER_TABLE));
		TradistaDBUtil.addParameterizedFilter(sqlQuery, TRADE_ID_FIELD);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTransfersByTradeId = con.prepareStatement(sqlQuery.toString())) {
			stmtGetTransfersByTradeId.setLong(1, tradeId);
			try (ResultSet results = stmtGetTransfersByTradeId.executeQuery()) {
				while (results.next()) {
					if (transfers == null) {
						transfers = new ArrayList<>();
					}
					Transfer transfer = createTransfer(currencyBusinessDelegate, productBusinessDelegate,
							tradeBusinessDelegate, bookBusinessDelegate, results);
					transfers.add(transfer);
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return transfers;
	}

	public static List<CashTransfer> getCashTransfersByProductIdAndStartDate(long productId, LocalDate startDate) {
		List<CashTransfer> transfers = null;
		CurrencyBusinessDelegate currencyBusinessDelegate = new CurrencyBusinessDelegate();
		TradeBusinessDelegate tradeBusinessDelegate = new TradeBusinessDelegate();
		ProductBusinessDelegate productBusinessDelegate = new ProductBusinessDelegate();
		BookBusinessDelegate bookBusinessDelegate = new BookBusinessDelegate();

		StringBuilder sqlQuery = new StringBuilder(TradistaDBUtil.buildSelectQuery(TRANSFER_TABLE));
		TradistaDBUtil.addParameterizedFilter(sqlQuery, PRODUCT_ID_FIELD);
		TradistaDBUtil.addParameterizedFilter(sqlQuery, SETTLEMENT_DATE_FIELD, true);
		TradistaDBUtil.addFilter(sqlQuery, TYPE_FIELD, Transfer.Type.CASH);

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetCashTransfersByProductIdAndStartDate = con
						.prepareStatement(sqlQuery.toString())) {
			stmtGetCashTransfersByProductIdAndStartDate.setLong(1, productId);
			stmtGetCashTransfersByProductIdAndStartDate.setDate(2, Date.valueOf(startDate));
			try (ResultSet results = stmtGetCashTransfersByProductIdAndStartDate.executeQuery()) {
				while (results.next()) {
					if (transfers == null) {
						transfers = new ArrayList<>();
					}
					Transfer transfer = createTransfer(currencyBusinessDelegate, productBusinessDelegate,
							tradeBusinessDelegate, bookBusinessDelegate, results);
					transfers.add((CashTransfer) transfer);
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return transfers;
	}

	public static List<Transfer> getTransfers(Type type, Status status, Direction direction, TransferPurpose purpose,
			long tradeId, long productId, long bookId, long currencyId, LocalDate startFixingDate,
			LocalDate endFixingDate, LocalDate startSettlementDate, LocalDate endSettlementDate,
			LocalDate startCreationDate, LocalDate endCreationDate) {
		List<Transfer> transfers = null;
		CurrencyBusinessDelegate currencyBusinessDelegate = new CurrencyBusinessDelegate();
		ProductBusinessDelegate productBusinessDelegate = new ProductBusinessDelegate();
		TradeBusinessDelegate tradeBusinessDelegate = new TradeBusinessDelegate();
		BookBusinessDelegate bookBusinessDelegate = new BookBusinessDelegate();
		StringBuilder sqlQuery = new StringBuilder(TradistaDBUtil.buildSelectQuery(TRANSFER_TABLE));

		if (startFixingDate != null) {
			TradistaDBUtil.addFilter(sqlQuery, FIXING_DATETIME_FIELD, startFixingDate, true);
		}
		if (endFixingDate != null) {
			TradistaDBUtil.addFilter(sqlQuery, FIXING_DATETIME_FIELD, endFixingDate, false);
		}

		if (startSettlementDate != null) {
			TradistaDBUtil.addFilter(sqlQuery, SETTLEMENT_DATE_FIELD, startSettlementDate, true);
		}
		if (endSettlementDate != null) {
			TradistaDBUtil.addFilter(sqlQuery, SETTLEMENT_DATE_FIELD, endSettlementDate, false);
		}

		if (startCreationDate != null) {
			TradistaDBUtil.addFilter(sqlQuery, CREATION_DATETIME_FIELD, startCreationDate, true);
		}
		if (endCreationDate != null) {
			TradistaDBUtil.addFilter(sqlQuery, CREATION_DATETIME_FIELD, endCreationDate, false);
		}

		if (type != null) {
			TradistaDBUtil.addFilter(sqlQuery, TYPE_FIELD, type);
		}

		if (status != null) {
			TradistaDBUtil.addFilter(sqlQuery, STATUS_FIELD, status);
		}

		if (direction != null) {
			TradistaDBUtil.addFilter(sqlQuery, DIRECTION_FIELD, direction);
		}

		if (purpose != null) {
			TradistaDBUtil.addFilter(sqlQuery, PURPOSE_FIELD, purpose);
		}

		if (tradeId > 0) {
			TradistaDBUtil.addFilter(sqlQuery, TRADE_ID_FIELD, tradeId);
		}

		if (productId > 0) {
			TradistaDBUtil.addFilter(sqlQuery, PRODUCT_ID_FIELD, productId);
		}

		if (bookId > 0) {
			TradistaDBUtil.addFilter(sqlQuery, BOOK_ID_FIELD, bookId);
		}

		if (currencyId > 0) {
			TradistaDBUtil.addFilter(sqlQuery, CURRENCY_ID_FIELD, currencyId);
		}

		try (Connection con = TradistaDB.getConnection();
				Statement stmt = con.createStatement();
				ResultSet results = stmt.executeQuery(sqlQuery.toString())) {
			while (results.next()) {
				if (transfers == null) {
					transfers = new ArrayList<>();
				}
				Transfer transfer = createTransfer(currencyBusinessDelegate, productBusinessDelegate,
						tradeBusinessDelegate, bookBusinessDelegate, results);
				transfers.add(transfer);
			}
		} catch (SQLException | TradistaBusinessException e) {
			throw new TradistaTechnicalException(e);
		}
		return transfers;
	}

}