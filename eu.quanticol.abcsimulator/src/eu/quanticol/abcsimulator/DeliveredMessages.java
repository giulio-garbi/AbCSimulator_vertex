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
public class DeliveredMessages implements Measure<AbCSystem> {

	@Override
	public double measure(AbCSystem t) {
		return t.getDeliveredMessages();
	}

	@Override
	public String getName() {
		return "AVERAGE DELIVERY TIME";
	}

}
