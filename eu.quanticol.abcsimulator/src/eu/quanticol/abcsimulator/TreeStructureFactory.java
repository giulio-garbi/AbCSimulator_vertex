/**
 * 
 */
package eu.quanticol.abcsimulator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
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
public class TreeStructureFactory implements SimulationFactory<AbCSystem>{
	
	protected int agents;
	private BiFunction<AbCNode, AbCNode, Double> sendingRate;
	private Function<AbCNode, Double> handlingRate;
	private Function<AbCNode, Double> dataRate;
	private int levels;
	private int children;
	private int[][] graph;
	
	public TreeStructureFactory(
			int levels, //Tree levels
			int children, //Number of children for each node 
			int agents, //Number of leaves for each node at the last level
			float p, 
			//int maxSender, //Max number of senders at the same time (-1 is unbound)
		BiFunction<AbCNode, AbCNode, Double> sendingRate,
		Function<AbCNode, Double> handlingRate,
		Function<AbCNode, Double> dataRate		
	) {
		this.levels = levels;
		this.children = children;
		this.agents = agents;
		this.sendingRate = sendingRate;
		this.handlingRate = handlingRate;
		this.dataRate = dataRate;
		
		int seed = 1;
		int leafs = (int)Math.pow(children, levels);
		this.graph = GraphVertex.makeGraph(new Random(seed), leafs*agents, p);
 	}	

	@Override
	public AbCSystem getModel() {
		AbCSystem system = new AbCSystem();	
		ServerNode root = new ServerNode(system, 0);
		int counter = 1;
		LinkedList<ServerNode> level = new LinkedList<>();
		level.add(root);
		for( int l=1 ; l<levels ; l++ ) {
//			System.out.println(level);
			LinkedList<ServerNode> nextLevel = new LinkedList<>();
			for (ServerNode parent : level) {
				for( int i=0 ; i<children ; i++ ) {
					ServerNode n = new ServerNode(system, counter++);
					n.setParent(parent);
					parent.addChild(n);
					nextLevel.add(n);
				}
				
			}
			level=nextLevel;
//			for (ServerNode node : level) {
//				System.out.print(node.getIndex()+" ");	
//			}
//			System.out.println();
		}
//		System.out.println(level);

		RandomGenerator r = RandomGeneratorRegistry.getInstance().get();
		
		ArrayList<ComponentNode> nodes = new ArrayList<>();		
		for (ServerNode parent : level) {
			for( int i=0 ; i<agents ; i++ ) {
				ComponentNode n = new ComponentNode(system, counter+i, parent, i, this.graph[i]);
				nodes.add(n);
//				System.out.print(parent.getIndex()+" ");
				parent.addChild( n );
			}
		}
		
		system.setRoot( root );
		system.setDataRate(dataRate);
		system.setSendingRate(sendingRate);
		system.setHandlingRate(handlingRate);
		system.setAgents(level.size()*agents);
//		system.setMaxNumberOfSenders( maxSender );
		return system;
	}

	@Override
	public Measure<AbCSystem> getMeasure(String name) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
