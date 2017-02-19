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
public class MaxMessageInterval implements Measure<AbCSystem> {

	@Override
	public double measure(AbCSystem t) {
		return t.getMessageIntevalStatistics().getMax();
	}

	@Override
	public String getName() {
		return "MAX MESSAGE INTERVAL";
	}

}
