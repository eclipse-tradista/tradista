package org.eclipse.tradista.security.bond.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.eclipse.tradista.core.common.model.TradistaModelUtil;
import org.eclipse.tradista.core.currency.model.Currency;
import org.eclipse.tradista.core.exchange.model.Exchange;
import org.eclipse.tradista.core.index.model.Index;
import org.eclipse.tradista.core.marketdata.model.Instrument;
import org.eclipse.tradista.core.tenor.model.Tenor;
import org.eclipse.tradista.security.common.model.Security;

/********************************************************************************
 * Copyright (c) 2014 Olivier Asuncion
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

public class Bond extends Security implements Instrument {

	private static final long serialVersionUID = -1544895032L;

	public static final String BOND = "Bond";

	public enum CapFloorCollar {
		NONE, CAP, FLOOR, COLLAR;

		@Override
		public String toString() {
			switch (this) {
			case NONE:
				return "None";
			case CAP:
				return "Cap";
			case FLOOR:
				return "Floor";
			case COLLAR:
				return "Collar";
			}
			return super.toString();
		}
	};

	private BigDecimal coupon;

	private LocalDate maturityDate;

	private BigDecimal principal;

	private BigDecimal redemptionPrice;

	private Currency redemptionCurrency;

	private LocalDate datedDate;

	private String couponType;

	private Tenor couponFrequency;

	private Index referenceRateindex;

	private BigDecimal cap;

	private BigDecimal floor;

	private BigDecimal spread;

	private BigDecimal leverageFactor;

	private List<Coupon> coupons;

	/**
	 * @deprecated use {@link Builder} instead.
	 */
	@Deprecated(forRemoval = true, since = "3.3.0")
	public Bond(Exchange exchange, String isin) {
		super(exchange, isin);
	}

	protected Bond(Builder builder) {
		super(builder);
		this.coupon = builder.coupon;
		this.maturityDate = builder.maturityDate;
		this.principal = builder.principal;
		this.redemptionPrice = builder.redemptionPrice;
		this.redemptionCurrency = builder.redemptionCurrency;
		this.datedDate = builder.datedDate;
		this.couponType = builder.couponType;
		this.couponFrequency = builder.couponFrequency;
		this.referenceRateindex = builder.referenceRateindex;
		this.cap = builder.cap;
		this.floor = builder.floor;
		this.spread = builder.spread;
		this.leverageFactor = builder.leverageFactor;
		this.coupons = builder.coupons;
	}

	@Override
	public Builder toBuilder() {
		Builder builder = new Builder(getExchange(), getIsin());
		builder.id(getId()).creationTime(getCreationTime()).lastUpdateTime(getLastUpdateTime()).issuer(getIssuer())
				.issueDate(getIssueDate()).issuePrice(getIssuePrice()).currency(getCurrency()).coupon(this.coupon)
				.maturityDate(this.maturityDate).principal(this.principal).redemptionPrice(this.redemptionPrice)
				.redemptionCurrency(this.redemptionCurrency).datedDate(this.datedDate).couponType(this.couponType)
				.couponFrequency(this.couponFrequency).referenceRateIndex(this.referenceRateindex).cap(this.cap)
				.floor(this.floor).spread(this.spread).leverageFactor(this.leverageFactor).coupons(this.coupons);
		return builder;
	}

	public static class Builder extends Security.Builder<Bond, Builder> {
		protected BigDecimal coupon;
		protected LocalDate maturityDate;
		protected BigDecimal principal;
		protected BigDecimal redemptionPrice;
		protected Currency redemptionCurrency;
		protected LocalDate datedDate;
		protected String couponType;
		protected Tenor couponFrequency;
		protected Index referenceRateindex;
		protected BigDecimal cap;
		protected BigDecimal floor;
		protected BigDecimal spread;
		protected BigDecimal leverageFactor;
		protected List<Coupon> coupons;

		public Builder(Exchange exchange, String isin) {
			super(exchange, isin);
		}

		public Builder coupon(BigDecimal coupon) {
			this.coupon = coupon;
			return this;
		}

		public Builder maturityDate(LocalDate maturityDate) {
			this.maturityDate = maturityDate;
			return this;
		}

		public Builder principal(BigDecimal principal) {
			this.principal = principal;
			return this;
		}

		public Builder redemptionPrice(BigDecimal redemptionPrice) {
			this.redemptionPrice = redemptionPrice;
			return this;
		}

		public Builder redemptionCurrency(Currency redemptionCurrency) {
			this.redemptionCurrency = redemptionCurrency;
			return this;
		}

		public Builder datedDate(LocalDate datedDate) {
			this.datedDate = datedDate;
			return this;
		}

		public Builder couponType(String couponType) {
			this.couponType = couponType;
			return this;
		}

		public Builder couponFrequency(Tenor couponFrequency) {
			this.couponFrequency = couponFrequency;
			return this;
		}

		public Builder referenceRateIndex(Index referenceRateindex) {
			this.referenceRateindex = referenceRateindex;
			return this;
		}

		public Builder cap(BigDecimal cap) {
			this.cap = cap;
			return this;
		}

		public Builder floor(BigDecimal floor) {
			this.floor = floor;
			return this;
		}

		public Builder spread(BigDecimal spread) {
			this.spread = spread;
			return this;
		}

		public Builder leverageFactor(BigDecimal leverageFactor) {
			this.leverageFactor = leverageFactor;
			return this;
		}

		public Builder coupons(List<Coupon> coupons) {
			this.coupons = coupons;
			return this;
		}

		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public Bond build() {
			return new Bond(this);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Coupon> getCoupons() {
		return (List<Coupon>) TradistaModelUtil.deepCopy(coupons);
	}

	public void setCoupons(List<Coupon> coupons) {
		this.coupons = coupons;
	}

	public BigDecimal getCoupon() {
		return coupon;
	}

	public LocalDate getLastCouponDate() {
		Coupon lastCoupon = null;
		for (Coupon coupon : coupons) {
			if (lastCoupon == null) {
				lastCoupon = coupon;
			} else {
				if (coupon.getDate().isAfter(lastCoupon.getDate())) {
					lastCoupon = coupon;
				}
			}
		}
		return lastCoupon.getDate();
	}

	public void setCoupon(BigDecimal coupon) {
		this.coupon = coupon;
	}

	public LocalDate getMaturityDate() {
		return maturityDate;
	}

	public void setMaturityDate(LocalDate maturityDate) {
		this.maturityDate = maturityDate;
	}

	public BigDecimal getPrincipal() {
		return principal;
	}

	public String getProductType() {
		return BOND;
	}

	public void setPrincipal(BigDecimal principal) {
		this.principal = principal;
	}

	public LocalDate getDatedDate() {
		return datedDate;
	}

	public void setDatedDate(LocalDate datedDate) {
		this.datedDate = datedDate;
	}

	public String getCouponType() {
		return couponType;
	}

	public void setCouponType(String couponType) {
		this.couponType = couponType;
	}

	public Tenor getCouponFrequency() {
		return couponFrequency;
	}

	public void setCouponFrequency(Tenor couponFrequency) {
		this.couponFrequency = couponFrequency;
	}

	public BigDecimal getRedemptionPrice() {
		return redemptionPrice;
	}

	public void setRedemptionPrice(BigDecimal redemptionPrice) {
		this.redemptionPrice = redemptionPrice;
	}

	public Currency getRedemptionCurrency() {
		return TradistaModelUtil.clone(redemptionCurrency);
	}

	public long getRedemptionCurrencyId() {
		if (redemptionCurrency != null) {
			return redemptionCurrency.getId();
		} else {
			return 0;
		}
	}

	public void setRedemptionCurrency(Currency redemptionCurrency) {
		this.redemptionCurrency = redemptionCurrency;
	}

	public Index getReferenceRateIndex() {
		return TradistaModelUtil.clone(referenceRateindex);
	}

	public void setReferenceRateIndex(Index index) {
		this.referenceRateindex = index;
	}

	public BigDecimal getCap() {
		return cap;
	}

	public void setCap(BigDecimal cap) {
		this.cap = cap;
	}

	public BigDecimal getFloor() {
		return floor;
	}

	public void setFloor(BigDecimal floor) {
		this.floor = floor;
	}

	public boolean isCap() {
		return (cap != null && floor == null);
	}

	public boolean isFloor() {
		return (cap == null && floor != null);
	}

	public boolean isCollar() {
		return (cap != null && floor != null);
	}

	public BigDecimal getSpread() {
		return spread;
	}

	public void setSpread(BigDecimal spread) {
		this.spread = spread;
	}

	public BigDecimal getLeverageFactor() {
		return leverageFactor;
	}

	public void setLeverageFactor(BigDecimal leverageFactor) {
		this.leverageFactor = leverageFactor;
	}

	@Override
	public String getInstrumentName() {
		return BOND;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Bond clone() {
		Bond bond = (Bond) super.clone();
		bond.redemptionCurrency = TradistaModelUtil.clone(redemptionCurrency);
		bond.referenceRateindex = TradistaModelUtil.clone(referenceRateindex);
		bond.coupons = (List<Coupon>) TradistaModelUtil.deepCopy(coupons);
		return bond;
	}

}