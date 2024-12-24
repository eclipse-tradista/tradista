package org.eclipse.tradista.core.transfer.persistence;

import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.book.model.Book;
import org.eclipse.tradista.core.book.service.BookBusinessDelegate;
import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.exception.TradistaTechnicalException;
import org.eclipse.tradista.core.common.persistence.db.TradistaDB;
import org.eclipse.tradista.core.configuration.service.ConfigurationBusinessDelegate;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.currency.service.CurrencyBusinessDelegate;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.product.service.ProductBusinessDelegate;
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

	private static ConfigurationBusinessDelegate configurationBusinessDelegate = new ConfigurationBusinessDelegate();

	public static Set<Transfer> getAllTransfers() {
		Set<Transfer> transfers = null;
		CurrencyBusinessDelegate currencyBusinessDelegate = new CurrencyBusinessDelegate();
		ProductBusinessDelegate productBusinessDelegate = new ProductBusinessDelegate();
		TradeBusinessDelegate tradeBusinessDelegate = new TradeBusinessDelegate();
		BookBusinessDelegate bookBusinessDelegate = new BookBusinessDelegate();

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetAllTransfers = con.prepareStatement("SELECT * FROM TRANSFER");
				ResultSet results = stmtGetAllTransfers.executeQuery()) {
			while (results.next()) {
				if (transfers == null) {
					transfers = new HashSet<>();
				}
				Transfer transfer;
				Transfer.Type type = Transfer.Type.valueOf(results.getString("type"));
				Book book = bookBusinessDelegate.getBookById(results.getLong("book_id"));
				Product product = null;
				long productId = results.getLong("product_id");
				if (productId > 0) {
					product = productBusinessDelegate.getProductById(productId);
				}
				Trade<?> trade = null;
				long tradeId = results.getLong("trade_id");
				if (tradeId > 0) {
					trade = tradeBusinessDelegate.getTradeById(tradeId, true);
				}
				if (type.equals(Transfer.Type.CASH)) {
					Currency currency = currencyBusinessDelegate.getCurrencyById(results.getLong("currency_id"));
					transfer = new CashTransfer(book, product, TransferPurpose.valueOf(results.getString("purpose")),
							results.getDate("settlement_date").toLocalDate(), trade, currency);
					((CashTransfer) transfer).setAmount(results.getBigDecimal("quantity"));
				} else {
					transfer = new ProductTransfer(book, product, TransferPurpose.valueOf(results.getString("purpose")),
							results.getDate("settlement_date").toLocalDate(), trade);
					((ProductTransfer) transfer).setQuantity(results.getBigDecimal("quantity"));
				}
				transfer.setId(results.getLong("id"));
				transfer.setStatus(Transfer.Status.valueOf(results.getString("status")));
				String direction = results.getString("direction");
				if (direction != null) {
					transfer.setDirection(Transfer.Direction.valueOf(direction));
				}
				transfer.setCreationDateTime(results.getTimestamp("creation_datetime").toLocalDateTime());
				Timestamp fixingTimestamp = results.getTimestamp("fixing_datetime");
				if (fixingTimestamp != null) {
					transfer.setFixingDateTime(fixingTimestamp.toLocalDateTime());
				}
				transfers.add(transfer);
			}
		} catch (SQLException | TradistaBusinessException e) {
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}
		return transfers;
	}

	public static long saveTransfer(Transfer transfer) {

		long transferId = 0;

		short scale = configurationBusinessDelegate.getScale();
		RoundingMode roundingMode = configurationBusinessDelegate.getRoundingMode();

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtSaveTransfer = (transfer.getId() != 0) ? con.prepareStatement(
						"UPDATE TRANSFER SET TYPE=?, STATUS=?, DIRECTION=?, QUANTITY=?, TRADE_ID=?, CREATION_DATETIME=?, FIXING_DATETIME=?, SETTLEMENT_DATE=?, PURPOSE=?, CURRENCY_ID=?, PRODUCT_ID=?, BOOK_ID=? WHERE ID = ?")
						: con.prepareStatement(
								"INSERT INTO TRANSFER(TYPE, STATUS, DIRECTION, QUANTITY, TRADE_ID, CREATION_DATETIME, FIXING_DATETIME, SETTLEMENT_DATE, PURPOSE,CURRENCY_ID,PRODUCT_ID, BOOK_ID) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
								Statement.RETURN_GENERATED_KEYS)) {
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
			Trade<?> trade = transfer.getTrade();
			if (trade != null) {
				stmtSaveTransfer.setLong(5, trade.getId());
			} else {
				stmtSaveTransfer.setNull(5, java.sql.Types.BIGINT);
			}
			stmtSaveTransfer.setTimestamp(6, Timestamp.valueOf(transfer.getCreationDateTime()));
			stmtSaveTransfer.setTimestamp(7, Timestamp.valueOf(transfer.getFixingDateTime()));
			stmtSaveTransfer.setDate(8, Date.valueOf(transfer.getSettlementDate()));
			stmtSaveTransfer.setString(9, transfer.getPurpose().name());
			if (transfer.getType().equals(Transfer.Type.CASH)) {
				stmtSaveTransfer.setLong(10, ((CashTransfer) transfer).getCurrency().getId());
				Product product = transfer.getProduct();
				if (product != null) {
					stmtSaveTransfer.setLong(11, product.getId());
				} else {
					stmtSaveTransfer.setNull(11, java.sql.Types.BIGINT);
				}
			} else {
				stmtSaveTransfer.setNull(10, java.sql.Types.BIGINT);
				stmtSaveTransfer.setLong(11, transfer.getProduct().getId());
			}
			stmtSaveTransfer.setLong(12, transfer.getBook().getId());
			stmtSaveTransfer.executeUpdate();

			if (transfer.getId() == 0) {
				try (ResultSet generatedKeys = stmtSaveTransfer.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						transferId = generatedKeys.getLong(1);
						transfer.setId(transferId);
					} else {
						throw new SQLException("Creating transfer failed, no generated key obtained.");
					}
				}
			} else {
				transferId = transfer.getId();
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle.getMessage());
		}

		return transferId;
	}

	public static void saveTransfers(List<Transfer> transfers) {
		short scale = configurationBusinessDelegate.getScale();
		RoundingMode roundingMode = configurationBusinessDelegate.getRoundingMode();
		if (transfers != null && !transfers.isEmpty()) {
			try (Connection con = TradistaDB.getConnection();
					PreparedStatement stmtUpdateTransfer = con.prepareStatement(
							"UPDATE TRANSFER SET TYPE=?, STATUS=?, DIRECTION=?, QUANTITY=?, TRADE_ID=?, CREATION_DATETIME=?, FIXING_DATETIME=?, SETTLEMENT_DATE=?, PURPOSE=?, CURRENCY_ID=?, PRODUCT_ID=?, BOOK_ID=? WHERE ID = ?");
					PreparedStatement stmtSaveTransfer = con.prepareStatement(
							"INSERT INTO TRANSFER(TYPE, STATUS, DIRECTION, QUANTITY, TRADE_ID, CREATION_DATETIME, FIXING_DATETIME, SETTLEMENT_DATE, PURPOSE,CURRENCY_ID,PRODUCT_ID, BOOK_ID) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
							Statement.RETURN_GENERATED_KEYS)) {
				for (Transfer transfer : transfers) {
					if (transfer.getId() != 0) {
						stmtUpdateTransfer.setLong(13, transfer.getId());
						stmtUpdateTransfer.setString(1, transfer.getType().name());
						stmtUpdateTransfer.setString(2, transfer.getStatus().name());
						Transfer.Direction direction = transfer.getDirection();
						if (direction != null) {
							stmtUpdateTransfer.setString(3, direction.name());
						} else {
							stmtUpdateTransfer.setNull(3, java.sql.Types.VARCHAR);
						}
						stmtUpdateTransfer.setBigDecimal(4, transfer.getType().equals(Transfer.Type.CASH)
								? (((CashTransfer) transfer).getAmount() == null ? null
										: ((CashTransfer) transfer).getAmount().setScale(scale, roundingMode))
								: (((ProductTransfer) transfer).getQuantity() == null ? null
										: ((ProductTransfer) transfer).getQuantity().setScale(scale, roundingMode)));
						Trade<?> trade = transfer.getTrade();
						if (trade != null) {
							stmtUpdateTransfer.setLong(5, trade.getId());
						} else {
							stmtUpdateTransfer.setNull(5, java.sql.Types.BIGINT);
						}
						stmtUpdateTransfer.setTimestamp(6, Timestamp.valueOf(transfer.getCreationDateTime()));
						stmtUpdateTransfer.setTimestamp(7, Timestamp.valueOf(transfer.getFixingDateTime()));
						stmtUpdateTransfer.setDate(8, Date.valueOf(transfer.getSettlementDate()));
						stmtUpdateTransfer.setString(9, transfer.getPurpose().name());
						if (transfer.getType().equals(Transfer.Type.CASH)) {
							stmtUpdateTransfer.setLong(10, ((CashTransfer) transfer).getCurrency().getId());
							Product product = transfer.getProduct();
							if (product != null) {
								stmtUpdateTransfer.setLong(11, product.getId());
							} else {
								stmtUpdateTransfer.setNull(11, java.sql.Types.BIGINT);
							}
						} else {
							stmtUpdateTransfer.setNull(10, java.sql.Types.BIGINT);
							stmtUpdateTransfer.setLong(11, transfer.getProduct().getId());
						}
						stmtUpdateTransfer.setLong(12, transfer.getBook().getId());
						stmtUpdateTransfer.addBatch();

					} else {
						stmtSaveTransfer.setString(1, transfer.getType().name());
						stmtSaveTransfer.setString(2, transfer.getStatus().name());
						Transfer.Direction direction = transfer.getDirection();
						if (direction != null) {
							stmtSaveTransfer.setString(3, direction.name());
						} else {
							stmtSaveTransfer.setNull(3, java.sql.Types.VARCHAR);
						}
						stmtSaveTransfer.setBigDecimal(4, transfer.getType().equals(Transfer.Type.CASH)
								? (((CashTransfer) transfer).getAmount() == null ? null
										: ((CashTransfer) transfer).getAmount().setScale(scale, roundingMode))
								: (((ProductTransfer) transfer).getQuantity() == null ? null
										: ((ProductTransfer) transfer).getQuantity().setScale(scale, roundingMode)));
						Trade<?> trade = transfer.getTrade();
						if (trade != null) {
							stmtSaveTransfer.setLong(5, trade.getId());
						} else {
							stmtSaveTransfer.setNull(5, java.sql.Types.BIGINT);
						}
						stmtSaveTransfer.setTimestamp(6, Timestamp.valueOf(transfer.getCreationDateTime()));
						LocalDateTime fixingDateTime = transfer.getFixingDateTime();
						if (fixingDateTime != null) {
							stmtSaveTransfer.setTimestamp(7, Timestamp.valueOf(fixingDateTime));
						} else {
							stmtSaveTransfer.setNull(7, Types.TIMESTAMP);
						}
						stmtSaveTransfer.setDate(8, Date.valueOf(transfer.getSettlementDate()));
						stmtSaveTransfer.setString(9, transfer.getPurpose().name());
						if (transfer.getType().equals(Transfer.Type.CASH)) {
							stmtSaveTransfer.setLong(10, ((CashTransfer) transfer).getCurrency().getId());
							Product product = transfer.getProduct();
							if (product != null) {
								stmtSaveTransfer.setLong(11, product.getId());
							} else {
								stmtSaveTransfer.setNull(11, java.sql.Types.BIGINT);
							}
						} else {
							stmtSaveTransfer.setNull(10, java.sql.Types.BIGINT);
							stmtSaveTransfer.setLong(11, transfer.getProduct().getId());
						}
						stmtSaveTransfer.setLong(12, transfer.getBook().getId());
						stmtSaveTransfer.addBatch();
					}
				}

				stmtSaveTransfer.executeBatch();
				stmtUpdateTransfer.executeBatch();

			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw new TradistaTechnicalException(sqle);
			}
		}
	}

	public static void deleteTransfer(long transferId) {
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtDeleteTransfer = con.prepareStatement("DELETE FROM TRANSFER WHERE ID = ?")) {
			stmtDeleteTransfer.setLong(1, transferId);
			stmtDeleteTransfer.executeUpdate();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new TradistaTechnicalException(sqle);
		}
	}

	public static Transfer getTransferById(long transferId) {

		Transfer transfer = null;
		CurrencyBusinessDelegate currencyBusinessDelegate = new CurrencyBusinessDelegate();
		ProductBusinessDelegate productBusinessDelegate = new ProductBusinessDelegate();
		TradeBusinessDelegate tradeBusinessDelegate = new TradeBusinessDelegate();
		BookBusinessDelegate bookBusinessDelegate = new BookBusinessDelegate();

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTransferById = con.prepareStatement("SELECT * FROM TRANSFER WHERE ID = ?")) {
			stmtGetTransferById.setLong(1, transferId);
			try (ResultSet results = stmtGetTransferById.executeQuery()) {
				while (results.next()) {
					Transfer.Type type = Transfer.Type.valueOf(results.getString("type"));
					Book book = bookBusinessDelegate.getBookById(results.getLong("book_id"));
					Product product = null;
					long productId = results.getLong("product_id");
					if (productId > 0) {
						product = productBusinessDelegate.getProductById(productId);
					}
					Trade<?> trade = null;
					long tradeId = results.getLong("trade_id");
					if (tradeId > 0) {
						trade = tradeBusinessDelegate.getTradeById(tradeId, true);
					}
					if (type.equals(Transfer.Type.CASH)) {
						Currency currency = currencyBusinessDelegate.getCurrencyById(results.getLong("currency_id"));
						transfer = new CashTransfer(book, product,
								TransferPurpose.valueOf(results.getString("purpose")),
								results.getDate("settlement_date").toLocalDate(), trade, currency);
						((CashTransfer) transfer).setAmount(results.getBigDecimal("quantity"));

					} else {
						transfer = new ProductTransfer(book, product,
								TransferPurpose.valueOf(results.getString("purpose")),
								results.getDate("settlement_date").toLocalDate(), trade);
						((ProductTransfer) transfer).setQuantity(results.getBigDecimal("quantity"));
					}
					transfer.setId(results.getLong("id"));
					transfer.setStatus(Transfer.Status.valueOf(results.getString("status")));
					String direction = results.getString("direction");
					if (direction != null) {
						transfer.setDirection(Transfer.Direction.valueOf(direction));
					}
					transfer.setCreationDateTime(results.getTimestamp("creation_datetime").toLocalDateTime());
					Timestamp fixingTimestamp = results.getTimestamp("fixing_datetime");
					if (fixingTimestamp != null) {
						transfer.setFixingDateTime(fixingTimestamp.toLocalDateTime());
					}
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			e.printStackTrace();
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
		String query = "SELECT * FROM TRANSFER WHERE TRADE_ID = ? AND PURPOSE = ?";

		if (!includeCancel) {
			query += " AND STATUS <> 'CANCELED'";
		}
		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTransferByTradeIdAndPurpose = con.prepareStatement(query)) {
			stmtGetTransferByTradeIdAndPurpose.setLong(1, tradeId);
			stmtGetTransferByTradeIdAndPurpose.setString(2, purpose.name());
			try (ResultSet results = stmtGetTransferByTradeIdAndPurpose.executeQuery()) {
				while (results.next()) {
					if (transfers == null) {
						transfers = new ArrayList<>();
					}
					Transfer transfer = null;
					Transfer.Type type = Transfer.Type.valueOf(results.getString("type"));
					Book book = bookBusinessDelegate.getBookById(results.getLong("book_id"));
					Product product = null;
					long productId = results.getLong("product_id");
					if (productId > 0) {
						product = productBusinessDelegate.getProductById(productId);
					}
					Trade<?> trade = null;
					if (tradeId > 0) {
						trade = tradeBusinessDelegate.getTradeById(tradeId, true);
					}
					if (type.equals(Transfer.Type.CASH)) {
						Currency currency = currencyBusinessDelegate.getCurrencyById(results.getLong("currency_id"));
						transfer = new CashTransfer(book, product,
								TransferPurpose.valueOf(results.getString("purpose")),
								results.getDate("settlement_date").toLocalDate(), trade, currency);
						((CashTransfer) transfer).setAmount(results.getBigDecimal("quantity"));
					} else {
						transfer = new ProductTransfer(book, product,
								TransferPurpose.valueOf(results.getString("purpose")),
								results.getDate("settlement_date").toLocalDate(), trade);
						((ProductTransfer) transfer).setQuantity(results.getBigDecimal("quantity"));
					}
					transfer.setId(results.getLong("id"));
					transfer.setStatus(Transfer.Status.valueOf(results.getString("status")));
					String direction = results.getString("direction");
					if (direction != null) {
						transfer.setDirection(Transfer.Direction.valueOf(direction));
					}
					transfer.setCreationDateTime(results.getTimestamp("creation_datetime").toLocalDateTime());
					Timestamp fixingTimestamp = results.getTimestamp("fixing_datetime");
					if (fixingTimestamp != null) {
						transfer.setFixingDateTime(fixingTimestamp.toLocalDateTime());
					}
					transfers.add(transfer);
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			e.printStackTrace();
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

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetTransfersByTradeId = con
						.prepareStatement("SELECT * FROM TRANSFER WHERE TRADE_ID = ?")) {
			stmtGetTransfersByTradeId.setLong(1, tradeId);
			try (ResultSet results = stmtGetTransfersByTradeId.executeQuery()) {
				while (results.next()) {
					if (transfers == null) {
						transfers = new ArrayList<>();
					}
					Transfer transfer = null;
					Transfer.Type type = Transfer.Type.valueOf(results.getString("type"));
					Book book = bookBusinessDelegate.getBookById(results.getLong("book_id"));
					Product product = null;
					long productId = results.getLong("product_id");
					if (productId > 0) {
						product = productBusinessDelegate.getProductById(productId);
					}
					Trade<?> trade = null;
					if (tradeId > 0) {
						trade = tradeBusinessDelegate.getTradeById(tradeId, true);
					}
					if (type.equals(Transfer.Type.CASH)) {
						Currency currency = currencyBusinessDelegate.getCurrencyById(results.getLong("currency_id"));
						transfer = new CashTransfer(book, product,
								TransferPurpose.valueOf(results.getString("purpose")),
								results.getDate("settlement_date").toLocalDate(), trade, currency);
						((CashTransfer) transfer).setAmount(results.getBigDecimal("quantity"));
					} else {
						transfer = new ProductTransfer(book, product,
								TransferPurpose.valueOf(results.getString("purpose")),
								results.getDate("settlement_date").toLocalDate(), trade);
						((ProductTransfer) transfer).setQuantity(results.getBigDecimal("quantity"));
					}
					transfer.setId(results.getLong("id"));
					transfer.setStatus(Transfer.Status.valueOf(results.getString("status")));
					String direction = results.getString("direction");
					if (direction != null) {
						transfer.setDirection(Transfer.Direction.valueOf(direction));
					}
					transfer.setCreationDateTime(results.getTimestamp("creation_datetime").toLocalDateTime());
					Timestamp fixingTimestamp = results.getTimestamp("fixing_datetime");
					if (fixingTimestamp != null) {
						transfer.setFixingDateTime(fixingTimestamp.toLocalDateTime());
					}
					transfers.add(transfer);
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			e.printStackTrace();
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

		try (Connection con = TradistaDB.getConnection();
				PreparedStatement stmtGetCashTransfersByProductIdAndStartDate = con.prepareStatement(
						"SELECT * FROM TRANSFER WHERE PRODUCT_ID = ? AND SETTLEMENT_DATE >= ? AND TYPE = 'CASH'")) {
			stmtGetCashTransfersByProductIdAndStartDate.setLong(1, productId);
			stmtGetCashTransfersByProductIdAndStartDate.setDate(2, Date.valueOf(startDate));
			try (ResultSet results = stmtGetCashTransfersByProductIdAndStartDate.executeQuery()) {
				while (results.next()) {
					if (transfers == null) {
						transfers = new ArrayList<>();
					}
					Product product = null;
					if (productId > 0) {
						product = productBusinessDelegate.getProductById(productId);
					}
					Book book = bookBusinessDelegate.getBookById(results.getLong("book_id"));
					Trade<?> trade = null;
					long tradeId = results.getLong("trade_id");
					if (tradeId > 0) {
						trade = tradeBusinessDelegate.getTradeById(tradeId, true);
					}
					Currency currency = currencyBusinessDelegate.getCurrencyById(results.getLong("currency_id"));
					CashTransfer transfer = null;
					transfer = new CashTransfer(book, product, TransferPurpose.valueOf(results.getString("purpose")),
							results.getDate("settlement_date").toLocalDate(), trade, currency);
					transfer.setAmount(results.getBigDecimal("quantity"));
					transfer.setId(results.getLong("id"));
					transfer.setStatus(Transfer.Status.valueOf(results.getString("status")));
					String direction = results.getString("direction");
					if (direction != null) {
						transfer.setDirection(Transfer.Direction.valueOf(direction));
					}
					transfer.setCreationDateTime(results.getTimestamp("creation_datetime").toLocalDateTime());
					Timestamp fixingTimestamp = results.getTimestamp("fixing_datetime");
					if (fixingTimestamp != null) {
						transfer.setFixingDateTime(fixingTimestamp.toLocalDateTime());
					}
					transfers.add(transfer);
				}
			}
		} catch (SQLException | TradistaBusinessException e) {
			e.printStackTrace();
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
		String query = "SELECT * FROM TRANSFER ";

		if (startFixingDate != null || endFixingDate != null || startSettlementDate != null || endSettlementDate != null
				|| startCreationDate != null || endCreationDate != null) {
			if (startFixingDate != null) {
				query += " WHERE FIXING_DATE >= '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(startFixingDate)
						+ "'";
			}
			if (endFixingDate != null) {
				if (query.contains("WHERE")) {
					query += " AND ";
				} else {
					query += "WHERE";
				}
				query += " FIXING_DATE <= '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(endFixingDate) + "'";
			}
			if (startSettlementDate != null) {
				if (query.contains("WHERE")) {
					query += " AND ";
				} else {
					query += "WHERE";
				}
				query += " SETTLEMENT_DATE >= '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(startSettlementDate)
						+ "'";
			}
			if (endSettlementDate != null) {
				if (query.contains("WHERE")) {
					query += " AND ";
				} else {
					query += "WHERE";
				}
				query += " SETTLEMENT_DATE <= '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(endSettlementDate)
						+ "'";
			}
			if (startCreationDate != null) {
				if (query.contains("WHERE")) {
					query += " AND ";
				} else {
					query += "WHERE";
				}
				query += " CREATION_DATE >= '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(startCreationDate)
						+ "'";
			}
			if (endCreationDate != null) {
				if (query.contains("WHERE")) {
					query += " AND ";
				} else {
					query += "WHERE";
				}
				query += " CREATION_DATE <= '" + DateTimeFormatter.ofPattern("MM/dd/yyyy").format(endCreationDate)
						+ "'";
			}
		}

		if (type != null) {
			if (query.contains("WHERE")) {
				query += " AND ";
			} else {
				query += "WHERE";
			}
			query += " TYPE = '" + type.name() + "'";
		}

		if (status != null) {
			if (query.contains("WHERE")) {
				query += " AND ";
			} else {
				query += "WHERE";
			}
			query += " STATUS = '" + status.name() + "'";
		}

		if (direction != null) {
			if (query.contains("WHERE")) {
				query += " AND ";
			} else {
				query += "WHERE";
			}
			query += " DIRECTION = '" + direction.name() + "'";
		}

		if (purpose != null) {
			if (query.contains("WHERE")) {
				query += " AND ";
			} else {
				query += "WHERE";
			}
			query += " PURPOSE = '" + purpose.name() + "'";
		}

		if (tradeId > 0) {
			if (query.contains("WHERE")) {
				query += " AND ";
			} else {
				query += "WHERE";
			}
			query += " TRADE_ID = " + tradeId;
		}

		if (productId > 0) {
			if (query.contains("WHERE")) {
				query += " AND ";
			} else {
				query += "WHERE";
			}
			query += " PRODUCT_ID = " + productId;
		}

		if (bookId > 0) {
			if (query.contains("WHERE")) {
				query += " AND ";
			} else {
				query += "WHERE";
			}
			query += " BOOK_ID = " + bookId;
		}

		if (currencyId > 0) {
			if (query.contains("WHERE")) {
				query += " AND ";
			} else {
				query += "WHERE";
			}
			query += " CURRENCY_ID = " + currencyId;
		}

		try (Connection con = TradistaDB.getConnection();
				Statement stmt = con.createStatement();
				ResultSet results = stmt.executeQuery(query)) {
			while (results.next()) {
				if (transfers == null) {
					transfers = new ArrayList<>();
				}
				Transfer transfer = null;
				Transfer.Type transferType = Transfer.Type.valueOf(results.getString("type"));
				Product product = null;
				long transferProductId = results.getLong("product_id");
				if (transferProductId > 0) {
					product = productBusinessDelegate.getProductById(transferProductId);
				}
				Book book = bookBusinessDelegate.getBookById(results.getLong("book_id"));
				Trade<?> trade = null;
				long transferTradeId = results.getLong("trade_id");
				if (transferTradeId > 0) {
					trade = tradeBusinessDelegate.getTradeById(transferTradeId, true);
				}
				if (transferType.equals(Transfer.Type.CASH)) {
					Currency currency = currencyBusinessDelegate.getCurrencyById(results.getLong("currency_id"));
					transfer = new CashTransfer(book, product, TransferPurpose.valueOf(results.getString("purpose")),
							results.getDate("settlement_date").toLocalDate(), trade, currency);
					((CashTransfer) transfer).setAmount(results.getBigDecimal("quantity"));
				} else {
					if (product != null) {
						transfer = new ProductTransfer(book, product,
								TransferPurpose.valueOf(results.getString("purpose")),
								results.getDate("settlement_date").toLocalDate(), trade);
						((ProductTransfer) transfer).setQuantity(results.getBigDecimal("quantity"));
					} else {
						transfer = new ProductTransfer(book, TransferPurpose.valueOf(results.getString("purpose")),
								results.getDate("settlement_date").toLocalDate(), trade);
						((ProductTransfer) transfer).setQuantity(results.getBigDecimal("quantity"));
					}
				}
				transfer.setId(results.getLong("id"));
				transfer.setStatus(Transfer.Status.valueOf(results.getString("status")));
				String transferDirection = results.getString("direction");
				if (transferDirection != null) {
					transfer.setDirection(Transfer.Direction.valueOf(transferDirection));
				}
				transfer.setCreationDateTime(results.getTimestamp("creation_datetime").toLocalDateTime());
				Timestamp fixingTimestamp = results.getTimestamp("fixing_datetime");
				if (fixingTimestamp != null) {
					transfer.setFixingDateTime(fixingTimestamp.toLocalDateTime());
				}
				transfers.add(transfer);
			}
		} catch (SQLException | TradistaBusinessException e) {
			e.printStackTrace();
			throw new TradistaTechnicalException(e);
		}
		return transfers;
	}

}