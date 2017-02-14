/**
 * 
 */
package eu.quanticol.abcsimulator;

import java.util.LinkedList;
import java.util.function.BiFunction;
import java.util.function.Function;

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
	private int maxSender;
	private int levels;
	private int children;
	
	public TreeStructureFactory(
			int levels, //Tree levels
			int children, //Number of children for each node 
			int agents, //Number of leaves for each node at the last level
			int maxSender, //Max number of senders at the same time (-1 is unbound)
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
		this.maxSender = maxSender;
 	}	

	@Override
	public AbCSystem getModel() {
		AbCSystem system = new AbCSystem();	
		ServerNode root = new ServerNode(system, 0);
		int counter = 1;
		LinkedList<ServerNode> level = new LinkedList<>();
		level.add(root);
		LinkedList<ServerNode> lvl=level;
		for( int l=1 ; l<levels ; l++ ) {
			LinkedList<ServerNode> nextLevel = new LinkedList<>();
			for (ServerNode parent : lvl) {
				for( int i=0 ; i<children ; i++ ) {
					ServerNode n = new ServerNode(system, counter++);
					n.setParent(parent);
					parent.addChild(n);
					nextLevel.add(n);
				}
				
			}
			lvl=nextLevel;
			for (ServerNode node : lvl) {
				System.out.print(node.getIndex()+" ");	
			}
			System.out.println();
		}

		for (ServerNode parent : lvl) {
			for( int i=0 ; i<agents ; i++ ) {
				ComponentNode n = new ComponentNode(system, counter+i, parent);
				System.out.print(parent.getIndex()+" ");
				parent.addChild( n );
			}
		}
		System.out.println();
		system.setRoot( root );
		system.setDataRate(dataRate);
		system.setSendingRate(sendingRate);
		system.setHandlingRate(handlingRate);
		system.setAgents(level.size()*agents);
		system.setMaxNumberOfSenders( maxSender );
		return system;
	}

	@Override
	public Measure<AbCSystem> getMeasure(String name) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
