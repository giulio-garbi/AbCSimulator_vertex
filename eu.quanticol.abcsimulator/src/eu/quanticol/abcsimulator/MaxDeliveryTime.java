/**
 * 
 */
package eu.quanticol.abcsimulator;

import java.util.LinkedList;

import org.cmg.ml.sam.sim.sampling.Measure;

/**
 * @author loreti
 *
 */
public class MaxDeliveryTime implements Measure<AbCSystem> {

	@Override
	public double measure(AbCSystem t) {
		return t.getMaxDeliveryTime();
	}

	@Override
	public String getName() {
		return "MAXIMUM DELIVERY TIME";
	}

}
