package org.eclipse.tradista.core.common.model;

import java.time.Instant;

/********************************************************************************
 * Copyright (c) 2026 Olivier Asuncion
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

public abstract class TimestampedObject extends TradistaObject implements Timestamped {

	private static final long serialVersionUID = 1L;

	private Instant creationTime;

	private Instant lastUpdateTime;

	protected TimestampedObject() {
		this.creationTime = Instant.now();
		this.lastUpdateTime = Instant.now();
	}

	protected TimestampedObject(Builder<?, ?> builder) {
		super(builder.id);
		this.creationTime = (builder.creationTime != null) ? builder.creationTime : Instant.now();
		this.lastUpdateTime = (builder.lastUpdateTime != null) ? builder.lastUpdateTime : Instant.now();
	}

	public abstract Builder<?, ?> toBuilder();

	@Override
	public Instant getCreationTime() {
		return creationTime;
	}

	protected void setCreationTime(Instant creationTime) {
		this.creationTime = creationTime;
	}

	@Override
	public Instant getLastUpdateTime() {
		return lastUpdateTime;
	}

	public abstract static class Builder<T extends TimestampedObject, B extends Builder<T, B>> {
		protected long id;
		protected Instant creationTime;
		protected Instant lastUpdateTime;

		protected abstract B self();

		public abstract T build();

		public B id(long id) {
			this.id = id;
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

}
