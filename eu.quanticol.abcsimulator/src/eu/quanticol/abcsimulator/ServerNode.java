/**
 * 
 */
package eu.quanticol.abcsimulator;

import java.util.HashMap;
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
public class ServerNode extends AbCNode {


	protected AbCNode parent;
	
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
//									handleDataPacket(message.getSource(), message.getMessageIndex());
//									if (message.getMessageIndex()==next_index) {
//										handleDataPacket(message.getSource(), message.getMessageIndex());
//									} else {
//										waitingQueue.add(message);
//									}
//									if (waitingQueue.size()>4) {
//										System.out.println("WQ SIZE AT: "+getIndex()+": "+waitingQueue.size());
//									}
									if ((parent != null)&&(message.getSource() != parent)) {
										outQueue.add( new AbCMessage( ServerNode.this , MessageType.DATA , message.getMessageIndex() , message.getData(), null , parent  ) );
									}									
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
	
	
	protected void handleDataPacket( AbCNode from , AbCMessage message ) {
//		if (this.next_index != index) {
//			System.out.println("SERVER: "+this.next_index+"->"+index);
//		}
//		if ((parent != null)&&(from != parent)) {
//			outQueue.add( new AbCMessage( this , MessageType.DATA , index , null , parent  ) );
//		}
		for (AbCNode n : children.values()) {
			if (from != n) {
				outQueue.add( new AbCMessage( this , MessageType.DATA , message.getMessageIndex(), message.getData() , null , n  ) );				
			}
		}
//		System.out.println(getIndex()+": "+this.next_index+"<->"+index);
		this.next_index = message.getMessageIndex()+1;
//		if (!waitingQueue.isEmpty()&&waitingQueue.peek().getMessageIndex()==next_index) {
//			inQueue.addFirst(waitingQueue.poll());
//		}
	}
	
	protected void handleIndexRequest( AbCNode from , LinkedList<Integer> route ) {
		if (parent != null) {
			route.add(this.getIndex());
			outQueue.add( new AbCMessage(this, MessageType.ID_REQUEST, -1, null, route, parent));
		} else {
			int message_index = this.current_index++;	
			AbCNode to = children.get(route.pollLast());
			outQueue.add( new AbCMessage(this, MessageType.ID_REPLY, message_index, null, route, to));
		}
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
//							System.out.println("SENDING "+message.getMessageIndex());
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

	protected void setParent( AbCNode parent ) {
		this.parent = parent;
	}
	
	public String getInfo() {
		return "WAITING: "+waitingQueue+"\nINPUT: "+inQueue+"\nOUTPUT: "+outQueue;
	}

}
