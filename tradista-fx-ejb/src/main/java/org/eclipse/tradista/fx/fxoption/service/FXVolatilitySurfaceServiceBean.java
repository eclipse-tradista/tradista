package org.eclipse.tradista.fx.fxoption.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.util.TradistaUtil;
import org.eclipse.tradista.core.marketdata.constants.MarketDataConstants;
import org.eclipse.tradista.core.marketdata.generationalgorithm.SurfaceGenerationAlgorithm;
import org.eclipse.tradista.core.marketdata.interpolator.MultivariateInterpolator;
import org.eclipse.tradista.core.marketdata.model.QuoteSet;
import org.eclipse.tradista.core.marketdata.model.SurfacePoint;
import org.eclipse.tradista.core.marketdata.service.SurfaceBusinessDelegate;
import org.eclipse.tradista.core.marketdata.service.VolatilitySurfaceFilteringInterceptor;
import org.eclipse.tradista.fx.fx.service.FXProductScopeFilteringInterceptor;
import org.eclipse.tradista.fx.fxoption.model.FXOptionTrade;
import org.eclipse.tradista.fx.fxoption.model.FXVolatilitySurface;
import org.eclipse.tradista.fx.fxoption.persistence.FXVolatilitySurfaceSQL;
import org.jboss.ejb3.annotation.SecurityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

/*
 * Copyright 2015 Olivier Asuncion
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.    */

@SecurityDomain(value = "other")
@PermitAll
@Stateless
public class FXVolatilitySurfaceServiceBean implements FXVolatilitySurfaceService {

	@Interceptors(VolatilitySurfaceFilteringInterceptor.class)
	@Override
	public Set<FXVolatilitySurface> getAllFXVolatilitySurfaces() {
		return FXVolatilitySurfaceSQL.getAllFXVolatilitySurfaces();
	}

	@Override
	public FXVolatilitySurface getFXVolatilitySurfaceByName(String name) {
		return FXVolatilitySurfaceSQL.getFXVolatilitySurfaceByName(name);
	}

	@Interceptors(VolatilitySurfaceFilteringInterceptor.class)
	@Override
	public FXVolatilitySurface getFXVolatilitySurfaceById(long id) {
		return FXVolatilitySurfaceSQL.getFXVolatilitySurfaceById(id);
	}

	@Override
	public boolean saveFXVolatilitySurfacePoints(long id,
			List<SurfacePoint<Long, BigDecimal, BigDecimal>> surfacePoints, Long optionExpiry, BigDecimal strike) {
		// TODO Auto-generated method stub
		return FXVolatilitySurfaceSQL.saveFXVolatilitySurfacePoints(id, surfacePoints, optionExpiry, strike);
	}

	@Interceptors(VolatilitySurfaceFilteringInterceptor.class)
	@Override
	public boolean deleteFXVolatilitySurface(long surfaceId) throws TradistaBusinessException {
		return FXVolatilitySurfaceSQL.deleteFXVolatilitySurface(surfaceId);
	}

	@Override
	public BigDecimal getVolatility(String surfaceName, int timeToMaturity, double tenor) {
		return FXVolatilitySurfaceSQL.getVolatilityBySurfaceNameOptionExpiryAndStrike(surfaceName, timeToMaturity,
				tenor);
	}

	@Override
	public List<SurfacePoint<Number, Number, Number>> getFXVolatilitySurfacePointsBySurfaceIdOptionExpiryAndStrike(
			long surfaceId, Long optionExpiry, BigDecimal strike) {
		return FXVolatilitySurfaceSQL.getFXVolatilitySurfacePointsBySurfaceIdOptionExpiryAndStrike(surfaceId,
				optionExpiry, strike);
	}

