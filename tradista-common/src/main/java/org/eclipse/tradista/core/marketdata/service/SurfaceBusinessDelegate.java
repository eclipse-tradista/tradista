package org.eclipse.tradista.core.marketdata.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.servicelocator.TradistaServiceLocator;
import org.eclipse.tradista.core.common.util.SecurityUtil;
import org.eclipse.tradista.core.marketdata.model.VolatilitySurface;
import org.eclipse.tradista.core.marketdata.service.SurfaceService;

/********************************************************************************
 * Copyright (c) 2018 Olivier Asuncion
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

public class SurfaceBusinessDelegate {

	private SurfaceService surfaceService;

	public SurfaceBusinessDelegate() {
		surfaceService = TradistaServiceLocator.getInstance().getSurfaceService();
	}

	public boolean surfaceExists(VolatilitySurface<?, ?, ?> surface, String type) throws TradistaBusinessException {
		if (surface == null) {
			throw new TradistaBusinessException("The surface is mandatory.");
		}
		if (StringUtils.isBlank(type)) {
			throw new TradistaBusinessException("The type is mandatory.");
		}
		return SecurityUtil.run(() -> surfaceService.surfaceExists(surface, type));
	}

	public List<VolatilitySurface<?, ?, ?>> getSurfaces(String surfaceType) {
		return SecurityUtil.run(() -> surfaceService.getSurfaces(surfaceType));
	}

	public VolatilitySurface<?, ?, ?> getSurfaceById(long surfaceId) throws TradistaBusinessException {
		if (surfaceId <= 0) {
			throw new TradistaBusinessException("The surface id must be positive.");
		}
		return SecurityUtil.runEx(() -> surfaceService.getSurfaceById(surfaceId));
	}

}