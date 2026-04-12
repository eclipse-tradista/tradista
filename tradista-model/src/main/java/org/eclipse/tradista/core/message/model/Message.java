package org.eclipse.tradista.core.message.model;

import java.time.Instant;

import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.workflow.model.Status;
import org.eclipse.tradista.core.workflow.model.WorkflowObject;

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

public abstract class Message extends TradistaObject implements WorkflowObject {

	private static final long serialVersionUID = -625696923557029488L;

	private long objectId;

	private ObjectType objectType;

	private String type;

	private Instant creationTime;

	private Instant lastUpdateTime;

	private String content;

	private Status status;

	private String interfaceName;

	protected Message(Builder<?, ?> builder) {
		setId(builder.id);
		this.objectId = builder.objectId;
		this.objectType = builder.objectType;
		this.type = builder.type;
		this.content = builder.content;
		this.status = builder.status;
		this.interfaceName = builder.interfaceName;
		this.creationTime = (builder.creationTime != null) ? builder.creationTime : Instant.now();
		this.lastUpdateTime = (builder.lastUpdateTime != null) ? builder.lastUpdateTime : Instant.now();
	}

	public abstract Builder<?, ?> toBuilder();

	public enum ObjectType {
		TRADE;

		@Override
		public String toString() {
			return switch (this) {
			case TRADE -> "Trade";
			default -> super.toString();
			};
		}
	}

	public long getObjectId() {
		return objectId;
	}

	public ObjectType getObjectType() {
		return objectType;
	}

	public String getType() {
		return type;
	}

	public Instant getCreationTime() {
		return creationTime;
	}

	public Instant getLastUpdateTime() {
		return lastUpdateTime;
	}

	public String getContent() {
		return content;
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public abstract boolean isIncoming();

	@Override
	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public String getWorkflow() {
		if (status != null) {
			return status.getWorkflowName();
		}
		return null;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	public abstract static class Builder<T extends Message, B extends Builder<T, B>> {
		protected long id;
		protected long objectId;
		protected ObjectType objectType;
		protected String type;
		protected String content;
		protected Status status;
		protected Instant creationTime;
		protected Instant lastUpdateTime;
		protected String interfaceName;

		protected abstract B self();

		public abstract T build();

		public B id(long id) {
			this.id = id;
			return self();
		}

		public B objectId(long objectId) {
			this.objectId = objectId;
			return self();
		}

		public B objectType(ObjectType type) {
			this.objectType = type;
			return self();
		}

		public B type(String type) {
			this.type = type;
			return self();
		}

		public B content(String content) {
			this.content = content;
			return self();
		}

		public B status(Status status) {
			this.status = status;
			return self();
		}

		public B interfaceName(String name) {
			this.interfaceName = name;
			return self();
		}

		public B creationTime(Instant ct) {
			this.creationTime = ct;
			return self();
		}

		public B lastUpdateTime(Instant lut) {
			this.lastUpdateTime = lut;
			return self();
		}
	}

	@Override
	public Message clone() {
		Message msg = (Message) super.clone();
		msg.setStatus((Status) status.clone());
		return msg;
	}

}