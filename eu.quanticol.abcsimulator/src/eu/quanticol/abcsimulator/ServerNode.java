/**
 * 
 */
package eu.quanticol.abcsimulator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

import org.apache.commons.math3.random.RandomGenerator;
import org.cmg.ml.sam.sim.Activity;
import org.cmg.ml.sam.sim.util.ComposedWeightedStructure;
import org.cmg.ml.sam.sim.util.WeightedElement;
import org.cmg.ml.sam.sim.util.WeightedStructure;

/**
 * @author loreti
 *
 */
public class ServerNode extends AbCNode {

	protected HashMap<Integer,AbCNode> children;
	
	protected int current_index;
	
	protected int next_index;
	
	protected PriorityQueue<AbCMessage> waitingQueue;
	
	protected LinkedList<AbCMessage> inQueue;
	
	protected LinkedList<AbCMessage> outQueue;
	
	public ServerNode(AbCSystem system, int id) {
		super(system, id);
		this.children = new HashMap<>();
		this.parent = null;
		this.waitingQueue = new PriorityQueue<AbCMessage>(10,new MessageComparator());
		this.inQueue = new LinkedList<AbCMessage>();
		this.outQueue = new LinkedList<>();
	}

	@Override
	public void receive(AbCMessage message) {
//		if (next_index < message.getMessageIndex()) {
//			waitingQueue.add(message);
//		} else {
//			inQueue.add(message);
//		}
		inQueue.add(message);
	}

	@Override
	public WeightedStructure<Activity> getActivities(RandomGenerator r) {
		WeightedStructure<Activity> result = new ComposedWeightedStructure<>();
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
			return new WeightedElement<Activity>( 
				getSystem().getMessageHandlingRate( this ) , 
				new Activity() {

					@Override
					public String getName() {
						return message.toString();
					}

					@Override
					public boolean execute(RandomGenerator r) {
						waitingQueue.remove(message);
						handleDataPacket(message.getSource(), message.getMessageIndex());
						return true;
					}
					
				}
			);
		} else {
			return null;
		}
	}

	private WeightedStructure<Activity> getMessageHandlingActivity() {
		if (!inQueue.isEmpty()) {
			AbCMessage message = inQueue.peek();
			return new WeightedElement<Activity>( 
				getSystem().getMessageHandlingRate( this ) , 
				new Activity() {

					@Override
					public String getName() {
						return message.toString();
					}

					@Override
					public boolean execute(RandomGenerator r) {
						inQueue.remove(message);
						switch (message.getType()) {
						case DATA:
							//handleDataPacket(message.getSource(), message.getMessageIndex());
							waitingQueue.add(message);
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
	
	
	protected void handleDataPacket( AbCNode from , int index ) {
		if ((parent != null)&&(from != parent)) {
			outQueue.add( new AbCMessage( this , MessageType.DATA , index , null , parent  ) );
		}
		for (AbCNode n : children.values()) {
			if (from != n) {
				outQueue.add( new AbCMessage( this , MessageType.DATA , index , null , n  ) );				
			}
		}
		this.next_index = index+1;
	}
	
	protected void handleIndexRequest( AbCNode from , LinkedList<Integer> route ) {
		if (parent != null) {
			route.add(this.getIndex());
			outQueue.add( new AbCMessage(this, MessageType.ID_REQUEST, -1, route, parent));
		} else {
			int message_index = this.current_index++;	
			AbCNode to = children.get(route.pollLast());
			outQueue.add( new AbCMessage(this, MessageType.ID_REPLY, message_index, route, to));
		}
	}

	protected void handleIndexReply( int index , LinkedList<Integer> route ) {
		AbCNode to = children.get(route.pollLast());
		outQueue.add( new AbCMessage(this, MessageType.ID_REPLY, index, route, to));
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
						public boolean execute(RandomGenerator r) {
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
	
//	private void handleWaitingMessages() {
//		if (!this.waitingQueue.isEmpty()) {
//			AbCMessage message = this.waitingQueue.peek();
//			if (message.getMessageIndex()==this.next_index) {
//				this.waitingQueue.poll();
//				this.inQueue.add(message);
//			}
//		}
//	}

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

	public void addChild(AbCNode n) {
		this.children.put(n.getIndex(), n);
	}

	
	public String getInfo() {
		return "WAITING: "+waitingQueue+"\nINPUT: "+inQueue+"\nOUTPUT: "+outQueue;
	}

}
