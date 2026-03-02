package org.eclipse.tradista.core.exporter.model;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.legalentity.model.LegalEntity;

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

public interface Exporter<X, Y> extends Runnable {

	String getType();

	String getName();

	LegalEntity getProcessingOrg();

	void exportObject(X object) throws TradistaBusinessException;

	Y createContent(X object) throws TradistaBusinessException;

	void sendMessage(Y content) throws TradistaBusinessException;

}