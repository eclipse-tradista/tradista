package org.eclipse.tradista.core.error.model;

import java.time.LocalDateTime;

import org.eclipse.tradista.core.common.model.TradistaObject;

/********************************************************************************
 * Copyright (c) 2016 Olivier Asuncion
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
 * Business error viewable by the user.
 * 
 * @author OA
 *
 */
public abstract class Error extends TradistaObject {

	private LocalDateTime errorDate;

	private LocalDateTime solvingDate;

	private String errorMessage;

	private String type;

	private Status status;

	public enum Status {
		SOLVED, UNSOLVED;

		@Override
		public String toString() {
			switch (this) {
			case SOLVED:
				return "Solved";
			case UNSOLVED:
				return "Unsolved";
			}
			return super.toString();
		}

		public static Status getStatus(String displayValue) {
			switch (displayValue) {
			case "Solved":
				return SOLVED;
			case "Unsolved":
				return UNSOLVED;
			}
			return null;
		}
	}

	private static final long serialVersionUID = -2187943264623829665L;

	protected Error() {
		status = Status.UNSOLVED;
	}

	public LocalDateTime getSolvingDate() {
		return solvingDate;
	}

	public void setSolvingDate(LocalDateTime solvingDate) {
		this.solvingDate = solvingDate;
	}

	public String getType() {
		return type;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void setType(String type) {
		this.type = type;
	}

	public LocalDateTime getErrorDate() {
		return errorDate;
	}

	public void setErrorDate(LocalDateTime errorDate) {
		this.errorDate = errorDate;
	}

	/**
	 * Key used to identify the error subject. Using this key, the system should be
	 * able to automatically change the status of a solved error
	 * 
	 * @return the identifier of the error subject
	 */
	public abstract String getSubjectKey();

	public void solve() {
		setStatus(Status.SOLVED);
		setSolvingDate(LocalDateTime.now());
	}

	public void update(String errorMessage) {
		setErrorMessage(errorMessage);
		setErrorDate(LocalDateTime.now());
	}

}