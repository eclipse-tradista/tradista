package org.eclipse.tradista.core.message.model;

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

public class IncomingMessage extends Message {

	private static final long serialVersionUID = 1983583031474077668L;
	
	public static final String INCOMING = "Incoming";

	protected IncomingMessage(Builder builder) {
		super(builder);
	}

	public static class Builder extends Message.Builder<IncomingMessage, Builder> {

		@Override
		protected Builder self() {
			return this;
		}

		@Override
		public IncomingMessage build() {
			return new IncomingMessage(this);
		}
	}

	@Override
	public Builder toBuilder() {
		return new Builder().id(this.getId()).objectId(this.getObjectId()).objectType(this.getObjectType())
				.type(this.getType()).content(this.getContent()).interfaceName(this.getInterfaceName())
				.status(this.getStatus()).creationTime(this.getCreationTime());
	}

	@Override
	public boolean isIncoming() {
		return true;
	}

}