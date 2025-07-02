package org.eclipse.tradista.core.marketdata.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.service.InformationBusinessDelegate;
import org.eclipse.tradista.core.marketdata.model.VolatilitySurface;
import org.eclipse.tradista.core.marketdata.persistence.SurfaceSQL;
import org.eclipse.tradista.fx.fxoption.model.FXVolatilitySurface;
import org.eclipse.tradista.fx.fxoption.service.FXVolatilitySurfaceBusinessDelegate;
import org.eclipse.tradista.ir.irswapoption.model.SwaptionVolatilitySurface;
import org.eclipse.tradista.ir.irswapoption.service.SwaptionVolatilitySurfaceBusinessDelegate;
import org.eclipse.tradista.security.equityoption.model.EquityOptionVolatilitySurface;
import org.eclipse.tradista.security.equityoption.service.EquityOptionVolatilitySurfaceBusinessDelegate;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

/********************************************************************************
 * Copyright (c) 2019 Olivier Asuncion
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

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class SurfaceServiceBean implements SurfaceService {

	private InformationBusinessDelegate informationBusinessDelegate;

	@PostConstruct
	private void init() {
		informationBusinessDelegate = new InformationBusinessDelegate();
	}

	@Override
	public boolean surfaceExists(VolatilitySurface<?, ?, ?> surface, String type) {
		return SurfaceSQL.surfaceExists(surface, type);
	}

	@Override
	public List<VolatilitySurface<?, ?, ?>> getSurfaces(String surfaceType) {
		List<VolatilitySurface<?, ?, ?>> surfaces = null;
		if (surfaceType == null) {
			if (informationBusinessDelegate.hasFXModule()) {
				Set<FXVolatilitySurface> fxSurfaces = new FXVolatilitySurfaceBusinessDelegate()
						.getAllFXVolatilitySurfaces();
				if (fxSurfaces != null && !fxSurfaces.isEmpty()) {
					surfaces = new ArrayList<>();
					surfaces.addAll(fxSurfaces);
				}
			}
			if (informationBusinessDelegate.hasSecurityModule()) {
				Set<EquityOptionVolatilitySurface> equityOptionSurfaces = new EquityOptionVolatilitySurfaceBusinessDelegate()
						.getAllEquityOptionVolatilitySurfaces();
				if (equityOptionSurfaces != null && !equityOptionSurfaces.isEmpty()) {
					if (surfaces == null) {
						surfaces = new ArrayList<>();
					}
					surfaces.addAll(equityOptionSurfaces);
				}
			}
			if (informationBusinessDelegate.hasIRModule()) {
				Set<SwaptionVolatilitySurface> swaptionVolatilitySurfaces = new SwaptionVolatilitySurfaceBusinessDelegate()
						.getAllSwaptionVolatilitySurfaces();
				if (swaptionVolatilitySurfaces != null && !swaptionVolatilitySurfaces.isEmpty()) {
					if (surfaces == null) {
						surfaces = new ArrayList<>();
					}
					surfaces.addAll(swaptionVolatilitySurfaces);
				}
			}
		} else if (surfaceType.equals("IR")) {
			if (informationBusinessDelegate.hasIRModule()) {
				Set<SwaptionVolatilitySurface> swaptionVolatilitySurfaces = new SwaptionVolatilitySurfaceBusinessDelegate()
						.getAllSwaptionVolatilitySurfaces();
				if (swaptionVolatilitySurfaces != null && !swaptionVolatilitySurfaces.isEmpty()) {
					surfaces = new ArrayList<>();
					surfaces.addAll(swaptionVolatilitySurfaces);
				}
			}
		} else if (surfaceType.equals("FX")) {
			if (informationBusinessDelegate.hasFXModule()) {
				Set<FXVolatilitySurface> fxSurfaces = new FXVolatilitySurfaceBusinessDelegate()
						.getAllFXVolatilitySurfaces();
				if (fxSurfaces != null && !fxSurfaces.isEmpty()) {
					surfaces = new ArrayList<>();
					surfaces.addAll(fxSurfaces);
				}
			}
		} else if (surfaceType.equals("EquityOption")) {
			if (informationBusinessDelegate.hasSecurityModule()) {
				Set<EquityOptionVolatilitySurface> equityOptionSurfaces = new EquityOptionVolatilitySurfaceBusinessDelegate()
						.getAllEquityOptionVolatilitySurfaces();
				if (equityOptionSurfaces != null && !equityOptionSurfaces.isEmpty()) {
					surfaces = new ArrayList<>();
					surfaces.addAll(equityOptionSurfaces);
				}
			}
		}
		return surfaces;
	}

	@Interceptors(VolatilitySurfaceFilteringInterceptor.class)
	@Override
	public VolatilitySurface<?, ?, ?> getSurfaceById(long id) throws TradistaBusinessException {
		VolatilitySurface<?, ?, ?> surface = null;
		if (informationBusinessDelegate.hasFXModule()) {
			surface = new FXVolatilitySurfaceBusinessDelegate().getFXVolatilitySurfaceById(id);
		}
		if (surface == null) {
			if (informationBusinessDelegate.hasSecurityModule()) {
				surface = new EquityOptionVolatilitySurfaceBusinessDelegate().getEquityOptionVolatilitySurfaceById(id);
			}
		}
		if (surface == null) {
			if (informationBusinessDelegate.hasIRModule()) {
				surface = new SwaptionVolatilitySurfaceBusinessDelegate().getSwaptionVolatilitySurfaceById(id);
			}
		}

		return surface;
	}

}