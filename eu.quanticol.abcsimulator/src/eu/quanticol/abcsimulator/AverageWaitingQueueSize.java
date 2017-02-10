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
public class AverageWaitingQueueSize implements Measure<AbCSystem> {

	@Override
	public double measure(AbCSystem t) {
		LinkedList<Integer> sizeList = t.getWaitingQueueSize();
		
		if (sizeList.isEmpty()) {
			return 0.0;
		}
		double tot = 0.0;
		for (Integer v : sizeList) {
			tot += v;
		}
		//System.out.println(tot);
		return tot/sizeList.size();
		
	}

	@Override
	public String getName() {
		return "AVERAGE WAITING QUEUE SIZE";
	}

}
