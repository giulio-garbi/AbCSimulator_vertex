/**
 * 
 */
package eu.quanticol.abcsimulator;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.cmg.ml.sam.sim.SimulationFactory;
import org.cmg.ml.sam.sim.sampling.Measure;

/**
 * @author loreti
 *
 */
public class SingleServerFactory implements SimulationFactory<AbCSystem>{
	
	protected int agents;
	private BiFunction<AbCNode, AbCNode, Double> sendingRate;
	private Function<AbCNode, Double> handlingRate;
	private Function<AbCNode, Double> dataRate;
	private int maxSender;
	
	public SingleServerFactory(int agents,
			int maxSender,
		BiFunction<AbCNode, AbCNode, Double> sendingRate,
		Function<AbCNode, Double> handlingRate,
		Function<AbCNode, Double> dataRate		
	) {
		this.agents = agents;
		this.sendingRate = sendingRate;
		this.handlingRate = handlingRate;
		this.dataRate = dataRate;
		this.maxSender = maxSender;
 	}	

	@Override
	public AbCSystem getModel() {
		AbCSystem system = new AbCSystem();
		
		ServerNode root = new ServerNode(system, 0);
		for( int i=0 ; i<agents ; i++ ) {
			ComponentNode n = new ComponentNode(system, i+1, root);
			root.addChild( n );
		}
	
		system.setRoot( root );
		system.setDataRate(dataRate);
		system.setSendingRate(sendingRate);
		system.setHandlingRate(handlingRate);
		system.setAgents(agents);
		system.setMaxNumberOfSenders( maxSender );
		return system;
	}

	@Override
	public Measure<AbCSystem> getMeasure(String name) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
