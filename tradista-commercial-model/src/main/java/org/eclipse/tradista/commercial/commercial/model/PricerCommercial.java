package org.eclipse.tradista.commercial.commercial.model;

import org.eclipse.tradista.core.pricing.pricer.Pricer;

public class PricerCommercial extends Pricer {

	private static final long serialVersionUID = 1L;

	public PricerCommercial() {
		super();
		getPricerMeasures().add(new PricerMeasureNPV());
	}

}
