/**
 * 
 */
package eu.quanticol.abcsimulator;

import java.util.LinkedList;

/**
 * @author loreti
 *
 */
public class AbCMessage {
	
	private AbCNode from;
	
	private AbCNode to;
	
	private Object[] data;

	private MessageType type;
	
	private int messageIndex;
	
	private LinkedList<Integer> route;

	
	/*
	 * @param type
	 * @param messageIndex
	 * @param route
	 */
	public AbCMessage(AbCNode from, MessageType type, int messageIndex, Object[] data, LinkedList<Integer> route, AbCNode to) {
		super();
		this.type = type;
		this.data = data;
		this.messageIndex = messageIndex;
		this.route = route;
		this.from = from;
		this.to = to;
	}

	/**
	 * @return the type
	 */
	public MessageType getType() {
		return type;
	}

	/**
	 * @return the messageIndex
	 */
	public int getMessageIndex() {
		return messageIndex;
	}

	/**
	 * @return the route
	 */
	public LinkedList<Integer> getRoute() {
		return route;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return from.toString()+"->"+"[type=" + type + ", messageIndex=" + messageIndex + ", route=" + route + "]"+"->"+to.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + messageIndex;
		result = prime * result + ((route == null) ? 0 : route.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AbCMessage)) {
			return false;
		}
		AbCMessage other = (AbCMessage) obj;
		if (messageIndex != other.messageIndex) {
			return false;
		}
		if (route == null) {
			if (other.route != null) {
				return false;
			}
		} else if (!route.equals(other.route)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}

	public Integer lastHop() {
		return route.peekLast();
	}

	public AbCNode getSource() {
		return from;
	}
	
	public AbCNode getTarget() {
		return to;
	}

	public Object[] getData() {
		return data;
	}
	
	
	
	
	

}
