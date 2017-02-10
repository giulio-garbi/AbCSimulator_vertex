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
public class MinDeliveryTime implements Measure<AbCSystem> {

	@Override
	public double measure(AbCSystem t) {
		return t.getMinDeliveryTime();
	}

	@Override
	public String getName() {
		return "MINIMUM DELIVERY TIME";
	}

}
