package org.eclipse.tradista.core.transfer.service;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.TradistaUtil;
import org.eclipse.tradista.core.marketdata.service.QuoteBusinessDelegate;
import org.eclipse.tradista.core.trade.messaging.TradeEvent;
import org.eclipse.tradista.core.transfer.messaging.CashTransferEvent;
import org.eclipse.tradista.core.transfer.messaging.ProductTransferEvent;
import org.eclipse.tradista.core.transfer.messaging.TransferEvent;
import org.eclipse.tradista.core.transfer.model.CashTransfer;
import org.eclipse.tradista.core.transfer.model.ProductTransfer;
import org.eclipse.tradista.core.transfer.model.Transfer;
import org.eclipse.tradista.core.transfer.model.Transfer.Direction;
import org.eclipse.tradista.core.transfer.model.Transfer.Status;
import org.eclipse.tradista.core.transfer.model.Transfer.Type;
import org.eclipse.tradista.core.transfer.model.TransferManager;
import org.eclipse.tradista.core.transfer.model.TransferPurpose;
import org.eclipse.tradista.core.transfer.persistence.TransferSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSContext;

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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class TransferServiceBean implements TransferService {

	private ConnectionFactory factory;

	private JMSContext context;

	private Destination cashInventoryDestination;

	private Destination productInventoryDestination;

	@PostConstruct
	private void initialize() {
		context = factory.createContext();
	}

	@Override
	public Set<Transfer> getAllTransfers() {
		return TransferSQL.getAllTransfers();
	}

	@Interceptors(TransferPostFilteringInterceptor.class)
	@Override
	public Transfer getTransferById(long id) {
		return TransferSQL.getTransferById(id);
	}

	@Override
	public long saveTransfer(Transfer transfer) {
		if (transfer.getType().equals(Transfer.Type.CASH)) {
			return saveCashTransfer((CashTransfer) transfer);
		} else {
			return saveProductTransfer((ProductTransfer) transfer);
		}
	}

	@Override
	public void saveTransfers(List<Transfer> transfers) {
		if (transfers != null && !transfers.isEmpty()) {
			Queue<TransferEvent<?>> events = new LinkedList<>();
			for (Transfer transfer : transfers) {
				if (transfer.getType().equals(Transfer.Type.CASH)) {
					CashTransferEvent event = new CashTransferEvent();
					if (transfer.getId() != 0) {
						CashTransfer oldTransfer = (CashTransfer) TransferSQL.getTransferById(transfer.getId());
						if (oldTransfer.getStatus().equals(Transfer.Status.KNOWN)) {
							event.setOldTransfer(oldTransfer);
						}
					}
					events.add(event);
				} else {
					ProductTransferEvent event = new ProductTransferEvent();
					if (transfer.getId() != 0) {
						ProductTransfer oldTransfer = (ProductTransfer) TransferSQL.getTransferById(transfer.getId());
						if (oldTransfer.getStatus().equals(Transfer.Status.KNOWN)) {
							event.setOldTransfer(oldTransfer);
						}
					}
					events.add(event);
				}
			}

			TransferSQL.saveTransfers(transfers);

			for (Transfer transfer : transfers) {
				if (transfer.getType().equals(Transfer.Type.CASH)) {
					CashTransferEvent event = (CashTransferEvent) events.remove();

					if (transfer.getStatus().equals(Transfer.Status.KNOWN)) {
						event.setTransfer((CashTransfer) transfer);
					}

					if (event.getOldTransfer() != null || event.getTransfer() != null) {
						context.createProducer().send(cashInventoryDestination, event);
					}
				} else {
					ProductTransferEvent event = (ProductTransferEvent) events.remove();

					if (transfer.getStatus().equals(Transfer.Status.KNOWN)) {
						event.setTransfer((ProductTransfer) transfer);
					}

					if (event.getOldTransfer() != null || event.getTransfer() != null) {
						context.createProducer().send(productInventoryDestination, event);
					}
				}
			}

		}
	}

	private long saveProductTransfer(ProductTransfer transfer) {
		ProductTransferEvent event = new ProductTransferEvent();
		boolean inventoryToBeUpdated = false;
		if (transfer.getId() != 0) {
			ProductTransfer oldTransfer = (ProductTransfer) TransferSQL.getTransferById(transfer.getId());
			if (oldTransfer.getStatus().equals(Transfer.Status.KNOWN)) {
				inventoryToBeUpdated = true;
				event.setOldTransfer(oldTransfer);
			}
		}

		long result = TransferSQL.saveTransfer(transfer);

		if (transfer.getStatus().equals(Transfer.Status.KNOWN)) {
			inventoryToBeUpdated = true;
			event.setTransfer(transfer);
		}

		if (inventoryToBeUpdated) {
			context.createProducer().send(productInventoryDestination, event);
		}

		return result;
	}

	private long saveCashTransfer(CashTransfer transfer) {
		CashTransferEvent event = new CashTransferEvent();
		boolean inventoryToBeUpdated = false;
		if (transfer.getId() != 0) {
			CashTransfer oldTransfer = (CashTransfer) TransferSQL.getTransferById(transfer.getId());
			if (oldTransfer.getStatus().equals(Transfer.Status.KNOWN)) {
				inventoryToBeUpdated = true;
				event.setOldTransfer(oldTransfer);
			}
		}

		long result = TransferSQL.saveTransfer(transfer);

		if (transfer.getStatus().equals(Transfer.Status.KNOWN)) {
			inventoryToBeUpdated = true;
			event.setTransfer(transfer);
		}

		if (inventoryToBeUpdated) {
			context.createProducer().send(cashInventoryDestination, event);
		}
		return result;
	}

	@PreDestroy
	private void clean() {
		context.close();
	}

	@Override
	public void createTransfers(TradeEvent<?> message) throws TradistaBusinessException {
		// Load the transfer manager, depending of the trade product type
		TransferManager<TradeEvent<?>> transferManager = TradistaUtil
				.getTransferManager(message.getTrade().getProductType());
		// Manage the request with the transfer manager
		transferManager.createTransfers(message);

	}

	@Override
	public List<Transfer> getTransfersByTradeIdAndPurpose(long tradeId, TransferPurpose purpose,
			boolean includeCancel) {
		return TransferSQL.getTransfersByTradeIdAndPurpose(tradeId, purpose, includeCancel);
	}

	@Override
	public List<Transfer> getTransfersByTradeId(long tradeId) {
		return TransferSQL.getTransfersByTradeId(tradeId);
	}

	@Override
	public List<CashTransfer> getCashTransfersByProductIdAndStartDate(long productId, LocalDate startDate) {
		return TransferSQL.getCashTransfersByProductIdAndStartDate(productId, startDate);
	}

	@Override
	public void deleteTransfer(long transferId) {
		TransferSQL.deleteTransfer(transferId);
	}

	@Interceptors(TransferPostFilteringInterceptor.class)
	@Override
	public List<Transfer> getTransfers(Type type, Status status, Direction direction, TransferPurpose purpose,
			long tradeId, long productId, long bookId, long currencyId, LocalDate startFixingDate,
			LocalDate endFixingDate, LocalDate startSettlementDate, LocalDate endSettlementDate,
			LocalDate startCreationDate, LocalDate endCreationDate) {
		return TransferSQL.getTransfers(type, status, direction, purpose, tradeId, productId, bookId, currencyId,
				startFixingDate, endFixingDate, startSettlementDate, endSettlementDate, startCreationDate,
				endCreationDate);
	}

	@Override
	public void fixCashTransfers(long quoteSetId) throws TradistaBusinessException {

		if (new QuoteBusinessDelegate().getQuoteSetById(quoteSetId) == null) {
			throw new TradistaBusinessException(
					String.format("The quoteSet %s doesn't exist in the system.", quoteSetId));
		}
		// 1. Get All the Cash Transfers with status UNKNOW
		List<Transfer> transfers = getTransfers(Transfer.Type.CASH, Transfer.Status.UNKNOWN, null, null, 0, 0, 0, 0,
				null, null, null, null, null, null);

		// 2. For each of these transfers, load the right TransferManager and try to
		// fix.
		if (transfers != null && !transfers.isEmpty()) {
			for (Transfer transfer : transfers) {
				String productType = null;
				if (transfer.getTrade() != null) {
					productType = transfer.getTrade().getProductType();
				} else {
					productType = transfer.getProduct().getProductType();
				}
				TransferManager<TradeEvent<?>> transferManager = null;
				try {
					transferManager = TradistaUtil.getTransferManager(productType);
				} catch (TradistaBusinessException tbe) {
					// Should not happen here.
				}
				try {
					transferManager.fixCashTransfer((CashTransfer) transfer, quoteSetId);
				} catch (TradistaBusinessException tbe) {
					// TODO add log
				}
			}
		}

	}

}