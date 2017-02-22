/**
 * 
 */
package eu.quanticol.abcsimulator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.cmg.ml.sam.sim.SimulationFactory;
import org.cmg.ml.sam.sim.sampling.Measure;

/**
 * @author loreti
 *
 */
public class RingStructureFactory implements SimulationFactory<AbCSystem>{
	
	protected int agents;
	private BiFunction<AbCNode, AbCNode, Double> sendingRate;
	private Function<AbCNode, Double> handlingRate;
	private Function<AbCNode, Double> dataRate;
	private int maxSender;
	private int elements;
	
	public RingStructureFactory(
			int elements, //Tree levels
			int agents, //Number of leaves for each node at the last level
			int maxSender, //Max number of senders at the same time (-1 is unbound)
		BiFunction<AbCNode, AbCNode, Double> sendingRate,
		Function<AbCNode, Double> handlingRate,
		Function<AbCNode, Double> dataRate		
	) {
		this.elements = elements;
		this.agents = agents;
		this.sendingRate = sendingRate;
		this.handlingRate = handlingRate;
		this.dataRate = dataRate;
		this.maxSender = maxSender;
 	}	

	@Override
	public AbCSystem getModel() {
		AbCSystem system = new AbCSystem();	
		SharedCounter counter = new SharedCounter();
		ArrayList<RingNode> ringElements = new ArrayList<>();
		int idCounter = 0;
		for( int e=0 ; e<elements ; e++ ) {
			RingNode n = new RingNode(system, idCounter++, counter, ringElements);
			ringElements.add(n);
			for( int i=0 ; i<agents; i++) {
				ComponentNode cn = new ComponentNode(system, idCounter++, n);
				n.addChild(cn);
			}
		}
		for( int e=0 ; e<elements ; e++ ) {
			RingNode n1 = ringElements.get(e);
			RingNode n2 = ringElements.get((e+1)%elements);
			n1.setNext(n2);
		}

		system.setRoot( ringElements.get(0) );
		system.setDataRate(dataRate);
		system.setSendingRate(sendingRate);
		system.setHandlingRate(handlingRate);
		system.setAgents(elements*agents);
		system.setMaxNumberOfSenders( maxSender );
		return system;
	}

	@Override
	public Measure<AbCSystem> getMeasure(String name) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
