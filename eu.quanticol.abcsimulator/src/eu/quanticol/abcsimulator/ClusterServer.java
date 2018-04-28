/**
 * 
 */
package eu.quanticol.abcsimulator;

import java.util.ArrayList;
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
public class ClusterServer extends AbCNode {


	protected HashMap<Integer,AbCNode> children;
	
	protected int current_index;
	
	protected int next_index;
	
	protected ArrayList<PriorityQueue<AbCMessage>> waitingQueue;
	
	protected LinkedList<AbCMessage> inQueue;
	
	protected ArrayList<LinkedList<AbCMessage>> outQueue;

	private int size;
	
	public ClusterServer(AbCSystem system, int id, int size) {
		super(system, id);
		this.children = new HashMap<>();
		this.waitingQueue = new ArrayList<>();
		this.inQueue = new LinkedList<AbCMessage>();
		this.outQueue = new ArrayList<>();
		this.size = size;
		setUpWaitingQueues();
	}
	
	

	private void setUpWaitingQueues() {
		for( int i=0 ; i<this.size; i++) {
			this.waitingQueue.add(new PriorityQueue<AbCMessage>(10,new MessageComparator()));
			this.outQueue.add(new LinkedList<>());
		}
	}



	@Override
	public void receive(AbCMessage message) {
		inQueue.add(message);
	}

	@Override
	public WeightedStructure<Activity> getActivities(RandomGenerator r) {
		WeightedStructure<Activity> result = new ComposedWeightedStructure<>();
		for (AbCNode n : children.values()) {
			result = result.add(n.getActivities(r));
		}
		for (int i=0 ; i<waitingQueue.size(); i++ ) {
			result = result.add(this.getSendingActivity(i));
			result = result.add(this.getMessageHandlingActivity(i));
//			result = result.add(this.getHandlingWaitingMessagesActivity(i));
		}
		return result;
	}
	
	private WeightedStructure<Activity> getHandlingWaitingMessagesActivity( int k ) {
		if (!waitingQueue.get(k).isEmpty()) {
			AbCMessage message = waitingQueue.get(k).peek();
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
								waitingQueue.get(k).remove(message);
								handleDataPacket(message.getSource(), k,message);
								return true;
							}
							
						}
					);
			}
		} 
		return null;
	}
	
	private boolean nextIdReady(int k) {
		return (!waitingQueue.get(k).isEmpty())&&(waitingQueue.get(k).peek().getMessageIndex()==next_index);
	}

	private WeightedStructure<Activity> getMessageHandlingActivity(int k) {
		if (!inQueue.isEmpty()) { //&&!nextIdReady(k)) {
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
									//waitingQueue.get(k).add(message);
									handleDataPacket(message.getSource(), k,message);
									break;
								case ID_REQUEST:
									handleIndexRequest(k,message.getSource(), message.getRoute());	
									break;
								case ID_REPLY:
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
	
	
	protected void handleDataPacket( AbCNode from , int k, AbCMessage message ) {
//		if (this.next_index != index) {
//			System.out.println("SERVER: "+this.next_index+"->"+index);
//		}
//		if ((parent != null)&&(from != parent)) {
//			outQueue.add( new AbCMessage( this , MessageType.DATA , index , null , parent  ) );
//		}
		for (AbCNode n : children.values()) {
			if (from != n) {
				outQueue.get(k).add( new AbCMessage( this , MessageType.DATA , message.getMessageIndex() , message.getData(), null , n  ) );				
			}
		}
//		System.out.println(getIndex()+": "+this.next_index+"<->"+index);
		this.next_index = message.getMessageIndex()+1;
//		if (!waitingQueue.isEmpty()&&waitingQueue.peek().getMessageIndex()==next_index) {
//			inQueue.addFirst(waitingQueue.poll());
//		}
	}
	
	protected void handleIndexRequest( int k, AbCNode from , LinkedList<Integer> route ) {
		int message_index = this.current_index++;	
		AbCNode to = children.get(route.pollLast());
		outQueue.get(k).add( new AbCMessage(this, MessageType.ID_REPLY, message_index, null, route, to));
	}

	private WeightedStructure<Activity> getSendingActivity(int k) {
		if (!outQueue.get(k).isEmpty()) {
			AbCMessage message = outQueue.get(k).peek();			
			return new WeightedElement<Activity>(
					getSystem().getSendRate(message.getSource(),message.getTarget()) , 
					new Activity() {

						@Override
						public String getName() {
							return message.toString();
						}

						@Override
						public boolean execute(RandomGenerator r, double starting_time, double duration) {
							outQueue.get(k).remove(message);
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

	public String getInfo() {
		return "WAITING: "+waitingQueue+"\nINPUT: "+inQueue+"\nOUTPUT: "+outQueue;
	}

}
