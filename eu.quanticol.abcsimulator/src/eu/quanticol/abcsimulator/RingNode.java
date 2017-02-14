/**
 * 
 */
package eu.quanticol.abcsimulator;

/**
 * @author loreti
 *
 */
public class RingNode extends ServerNode {

	public RingNode(AbCSystem system, int id) {
		super(system, id);
	}

	
	protected void handleDataPacket( AbCNode from , int index ) {
		this.next_index = index+1;
		if (parent != this) {
			outQueue.add( new AbCMessage( this , MessageType.DATA , index , null , parent  ) );
		}
		for (AbCNode n : children.values()) {
			if (from != n) {
				outQueue.add( new AbCMessage( this , MessageType.DATA , index , null , n  ) );				
			}
		}
	}	


}
