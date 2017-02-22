/**
 * 
 */
package eu.quanticol.abcsimulator;

import java.util.LinkedList;
import java.util.PriorityQueue;

import org.apache.commons.math3.random.RandomGenerator;
import org.cmg.ml.sam.sim.Activity;
import org.cmg.ml.sam.sim.util.WeightedStructure;

/**
 * @author loreti
 *
 */
public abstract class AbCNode {
	
	private AbCSystem system;
	
	private int id;
	
	public AbCNode( AbCSystem system , int id ) {
		this.id = id;
		this.system = system;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AbCNode) {
			return this.id == ((AbCNode) obj).id;
		}
		return super.equals(obj);
	}
	
	public abstract void receive( AbCMessage message );
	
	public abstract WeightedStructure<Activity> getActivities( RandomGenerator r );
	
	public AbCSystem getSystem() {
		return system;
	}
		
	public int getIndex() {
		return id;
	}
	
	public abstract LinkedList<Integer> getInputQueueSize(LinkedList<Integer> linkedList);

	public abstract LinkedList<Integer> getWaitingQueueSize(LinkedList<Integer> linkedList);

	public abstract LinkedList<Integer> getOutputQueueSize(LinkedList<Integer> linkedList);

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ""+id;
	}
	
	
}
