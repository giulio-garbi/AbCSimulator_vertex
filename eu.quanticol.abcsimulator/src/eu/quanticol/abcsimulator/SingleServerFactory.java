/**
 * 
 */
package eu.quanticol.abcsimulator;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.math3.random.RandomGenerator;
import org.cmg.ml.sam.sim.RandomGeneratorRegistry;
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
	private int clusterSize;
	
	public SingleServerFactory(int agents,
			int maxSender,
			int clusterSize,
		BiFunction<AbCNode, AbCNode, Double> sendingRate,
		Function<AbCNode, Double> handlingRate,
		Function<AbCNode, Double> dataRate		
	) {
		this.agents = agents;
		this.sendingRate = sendingRate;
		this.handlingRate = handlingRate;
		this.dataRate = dataRate;
		this.maxSender = maxSender;
		this.clusterSize = clusterSize;
 	}	

	@Override
	public AbCSystem getModel() {
		AbCSystem system = new AbCSystem();
		
		ClusterServer root = new ClusterServer(system, 0,clusterSize);

		
		RandomGenerator r = RandomGeneratorRegistry.getInstance().get();
		
		ArrayList<ComponentNode> nodes = new ArrayList<>();		
		for( int i=0 ; i<agents ; i++ ) {
			ComponentNode n = new ComponentNode(system, i+1, root,maxSender<0);
			root.addChild( n );
			nodes.add(n);
		}

		if (maxSender>0) {
			int senderCounter = 0;
			while( senderCounter < maxSender ) {
				int id = r.nextInt(nodes.size());
				ComponentNode n = nodes.get(id);
				if (!n.isASender()) {
					n.setSender( true );
					senderCounter++;
				}
			}
		}
		

		system.setRoot( root );
		system.setDataRate(dataRate);
		system.setSendingRate(sendingRate);
		system.setHandlingRate(handlingRate);
		system.setAgents(agents);
		return system;
	}

	@Override
	public Measure<AbCSystem> getMeasure(String name) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