	@Interceptors({ FXProductScopeFilteringInterceptor.class, VolatilitySurfaceFilteringInterceptor.class })
	@Override
	public long saveFXVolatilitySurface(FXVolatilitySurface surface) throws TradistaBusinessException {
		if (surface.getId() == 0) {
			checkSurfaceExistence(surface);
		} else {
			FXVolatilitySurface oldSurface = FXVolatilitySurfaceSQL.getFXVolatilitySurfaceById(surface.getId());
			if (!oldSurface.getName().equals(surface.getName())
					|| !oldSurface.getProcessingOrg().equals(surface.getProcessingOrg())) {
				checkSurfaceExistence(surface);
			}
		}
		return FXVolatilitySurfaceSQL.saveFXVolatilitySurface(surface);
	}

	private void checkSurfaceExistence(FXVolatilitySurface surface) throws TradistaBusinessException {
		if (new SurfaceBusinessDelegate().surfaceExists(surface, FXOptionTrade.FX_OPTION)) {
			throw new TradistaBusinessException(
					String.format("An %s volatility surface named %s already exists for the PO %s.",
							FXOptionTrade.FX_OPTION, surface.getName(), surface.getProcessingOrg()));
		}
	}

	@Override
	public List<SurfacePoint<Integer, BigDecimal, BigDecimal>> generate(String algorithm, String interpolator,
			String instance, LocalDate quoteDate, QuoteSet quoteSet, List<String> quoteNames, List<BigDecimal> deltas) {
		// Controls
		// Check if algorithm is supported
		if (!getAllGenerationAlgorithms().contains(algorithm)) {
			throw new IllegalArgumentException("The '" + algorithm + "' algorithm doesn't exist.");
		}
		// Check interpolator is supported
		if (!getAllInterpolators().contains(interpolator)) {
			throw new IllegalArgumentException("The '" + interpolator + "' interpolator doesn't exist.");
		}
		// Check instance is supported
		if (!getAllInstances().contains(instance)) {
			throw new IllegalArgumentException("The '" + instance + "' instance doesn't exist.");
		}
		// Check quoteids are valid
		if (quoteNames == null || quoteNames.isEmpty()) {
			throw new IllegalArgumentException("The quote Names list is null or empty.");
		}

		// Get the generation algorithm
		SurfaceGenerationAlgorithm genAlgorithm = TradistaUtil.getInstance(SurfaceGenerationAlgorithm.class,
				MarketDataConstants.GENERATION_ALGORITHM_PACKAGE + "." + algorithm);
		// Get the interpolator
		MultivariateInterpolator interpolatorObject = TradistaUtil.getInstance(MultivariateInterpolator.class,
				MarketDataConstants.INTERPOLATOR_PACKAGE + "." + interpolator);
		List<SurfacePoint<Integer, BigDecimal, BigDecimal>> surfacePoints = genAlgorithm.generate("FX", instance,
				quoteNames, quoteDate, quoteSet, interpolatorObject, deltas);

		return surfacePoints;
	}

	@Override
	public Set<String> getAllGenerationAlgorithms() {
		return TradistaUtil.getAvailableNames(SurfaceGenerationAlgorithm.class,
				MarketDataConstants.GENERATION_ALGORITHM_PACKAGE);
	}

	@Override
	public Set<String> getAllInterpolators() {
		return TradistaUtil.getAvailableNames(MultivariateInterpolator.class, MarketDataConstants.INTERPOLATOR_PACKAGE);
	}

	@Override
	public Set<String> getAllInstances() {
		Set<String> instances = new HashSet<String>();
		instances.add("CLOSE");
		instances.add("OPEN");
		instances.add("BID");
		instances.add("ASK");
		instances.add("MID");
		return instances;
	}

	@Override
	public List<SurfacePoint<Integer, BigDecimal, BigDecimal>> getFXVolatilitySurfacePointsBySurfaceId(
			long volatilitySurfaceId) {
		return FXVolatilitySurfaceSQL.getFXVolatilitySurfacePointsBySurfaceId(volatilitySurfaceId);
	}

	@Override
	public BigDecimal getVolatility(String volatilitySurfaceName, int optionExpiry) {
		return FXVolatilitySurfaceSQL.getVolatilityBySurfaceNameOptionExpiry(volatilitySurfaceName, optionExpiry);
	}
}