/**
 * 
 */
package eu.quanticol.abcsimulator;

import java.util.HashSet;
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
public class ComponentNode extends AbCNode {
	
	private int GAP_SIZE = 1;

	private boolean isSending = false;
	
	private int nextId;
	
	private LinkedList<AbCMessage> inQueue;
	
	protected PriorityQueue<AbCMessage> deliveryQueue;
	
	protected HashSet<Integer> myIndexes = new HashSet<>();
	
	private int lastReceived = -1;
	
	private double startSendingTime;
	
	private double previousMessageTime = -1;
	
	private boolean isASender;

	protected AbCNode parent;
	
	public ComponentNode(AbCSystem system, int id, AbCNode parent,boolean isASender) {
		super(system, id);
		setParent( parent );
		inQueue = new LinkedList<>();
		this.isASender = isASender;
		this.deliveryQueue = new PriorityQueue<AbCMessage>(10,new MessageComparator());
	}

	public ComponentNode(AbCSystem system, int id, AbCNode parent) {
		this(system,id,parent,false);
	}

	@Override
	public void receive(AbCMessage message) {
		inQueue.add(message);
	}
	
	@Override
	public WeightedStructure<Activity> getActivities(RandomGenerator r) {
		WeightedStructure<Activity> result = new ComposedWeightedStructure<>();
		result = result.add(getReceivingActivity());
		if ((!isSending)&&(isASender)) {
			result = result.add( getSendRequestActivity() );
		}
		if ((isSending)&&(nextId!=-1)) {
			result = result.add( getSendDataActivity() );
		}
		if (!deliveryQueue.isEmpty()&&(deliveryQueue.peek().getMessageIndex()==lastReceived+1)) {
			result = result.add( getHandlingWaitingMessageActivity() );
		}
		return result;
	}

	private WeightedStructure<Activity> getSendDataActivity() {
		if (this.lastReceived >= this.nextId-GAP_SIZE) {
			return new WeightedElement<Activity>( 
					getSystem().getSendRate( this , parent ), 
					new Activity() {

						@Override
						public String getName() {
							return this.toString()+"!";
						}

						@Override
						public boolean execute(RandomGenerator r, double starting_time, double duration) {
							getSystem().dataSent( ComponentNode.this, ComponentNode.this.nextId,startSendingTime);
							parent.receive( new AbCMessage(ComponentNode.this, MessageType.DATA, ComponentNode.this.nextId, null, parent));
							isSending = false;
							myIndexes.add(nextId);
							updateLastReceivedIndex( lastReceived );
							nextId = -1;
							return true;
						}
						
					}
				);
		} else {
			return null;
		}
	}

	protected void updateLastReceivedIndex( int newIndex ) {
		int size = myIndexes.size();
		for(int idx = 0; idx<size;idx++) {
			if (!myIndexes.contains(newIndex+idx+1)) {
				lastReceived = newIndex+idx;
				return ;
			}
			myIndexes.remove(newIndex+idx+1);
		}
		lastReceived = newIndex+size;
	}

	private WeightedStructure<Activity> getSendRequestActivity() {
		return new WeightedElement<Activity>( 
				getSystem().getDataRate( this ), 
				new Activity() {

					@Override
					public String getName() {
						return this.toString()+"!";
					}

					@Override
					public boolean execute(RandomGenerator r, double starting_time, double duration) {
						startSendingTime = starting_time;
						ComponentNode.this.isSending = true;
						ComponentNode.this.nextId = -1;
						LinkedList<Integer> route = new LinkedList<>();
						route.add(getIndex());
						AbCMessage message = new AbCMessage(ComponentNode.this, MessageType.ID_REQUEST, -1, route, parent);
						parent.receive( message);
						return true;
					}
					
				}
			);

	}

	private WeightedStructure<Activity> getReceivingActivity() {
		if (!inQueue.isEmpty()) {
			AbCMessage message = inQueue.peek();
			return new WeightedElement<Activity>(
				getSystem().getMessageHandlingRate(this) , 					
				new Activity() {

					@Override
					public String getName() {
						return ComponentNode.this+": "+message;
					}

					@Override
					public boolean execute(RandomGenerator r, double starting_time, double duration) {
						inQueue.remove(message);
						if ((message.getType()==MessageType.ID_REPLY)&&isSending) {
							ComponentNode.this.nextId = message.getMessageIndex();
							getSystem().registerWaitingTime((starting_time+duration)-startSendingTime);
						}
						if (message.getType()==MessageType.DATA) {
							deliveryQueue.add(message);
						}
						return true;
					}
					
				}
			);
		}
		return null;
	}

	@Override
	public LinkedList<Integer> getInputQueueSize(LinkedList<Integer> data) {
		return data;
	}

	@Override
	public LinkedList<Integer> getWaitingQueueSize(LinkedList<Integer> data) {
		return data;
	}

	@Override
	public LinkedList<Integer> getOutputQueueSize(LinkedList<Integer> data) {
		return data;
	}

	protected void setParent( AbCNode parent ) {
		this.parent = parent;
	}

	public boolean isASender() {
		return this.isASender;
	}

	public void setSender(boolean b) {
		this.isASender = b;
	}


	private WeightedStructure<Activity> getHandlingWaitingMessageActivity() {
		return new WeightedElement<Activity>( 
				getSystem().getMessageHandlingRate(this) , 
				new Activity() {

					@Override
					public String getName() {
						return this.toString()+"!";
					}

					@Override
					public boolean execute(RandomGenerator r, double starting_time, double duration) {
						AbCMessage message = deliveryQueue.poll();
						updateLastReceivedIndex( message.getMessageIndex() );							
						getSystem().dataReceived( ComponentNode.this , message.getMessageIndex(), starting_time+duration );
						if (previousMessageTime>=0) {
							getSystem().registerMessageInterval(starting_time+duration-previousMessageTime);
						} 
						previousMessageTime = starting_time+duration;
						return true;
					}
					
				}
			);

	}

	
	

}
