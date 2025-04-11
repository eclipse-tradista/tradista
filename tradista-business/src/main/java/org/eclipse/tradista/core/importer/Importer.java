package org.eclipse.tradista.core.importer;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.error.service.ErrorBusinessDelegate;
import org.eclipse.tradista.core.message.model.Message;
import org.eclipse.tradista.core.message.service.MessageBusinessDelegate;

/********************************************************************************
 * Copyright (c) 2025 Olivier Asuncion
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

public abstract class Importer implements Runnable {
	
	private MessageBusinessDelegate messageBusinessDelegate;
	
	private ErrorBusinessDelegate errorBusinessDelegate;
	
	public Importer() {
		messageBusinessDelegate = new MessageBusinessDelegate();
		errorBusinessDelegate = new ErrorBusinessDelegate();
	}

	private String name;

	protected abstract void start();

	public abstract String getType();
	
	protected void importMessage(Object externalMessage) throws TradistaBusinessException {
		Message msg = createMessage(externalMessage);
		try {
		validateMessage(externalMessage);
		} catch (TradistaBusinessException tbe) {
			errorBusinessDelegate.
			messageBusinessDelegate.saveMessage(msg);
		}
		Optional<? extends TradistaObject> object = processMessage(externalMessage);
		if (object.isPresent()) {
			saveObject(object.get());
		}
	}

	protected abstract void validateMessage(Object externalMessage) throws TradistaBusinessException;
	
	protected abstract Message createMessage(Object externalMessage);
	
	protected abstract Optional<? extends TradistaObject> processMessage(Object externalMessage);
	
	protected abstract void saveObject(TradistaObject tradistaObject);

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void run() {
		start();
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Importer other = (Importer) obj;
		return Objects.equals(name, other.name);
	}

}