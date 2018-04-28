/**
 * 
 */
package eu.quanticol.abcsimulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

import javax.print.DocFlavor.INPUT_STREAM;

import org.apache.commons.math3.random.RandomGenerator;
import org.cmg.ml.sam.sim.Activity;
import org.cmg.ml.sam.sim.util.ComposedWeightedStructure;
import org.cmg.ml.sam.sim.util.WeightedElement;
import org.cmg.ml.sam.sim.util.WeightedStructure;

/**
 * @author loreti
 *
 */
public class RingNode extends AbCNode {

	protected ArrayList<RingNode> ring;
	
	protected HashMap<Integer,ComponentNode> children;
	
	protected SharedCounter counter;
	
	protected int next_index;

	protected PriorityQueue<AbCMessage> waitingQueue;
	
	protected LinkedList<AbCMessage> inQueue;
	
	protected LinkedList<AbCMessage> outQueue;
	
	protected HashSet<Integer> myIndexes;

	private RingNode next;
	
	public RingNode(AbCSystem system, int id, SharedCounter counter, ArrayList<RingNode> ring) {
		super(system, id);
		this.children = new HashMap<>();
		this.counter = counter;
		this.waitingQueue = new PriorityQueue<AbCMessage>(10,new MessageComparator());
		this.inQueue = new LinkedList<AbCMessage>();
		this.outQueue = new LinkedList<>();
		this.myIndexes = new HashSet<>();
		this.ring = ring;
	}

	@Override
	public void receive(AbCMessage message) {
		inQueue.add(message);
	}

	@Override
	public WeightedStructure<Activity> getActivities(RandomGenerator r) {
		WeightedStructure<Activity> result = new ComposedWeightedStructure<>();
		for (RingNode n : ring) {
			result = n.getActivities(r, result);
		}
		return result;
	}

	private WeightedStructure<Activity> getActivities(RandomGenerator r, WeightedStructure<Activity> result) {
		for (AbCNode n : children.values()) {
			result = result.add(n.getActivities(r));
		}
		result = result.add(this.getSendingActivity());
		result = result.add(this.getMessageHandlingActivity());
		result = result.add(this.getHandlingWaitingMessagesActivity());
		return result;
	}

	private WeightedStructure<Activity> getHandlingWaitingMessagesActivity() {
		if (!waitingQueue.isEmpty()) {
			AbCMessage message = waitingQueue.peek();
			if (message.getMessageIndex()==next_index) {
				return new WeightedElement<Activity>( 
						getSystem().getMessageHandlingRate( this ) , 
						new Activity() {

							@Override
							public String getName() {
								return message.toString();
							}

							@Override
							public boolean execute(RandomGenerator r, double starting_time, double duration) {
								waitingQueue.remove(message);
								handleDataPacket(message.getSource(), message);
								return true;
							}
							
						}
					);
			}
		} 
		return null;
	}
	
	private boolean nextIdReady() {
		return (!waitingQueue.isEmpty())&&(waitingQueue.peek().getMessageIndex()==next_index);
	}

	private WeightedStructure<Activity> getMessageHandlingActivity() {
		if (!inQueue.isEmpty()&&!nextIdReady()) {
			AbCMessage message = inQueue.peek();
				return new WeightedElement<Activity>( 
						getSystem().getMessageHandlingRate( this ) , 
						new Activity() {

							@Override
							public String getName() {
								return message.toString();
							}

							@Override
							public boolean execute(RandomGenerator r, double starting_time, double duration) {
								inQueue.remove(message);
								switch (message.getType()) {
								case DATA:
									if (children.keySet().contains(message.getSource().getIndex())) {
										if (next != null) {
											outQueue.add( new AbCMessage( RingNode.this , MessageType.DATA , message.getMessageIndex() , message.getData(), null , next  ) );
										}
										waitingQueue.add(message);
									} else {
										if (!myIndexes.contains(message.getMessageIndex())) {
											if (next != null) {
												outQueue.add( new AbCMessage( RingNode.this , MessageType.DATA , message.getMessageIndex() , message.getData(), null , next  ) );
											}
											waitingQueue.add(message);
										}
									}
									break;
								case ID_REQUEST:
									handleIndexRequest(message.getSource(), message.getRoute());	
									break;
								case ID_REPLY:
									handleIndexReply(message.getMessageIndex(), message.getRoute());
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
	
	
	protected void handleDataPacket( AbCNode from , AbCMessage message ) {
		for (AbCNode n : children.values()) {
			if (from != n) {
				outQueue.add( new AbCMessage( this , MessageType.DATA , message.getMessageIndex(), message.getData() , null , n  ) );				
			}
		}
		this.next_index = message.getMessageIndex()+1;
	}
	
	protected void handleIndexRequest( AbCNode from , LinkedList<Integer> route ) {
		int message_index = this.counter.getAndUpdate();	
		AbCNode to = children.get(route.pollLast());
		this.myIndexes.add(message_index);
		outQueue.add( new AbCMessage(this, MessageType.ID_REPLY, message_index, null, route, to));
	}

	protected void handleIndexReply( int index , LinkedList<Integer> route ) {
		AbCNode to = children.get(route.pollLast());
		outQueue.add( new AbCMessage(this, MessageType.ID_REPLY, index, null, route, to));
	}

	private WeightedStructure<Activity> getSendingActivity() {
		if (!outQueue.isEmpty()) {
			AbCMessage message = outQueue.peek();			
			return new WeightedElement<Activity>(
					getSystem().getSendRate(message.getSource(),message.getTarget()) , 
					new Activity() {

						@Override
						public String getName() {
							return message.toString();
						}

						@Override
						public boolean execute(RandomGenerator r, double starting_time, double duration) {
							outQueue.remove(message);
							message.getTarget().receive(message);
							return true;
						}
						
					}
			);
		} else {
			return null;
		}
	}
	
	@Override
	public LinkedList<Integer> getInputQueueSize( LinkedList<Integer> data ) {
		data.add( inQueue.size() );
		for (AbCNode n : children.values()) {
			n.getInputQueueSize(data);
		}
		return data;
	}

	@Override
	public LinkedList<Integer> getWaitingQueueSize(LinkedList<Integer> data) {
		data.add( waitingQueue.size() );
		for (AbCNode n : children.values()) {
			n.getWaitingQueueSize(data);
		}
		return data;
	}

	@Override
	public LinkedList<Integer> getOutputQueueSize(LinkedList<Integer> data) {
		data.add( outQueue.size() );
		for (AbCNode n : children.values()) {
			n.getOutputQueueSize(data);
		}
		return data;
	}

	public void addChild(ComponentNode n) {
		this.children.put(n.getIndex(), n);
	}

	
	public String getInfo() {
		return "WAITING: "+waitingQueue+"\nINPUT: "+inQueue+"\nOUTPUT: "+outQueue;
	}

	protected void setNext( RingNode next ) {
		this.next = next;
	}
		
	

}
