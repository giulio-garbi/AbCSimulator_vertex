/**
 * 
 */
package eu.quanticol.abcsimulator;

import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.math3.random.RandomGenerator;
import org.cmg.ml.sam.sim.Activity;
import org.cmg.ml.sam.sim.util.ComposedWeightedStructure;
import org.cmg.ml.sam.sim.util.WeightedElement;
import org.cmg.ml.sam.sim.util.WeightedStructure;

/**
 * @author loreti
 *
 */
public class AbCCluster extends AbCNode {
	
	protected int counter;
	protected LinkedList<AbCMessage> input;
	protected AbCClusterElement[] elements;
	private HashMap<Integer,ComponentNode> children;
	
	public AbCCluster(AbCSystem system, int id, int size) {
		super(system, id);
		this.counter = 0;
		this.input = new LinkedList<>();
		this.children = new HashMap<>();
		setUpCluster(size);
	}


	public void addChild(ComponentNode n) {
		this.children.put(n.getIndex(), n);
	}


	private void setUpCluster(int size) {
		this.elements = new AbCClusterElement[size];
		for(int i=0; i<size; i++) {
			this.elements[i] = new AbCClusterElement();
		}
	}



	@Override
	public void receive(AbCMessage message) {
		this.input.add(message);
	}

	@Override
	public WeightedStructure<Activity> getActivities(RandomGenerator r) {
		WeightedStructure<Activity> result = new ComposedWeightedStructure<>();
		for (AbCClusterElement n : elements) {
			result = result.add(n.getActivities(r));
		}
		for (ComponentNode n: children.values()) {
			result = result.add(n.getActivities(r));
		}
		return result;
	}

	@Override
	public LinkedList<Integer> getInputQueueSize(LinkedList<Integer> data) {
		data.add( input.size() );
		for (AbCNode n : children.values()) {
			n.getInputQueueSize(data);
		}
		return data;
	}

	@Override
	public LinkedList<Integer> getWaitingQueueSize(LinkedList<Integer> data) {
		for (AbCNode n : children.values()) {
			n.getWaitingQueueSize(data);
		}
		return data;
	}

	@Override
	public LinkedList<Integer> getOutputQueueSize(LinkedList<Integer> data) {
		for (AbCClusterElement e: elements) {
			data.add(e.outputQueue.size());
		}
		for (AbCNode n : children.values()) {
			n.getOutputQueueSize(data);
		}
		return data;
	}
	
	public class AbCClusterElement {
		
		private LinkedList<AbCMessage> outputQueue;

		public WeightedStructure<Activity> getActivities(RandomGenerator r) {
			WeightedStructure<Activity> result = new ComposedWeightedStructure<>();
			result = result.add(this.getSendingActivity());
			result = result.add(this.getMessageHandlingActivity());
			return result;
		}

		private WeightedStructure<Activity> getMessageHandlingActivity() {
				if (!input.isEmpty()) {
					AbCMessage message = input.peek();
					return new WeightedElement<Activity>( 
						getSystem().getMessageHandlingRate( AbCCluster.this ) , 
						new Activity() {

							@Override
							public String getName() {
								return message.toString();
							}

							@Override
							public boolean execute(RandomGenerator r) {
								input.remove(message);
								switch (message.getType()) {
								case DATA:
									handleDataPacket(message.getSource(), message.getMessageIndex());
									break;
								case ID_REQUEST:
									handleIndexRequest(message.getSource(), message.getRoute());	
									break;
								case ID_REPLY:
									break;
								default:
									break;
								}
								return true;
							}
							
						}
					);
				} else {
					return null;
				}
			}

		protected void handleIndexRequest(AbCNode source, LinkedList<Integer> route) {
			int message_index = counter++;	
			AbCNode to = children.get(route.pollLast());
			outputQueue.add( new AbCMessage(AbCCluster.this, MessageType.ID_REPLY, message_index, route, to));
		}

		protected void handleDataPacket(AbCNode from, int messageIndex) {
			for (AbCNode n : children.values()) {
				if (from != n) {
					outputQueue.add( new AbCMessage( AbCCluster.this , MessageType.DATA , messageIndex , null , n  ) );				
				}
			}
		}

		private WeightedStructure<Activity> getSendingActivity() {
			if (!outputQueue.isEmpty()) {
				AbCMessage message = outputQueue.peek();			
				return new WeightedElement<Activity>(
						getSystem().getSendRate(message.getSource(),message.getTarget()) , 
						new Activity() {

							@Override
							public String getName() {
								return message.toString();
							}

							@Override
							public boolean execute(RandomGenerator r) {
								outputQueue.remove(message);
								message.getTarget().receive(message);
								return true;
							}
							
						}
				);
			} else {
				return null;
			}
		}

	}	

}
