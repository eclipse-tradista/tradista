package finance.tradista.ir.ircapfloorcollar.model;

import java.math.BigDecimal;

import finance.tradista.core.common.model.TradistaModelUtil;
import finance.tradista.core.product.model.Product;
import finance.tradista.core.trade.model.Trade;
import finance.tradista.ir.irforward.model.IRForwardTrade;

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
 * 
 * Class designed to represent Caps, Floors and Collars. Please note that the
 * trade amount and currency refer to premium amount and currency. In the case
 * of the collar, the premium is : premium of the cap - premium of the floor
 * 
 * @author OA
 *
 *
 */
public class IRCapFloorCollarTrade extends Trade<Product> {

	private static final long serialVersionUID = 2669121671559387940L;

	private BigDecimal capStrike;

	private BigDecimal floorStrike;

	private IRForwardTrade<Product> irForwardTrade;

	public static String IR_CAP_FLOOR_COLLAR = "IRCapFloorCollar";

	public static enum Type {
		CAP, FLOOR, COLLAR;

		public String toString() {
			switch (this) {
			case CAP:
				return "Cap";
			case FLOOR:
				return "Floor";
			case COLLAR:
				return "Collar";
			}
			return super.toString();
		}
	}

	public boolean isCap() {
		return (capStrike != null && floorStrike == null);
	}

	public boolean isFloor() {
		return (capStrike == null && floorStrike != null);
	}

	public boolean isCollar() {
		return (capStrike != null && floorStrike != null);
	}

	public String getProductType() {
		if (isCap()) {
			return "IRCap";
		}
		if (isFloor()) {
			return "IRFloor";
		}
		if (isCollar()) {
			return "IRCollar";
		}
		return null;
	}

	public BigDecimal getCapStrike() {
		return capStrike;
	}

	public void setCapStrike(BigDecimal capStrike) {
		this.capStrike = capStrike;
	}

	public BigDecimal getFloorStrike() {
		return floorStrike;
	}

	public void setFloorStrike(BigDecimal floorStrike) {
		this.floorStrike = floorStrike;
	}

	public IRForwardTrade<Product> getIrForwardTrade() {
		return TradistaModelUtil.clone(irForwardTrade);
	}

	public void setIrForwardTrade(IRForwardTrade<Product> irForwardTrade) {
		this.irForwardTrade = irForwardTrade;
	}

	@Override
	public IRCapFloorCollarTrade clone() {
		IRCapFloorCollarTrade irCapFloorCollarTrade = (IRCapFloorCollarTrade) super.clone();
		irCapFloorCollarTrade.irForwardTrade = TradistaModelUtil.clone(irForwardTrade);
		return irCapFloorCollarTrade;
	}

}