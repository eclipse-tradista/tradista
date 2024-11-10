package org.eclipse.tradista.core.marketdata.service;

import java.util.List;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.marketdata.model.VolatilitySurface;

import jakarta.ejb.Remote;

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
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

@Remote
public interface SurfaceService {

	boolean surfaceExists(VolatilitySurface<?, ?, ?> surface, String type);

	List<VolatilitySurface<?, ?, ?>> getSurfaces(String surfaceType);

	VolatilitySurface<?, ?, ?> getSurfaceById(long id) throws TradistaBusinessException;

}