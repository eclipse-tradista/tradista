package org.eclipse.tradista.commercial.commercial.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tradista.core.common.model.Id;
import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.product.model.Product;
import org.eclipse.tradista.core.trade.model.Trade;

public class CommercialTrade extends Trade<Product> {

	public static final String COMMERCIAL = "Commercial";

	private static final long serialVersionUID = 1L;

	public static class CommercialCashFlow extends TradistaObject {

		private static final long serialVersionUID = 1L;

		@Id
		private CommercialTrade commercialTrade;

		@Id
		private LocalDate settlementDate;

		private BigDecimal amount;

		private boolean settled;

		public LocalDate getSettlementDate() {
			return settlementDate;
		}

		public void setSettlementDate(LocalDate settlementDate) {
			this.settlementDate = settlementDate;
		}

		public BigDecimal getAmount() {
			return amount;
		}

		public void setAmount(BigDecimal amount) {
			this.amount = amount;
		}

		public boolean isSettled() {
			return settled;
		}

		public void setSettled(boolean settled) {
			this.settled = settled;
		}

	}

	private String documentReference;

	private List<CommercialCashFlow> items;

	protected CommercialTrade(Builder builder) {
		super(builder);
		this.documentReference = builder.documentReference;
		this.items = builder.items != null ? new ArrayList<>(builder.items) : new ArrayList<>();
	}

	@Override
	public String getProductType() {
		return COMMERCIAL;
	}

	public String getDocumentReference() {
		return documentReference;
	}

	public void setDocumentReference(String documentReference) {
		this.documentReference = documentReference;
	}

	@SuppressWarnings("unchecked")
	public List<CommercialCashFlow> getItems() {
		return (List<CommercialCashFlow>) TradistaModelUtil.deepCopy(items);
	}

	public void setItems(List<CommercialCashFlow> items) {
		this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
	}

	@Override
	public Builder toBuilder() {
		Builder builder = new Builder();
		builder.id(getId()).creationTime(getCreationTime()).lastUpdateTime(getLastUpdateTime()).product(getProduct())
				.tradeDate(getTradeDate()).settlementDate(getSettlementDate()).amount(getAmount())
				.currency(getCurrency()).counterparty(getCounterparty()).book(getBook()).status(getStatus())
				.workflow(getWorkflow()).buySell(isBuy()).documentReference(documentReference).items(getItems());
		return builder;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static CommercialTrade create() {
		return builder().build();
	}

	public static CommercialTrade of(Instant creationTime) {
		return builder().creationTime(creationTime).build();
	}

	public static class Builder extends Trade.Builder<Product, CommercialTrade, Builder> {
		private String documentReference;
		private List<CommercialCashFlow> items = new ArrayList<>();

		private Builder() {
		}

		public Builder documentReference(String documentReference) {
			this.documentReference = documentReference;
			return self();
		}

		public Builder items(List<CommercialCashFlow> items) {
			this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
			return self();
		}

		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public CommercialTrade build() {
			return new CommercialTrade(this);
		}
	}

}