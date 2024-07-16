package finance.tradista.core.trade.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import finance.tradista.core.common.model.TradistaModelUtil;
import finance.tradista.core.product.model.Product;

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
 * License for the specific language governing permissions and limitations
 * under the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
/**
 * Abstract class representing Options. Please note that : - getAmount() gets to
 * premium amount - getCurrency() gets the premium currency
 * 
 * @author Tradista
 *
 * @param <T>
 */
public abstract class OptionTrade<T extends Trade<? extends Product>> extends Trade<Product> {

	public static enum Type {
		CALL, PUT;

		public String toString() {
			switch (this) {
			case CALL:
				return "Call";
			case PUT:
				return "Put";
			}
			return super.toString();
		}

		public static Type getType(String displayValue) {
			switch (displayValue) {
			case "Call":
				return CALL;
			case "Put":
				return PUT;
			}
			return null;
		}
	};

	public static enum SettlementType {
		CASH, PHYSICAL;

		public String toString() {
			switch (this) {
			case CASH:
				return "Cash";
			case PHYSICAL:
				return "Physical";
			}
			return super.toString();
		}

		public static SettlementType getType(String displayValue) {
			switch (displayValue) {
			case "Cash":
				return CASH;
			case "Physical":
				return PHYSICAL;
			}
			return null;
		}
	};

	private Type type;

	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 6498388754667143318L;

	private T underlying;

	private LocalDate maturityDate;

	private BigDecimal strike;

	private SettlementType settlementType;

	private int settlementDateOffset;

	private LocalDate exerciseDate;

	public LocalDate getExerciseDate() {
		return exerciseDate;
	}

	public void setExerciseDate(LocalDate exerciseDate) {
		this.exerciseDate = exerciseDate;
	}

	public SettlementType getSettlementType() {
		return settlementType;
	}

	public void setSettlementType(SettlementType settlementType) {
		this.settlementType = settlementType;
	}

	public int getSettlementDateOffset() {
		return settlementDateOffset;
	}

	public void setSettlementDateOffset(int settlementDateOffset) {
		this.settlementDateOffset = settlementDateOffset;
	}

	public BigDecimal getStrike() {
		return strike;
	}

	public void setStrike(BigDecimal strike) {
		this.strike = strike;
	}

	public LocalDate getMaturityDate() {
		return maturityDate;
	}

	public void setMaturityDate(LocalDate maturityDate) {
		this.maturityDate = maturityDate;
	}

	public T getUnderlying() {
		return TradistaModelUtil.clone(underlying);
	}

	public void setUnderlying(T underlying) {
		this.underlying = underlying;
	}

	public Type getType() {
		return type;
	}

	public boolean isCall() {
		return getType().equals(OptionTrade.Type.CALL);
	}

	public boolean isPut() {
		return getType().equals(OptionTrade.Type.PUT);
	}

	public LocalDate getUnderlyingSettlementDate() {
		if (getExerciseDate() == null) {
			return null;
		}
		if (getUnderlying() != null) {
			return getUnderlying().getSettlementDate();
		}
		return null;
	}

	public LocalDate getUnderlyingTradeDate() {
		if (getExerciseDate() == null) {
			return null;
		}
		if (getUnderlying() != null) {
			return getUnderlying().getTradeDate();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public OptionTrade<T> clone() {
		OptionTrade<T> optionTrade = (OptionTrade<T>) super.clone();
		optionTrade.underlying = TradistaModelUtil.clone(underlying);
		return optionTrade;
	}

}