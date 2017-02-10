/**
 * 
 */
package eu.quanticol.abcsimulator;

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
public class ComponentNode extends AbCNode {

	private boolean isSending = false;
	
	private int nextId;
	
	private LinkedList<AbCMessage> inQueue;
	
	
	public ComponentNode(AbCSystem system, int id, AbCNode parent) {
		super(system, id);
		setParent( parent );
		inQueue = new LinkedList<>();
	}

	@Override
	public void receive(AbCMessage message) {
		inQueue.add(message);
	}
	
	@Override
	public WeightedStructure<Activity> getActivities(RandomGenerator r) {
		WeightedStructure<Activity> result = new ComposedWeightedStructure<>();
		result = result.add(getReceivingActivity());
		if ((!isSending)&&(getSystem().canSend())) {
			result = result.add( getSendRequestActivity() );
		}
		if ((isSending)&&(nextId!=-1)) {
			result = result.add( getSendDataActivity() );
		}
		return result;
	}

	private WeightedStructure<Activity> getSendDataActivity() {
		return new WeightedElement<Activity>( 
				getSystem().getSendRate( this , parent ), 
				new Activity() {

					@Override
					public String getName() {
						return this.toString()+"!";
					}

					@Override
					public boolean execute(RandomGenerator r) {
						getSystem().sendingDone();
						getSystem().dataSent( ComponentNode.this, ComponentNode.this.nextId);
						parent.receive( new AbCMessage(ComponentNode.this, MessageType.DATA, ComponentNode.this.nextId, null, parent));
						isSending = false;
						nextId = -1;
						return true;
					}
					
				}
			);
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
					public boolean execute(RandomGenerator r) {
						getSystem().addSender();
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
					public boolean execute(RandomGenerator r) {
						inQueue.remove(message);
						if ((message.getType()==MessageType.ID_REPLY)&&isSending) {
							ComponentNode.this.nextId = message.getMessageIndex();
						}
						if (message.getType()==MessageType.DATA) {
							getSystem().dataReceived( ComponentNode.this , message.getMessageIndex() );
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

}
