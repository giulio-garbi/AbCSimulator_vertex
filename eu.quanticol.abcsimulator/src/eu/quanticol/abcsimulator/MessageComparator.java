/**
 * 
 */
package eu.quanticol.abcsimulator;

import java.util.Comparator;

/**
 * @author loreti
 *
 */
public class MessageComparator implements Comparator<AbCMessage> {

	@Override
	public int compare(AbCMessage o1, AbCMessage o2) {
		if (o1.getMessageIndex()<o2.getMessageIndex()) {
			return -1;
		}
		if (o1.getMessageIndex()==o2.getMessageIndex()) {
			return 0;
		}
		return 1;
	}

}
