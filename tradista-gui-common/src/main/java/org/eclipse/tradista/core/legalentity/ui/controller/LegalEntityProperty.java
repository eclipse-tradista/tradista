package org.eclipse.tradista.core.legalentity.ui.controller;

import org.eclipse.tradista.core.legalentity.model.LegalEntity;

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

public class LegalEntityProperty {

	private LongProperty id = new SimpleLongProperty();
	private StringProperty shortName = new SimpleStringProperty();
	private StringProperty longName = new SimpleStringProperty();
	private StringProperty role = new SimpleStringProperty();
	private StringProperty description = new SimpleStringProperty();

	public LegalEntityProperty(LegalEntity legalEntity) {
		this.id.set(legalEntity.getId());
		this.shortName.set(legalEntity.getShortName());
		this.longName.set(legalEntity.getLongName());
		this.role.set(legalEntity.getRole().name());
		this.description.set(legalEntity.getDescription());
	}

	public LongProperty getId() {
		return id;
	}

	public StringProperty getShortName() {
		return shortName;
	}

	public StringProperty getLongName() {
		return longName;
	}

	public StringProperty getRole() {
		return role;
	}

	public StringProperty getDescription() {
		return description;
	}

}