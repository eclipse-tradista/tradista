package org.eclipse.tradista.security.bond.ui.controller;

import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tradista.core.common.ui.util.TradistaGUIUtil;
import org.eclipse.tradista.security.bond.model.Bond;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/********************************************************************************
 * Copyright (c) 2022 Olivier Asuncion
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

public class BondProperty {

	private LongProperty id = new SimpleLongProperty();
	private StringProperty coupon = new SimpleStringProperty();
	private StringProperty maturityDate = new SimpleStringProperty();
	private StringProperty principal = new SimpleStringProperty();
	private StringProperty creationDate = new SimpleStringProperty();
	private StringProperty issuer = new SimpleStringProperty();
	private StringProperty datedDate = new SimpleStringProperty();
	private StringProperty isin = new SimpleStringProperty();

	public BondProperty(Bond bond) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		this.id.set(bond.getId());
		this.coupon.set(bond.getCoupon() == null ? StringUtils.EMPTY : TradistaGUIUtil.formatAmount(bond.getCoupon()));
		this.maturityDate.set(bond.getMaturityDate().format(dtf));
		this.principal.set(TradistaGUIUtil.formatAmount(bond.getPrincipal()));
		this.creationDate.set(bond.getCreationDate().format(dtf));
		this.datedDate.set(bond.getDatedDate().format(dtf));
		this.isin.set(bond.getIsin());
		this.issuer.set(bond.getIssuer().toString());
	}

	public LongProperty getId() {
		return id;
	}

	public StringProperty getCoupon() {
		return coupon;
	}

	public StringProperty getMaturityDate() {
		return maturityDate;
	}

	public StringProperty getPrincipal() {
		return principal;
	}

	public StringProperty getCreationDate() {
		return creationDate;
	}

	public StringProperty getIssuer() {
		return issuer;
	}

	public StringProperty getDatedDate() {
		return datedDate;
	}

	public StringProperty getIsin() {
		return isin;
	}

}