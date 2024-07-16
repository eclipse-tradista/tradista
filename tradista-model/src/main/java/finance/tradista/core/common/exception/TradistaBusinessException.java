package finance.tradista.core.common.exception;

import jakarta.ejb.ApplicationException;

/********************************************************************************
 * Copyright (c) 2015 Olivier Asuncion
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

@ApplicationException
public class TradistaBusinessException extends Exception {

	private static final long serialVersionUID = 3972649531632900754L;

	public TradistaBusinessException(String msg) {
		super(msg);
	}

	public TradistaBusinessException(Exception exception) {
		super(exception);
	}

}