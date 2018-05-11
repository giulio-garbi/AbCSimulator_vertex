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
	
	private final ComponentBehaviour behaviour;
	private boolean justStarted;
	private boolean requestsToMake;
	private boolean pendingRequest;
	//private final int nodeId;
	
	protected AbCNode parent;
	
	public ComponentNode(AbCSystem system, int id, AbCNode parent, int nodeId, int[] N) {
		super(system, id);
		setParent( parent );
		inQueue = new LinkedList<>();
		behaviour = new GraphVertex(nodeId, N);
		//behaviour = new Counting(nodeId);
		justStarted = true;
		requestsToMake = false;
		pendingRequest = false;
		//this.nodeId = nodeId;
		this.deliveryQueue = new PriorityQueue<AbCMessage>(10,new MessageComparator());
	}

	/*public ComponentNode(AbCSystem system, int id, AbCNode parent) {
		this(system,id,parent,false);
	}*/

	@Override
	public void receive(AbCMessage message) {
		inQueue.add(message);
	}
	
	@Override
	public WeightedStructure<Activity> getActivities(RandomGenerator r) {
		WeightedStructure<Activity> result = new ComposedWeightedStructure<>();
		result = result.add(getReceivingActivity());

		if(justStarted) {
			if (!deliveryQueue.isEmpty()&&(deliveryQueue.peek().getMessageIndex()==lastReceived+1)) {
				result = result.add( getHandlingWaitingMessageActivity() );
			} else {
				requestsToMake |= behaviour.onStart();
				justStarted = false;
			}
		} else {		
			if (!deliveryQueue.isEmpty()&&(deliveryQueue.peek().getMessageIndex()==lastReceived+1)) {
				result = result.add( getHandlingWaitingMessageActivity() );
			}
		}
		
		if ((!isSending)&&(requestsToMake)&&(!pendingRequest)) {
			result = result.add( getSendRequestActivity() );
		}
		
		if ((isSending)&&(nextId!=-1)) {
			result = result.add( getSendDataActivity() );
		}
		/*
		
		if ((!isSending)&&true) {//(nodeId == receives%155)&&(!justAsked)) {
			//System.out.println("Sending "+nodeId+" "+justAsked);
			//System.out.println("Sra "+nodeId);
			result = result.add( getSendRequestActivity() );
			//justAsked = true;
		}
		if ((isSending)&&(nextId!=-1)) {
			//System.out.println("Sda "+nodeId);
			result = result.add( getSendDataActivity() );
		}
		if (!deliveryQueue.isEmpty()&&(deliveryQueue.peek().getMessageIndex()==lastReceived+1)) {
			//receives++;
			AbCMessage message = deliveryQueue.poll();
			if(gotMsg.contains(message.getMessageIndex()))
				throw new Error("Delivered twice!");
			gotMsg.add(message.getMessageIndex());
			//justAsked = false;
			result = result.add( getHandlingWaitingMessageActivity() );
			System.out.println("Got "+nodeId+" w "+result.getTotalWeight());
		}*/
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
							//System.out.println("Sda "+nodeId+" !");
							getSystem().dataSent( ComponentNode.this, ComponentNode.this.nextId,startSendingTime);
							ComponentBehaviour.GetMessage answ = behaviour.getMessage();
							requestsToMake = answ.wantSend;
							pendingRequest = false;
							Object[] data = answ.message == null ? null : answ.message.getData();
							parent.receive( new AbCMessage(ComponentNode.this, MessageType.DATA, ComponentNode.this.nextId, data, null, parent));
							isSending = false;
							myIndexes.add(nextId);
							updateLastReceivedIndex( lastReceived );
							nextId = -1;
							return true;
						}
						
					}
				);
		} else {
			//System.out.println("Sda "+nodeId+" null");
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
						//System.out.println("Sra "+nodeId+" !");
						requestsToMake = false;
						pendingRequest = true;
						startSendingTime = starting_time;
						ComponentNode.this.isSending = true;
						ComponentNode.this.nextId = -1;
						LinkedList<Integer> route = new LinkedList<>();
						route.add(getIndex());
						AbCMessage message = new AbCMessage(ComponentNode.this, MessageType.ID_REQUEST, -1, null, route, parent);
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
						//System.out.println("hwma "+nodeId);
						AbCMessage message = deliveryQueue.poll();
						updateLastReceivedIndex( message.getMessageIndex() );
						if(message.getData() != null) {
							if(justStarted) {
								requestsToMake |= behaviour.onStart(ComponentMessage.from(message));
								justStarted = false;
							} else {
								requestsToMake |= behaviour.onMessage(ComponentMessage.from(message));
							}
						}
						
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
