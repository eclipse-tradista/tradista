package org.eclipse.tradista.core.message.model;

import java.time.Instant;

import org.eclipse.tradista.core.common.model.TimestampedObject;
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

public abstract class Message extends TimestampedObject implements WorkflowObject {

	private static final long serialVersionUID = -625696923557029488L;

	public static final String TRADE = "Trade";

	private long objectId;

	private String objectType;

	private String type;

	private String content;

	private Status status;

	private String interfaceName;

	protected Message(Builder<?, ?> builder) {
		super(builder);
		this.objectId = builder.objectId;
		this.objectType = builder.objectType;
		this.type = builder.type;
		this.content = builder.content;
		this.status = builder.status;
		this.interfaceName = builder.interfaceName;
	}

	public abstract Builder<?, ?> toBuilder();

	public long getObjectId() {
		return objectId;
	}

	public String getObjectType() {
		return objectType;
	}

	public String getType() {
		return type;
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

	public abstract static class Builder<T extends Message, B extends Builder<T, B>>
			extends TimestampedObject.Builder<T, B> {
		protected long objectId;
		protected String objectType;
		protected String type;
		protected String content;
		protected Status status;
		protected String interfaceName;

		public B objectId(long objectId) {
			this.objectId = objectId;
			return self();
		}

		public B objectType(String type) {
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
	}

	@Override
	public Message clone() {
		Message msg = (Message) super.clone();
		msg.setStatus((Status) status.clone());
		return msg;
	}

}