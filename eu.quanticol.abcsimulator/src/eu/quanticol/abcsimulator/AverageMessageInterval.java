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
public class AverageMessageInterval implements Measure<AbCSystem> {

	@Override
	public double measure(AbCSystem t) {
		return t.getMessageIntevalStatistics().getMean();
	}

	@Override
	public String getName() {
		return "AVERAGE MESSAGE INTERVAL";
	}

}
