/**
 * 
 */
package eu.quanticol.abcsimulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.cmg.ml.sam.sim.Activity;
import org.cmg.ml.sam.sim.ModelI;
import org.cmg.ml.sam.sim.util.WeightedLinkedList;
import org.cmg.ml.sam.sim.util.WeightedStructure;
/**
 * @author loreti
 *
 */
public class AbCSystem implements ModelI {
		
	protected AbCNode root;
	protected BiFunction<AbCNode, AbCNode, Double> sendingRate;
	protected Function<AbCNode, Double> handlingRate;
	protected Function<AbCNode, Double> dataRate;

	SummaryStatistics deliveryStats = new SummaryStatistics();
	SummaryStatistics waitingIdStats = new SummaryStatistics();
	SummaryStatistics totalSendingTime = new SummaryStatistics();
	SummaryStatistics messageInterval = new SummaryStatistics();
	
	
	
	protected double time;
	
	private int agents;
	
	protected final HashMap<Integer,Double> sendingTime;
	
	protected final HashMap<Integer,Integer> deliveryTable;
	
	protected final HashMap<Integer,Double> deliveryTime;
	
	private double lastDeliveryTime = -1;
//	private int sender;
//	
//	private int maxSenders = Integer.MAX_VALUE;

	public AbCSystem() {
		this.time = 0.0;
		this.root = null;
		this.sendingTime = new HashMap<>();
		this.deliveryTable = new HashMap<>();
		this.deliveryTime = new HashMap<>();
		this.lastDeliveryTime = -1;
	}

	@Override
	public WeightedStructure<Activity> getActivities(RandomGenerator r) {
		if (this.root != null) {
			return this.root.getActivities(r);
		}
		return new WeightedLinkedList<Activity>();
	}

	@Override
	public void timeStep(double dt) {
		time += dt;
		//		System.out.println("Time: "+time+" Sender: "+sender+" Delivering: "+this.deliveryTable.size()+" Delivered: "+this.deliveryTime.size());
	}

	public double getSendRate(AbCNode source, AbCNode target) {
		return sendingRate.apply(source, target);
	}

	public double getMessageHandlingRate(AbCNode node) {
		return handlingRate.apply(node);
	}

	public double getDataRate(AbCNode node) {
		return dataRate.apply(node);
	}
	
	public void dataReceived(ComponentNode n, int index, double time) {
		int c = deliveryTable.get(index)+1;
		if (c == agents) {
			double t0 = sendingTime.get(index);
			double delta = time-t0;
			lastDeliveryTime = Math.max(lastDeliveryTime, time);
			deliveryStats.addValue(delta);
			deliveryTime.put(index, delta);
			deliveryTable.remove(index);
			sendingTime.remove(index);
//				System.out.println(time+"> "+lastDelivered+"<-"+delta+" ["+deliveryTable.size()+"]");
		} else {
			deliveryTable.put(index, c);
		//	System.out.println(deliveryTable);
		}
	}

	public void dataSent(ComponentNode n, int index, double time) {
		deliveryTable.put(index, 1);
		sendingTime.put(index, time);
//		System.out.println(index+"->"+time);
//		System.out.println("Sent: "+index);
	}
	
	public void registerWaitingTime( double time ) {
		waitingIdStats.addValue(time);
	}

	
	
	public LinkedList<Integer> getInputQueueSize() {
		return root.getInputQueueSize( new LinkedList<Integer>() );
	}

	public LinkedList<Integer> getWaitingQueueSize() {
		return root.getWaitingQueueSize( new LinkedList<Integer>() );
	}
	
	public LinkedList<Integer> getOutputQueueSize() {
		return root.getOutputQueueSize( new LinkedList<Integer>() );
	}

	public void setRoot(ServerNode root) {
		this.root = root;
	}

	/**
	 * @return the root
	 */
	public AbCNode getRoot() {
		return root;
	}

	/**
	 * @param root the root to set
	 */
	public void setRoot(AbCNode root) {
		this.root = root;
	}

	/**
	 * @return the sendingRate
	 */
	public BiFunction<AbCNode, AbCNode, Double> getSendingRate() {
		return sendingRate;
	}

	/**
	 * @param sendingRate the sendingRate to set
	 */
	public void setSendingRate(BiFunction<AbCNode, AbCNode, Double> sendingRate) {
		this.sendingRate = sendingRate;
	}

	/**
	 * @return the handlingRate
	 */
	public Function<AbCNode, Double> getHandlingRate() {
		return handlingRate;
	}

	/**
	 * @param handlingRate the handlingRate to set
	 */
	public void setHandlingRate(Function<AbCNode, Double> handlingRate) {
		this.handlingRate = handlingRate;
	}

	/**
	 * @return the dataRate
	 */
	public Function<AbCNode, Double> getDataRate() {
		return dataRate;
	}

	/**
	 * @param dataRate the dataRate to set
	 */
	public void setDataRate(Function<AbCNode, Double> dataRate) {
		this.dataRate = dataRate;
	}

	/**
	 * @return the time
	 */
	public double getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(double time) {
		this.time = time;
	}

	/**
	 * @return the agents
	 */
	public int getAgents() {
		return agents;
	}

	/**
	 * @param agents the agents to set
	 */
	public void setAgents(int agents) {
		this.agents = agents;
	}

	/**
	 * @return the sendingTime
	 */
	public HashMap<Integer, Double> getSendingTime() {
		return sendingTime;
	}


	/**
	 * @return the deliveryTable
	 */
	public HashMap<Integer, Integer> getDeliveryTable() {
		return deliveryTable;
	}


	/**
	 * @return the deliveryTime
	 */
	public HashMap<Integer, Double> getDeliveryTime() {
		return deliveryTime;
	}
	
//	public void addSender() {
//		this.sender++;
//	}
//
//	public void sendingDone() {
//		this.sender--;
//	}
//	
//	public boolean canSend() {
//		return (this.maxSenders==-1)||(this.sender<this.maxSenders) ;
//	}
//
//	public void setMaxNumberOfSenders(int maxSender) {
//		this.maxSenders = maxSender;
//	}

	public double averageDeliveryTime() {
		double tot = 0.0;
		for (Entry<Integer, Double> e : this.deliveryTime.entrySet()) {
			tot += e.getValue();
		}
		return tot/this.deliveryTime.size();
	}
	
	public double getAverageDeliveryTime() {
		return deliveryStats.getMean();
	}

	public double getMaxDeliveryTime() {
		return deliveryStats.getMax();
	}

	public double getMinDeliveryTime() {
		return deliveryStats.getMin();
	}
	
	public double getSDDeliveryTime() {
		return deliveryStats.getStandardDeviation();
	}

	public double getDeliveredMessages() {
		return deliveryStats.getN();
	}
	
	public double getAverageWaitingIdTime() {
		return waitingIdStats.getMean();
	}

	public double getMaxWaitingIdTime() {
		return waitingIdStats.getMax();
	}

	public double getMinWaitingIdTime() {
		return waitingIdStats.getMin();
	}

	public double getSDWaitingIdTime() {
		return waitingIdStats.getStandardDeviation();
	}
	
	public void registerMessageInterval( double time ) {
		messageInterval.addValue(time);
	}
	
	public SummaryStatistics getMessageIntevalStatistics() {
		return messageInterval;
	}

	public double getLastDeliveryTime() {
		return lastDeliveryTime;
	}
}
