/**
 * 
 */
package eu.quanticol.abcsimulator;

/**
 * @author loreti
 *
 */
public class SharedCounter {

	private int counter;
	
	public SharedCounter() {
		this.counter = 0;
	}
	
	public int getAndUpdate() {
		return counter++;
	}
	
	public int get() {
		return counter;
	}
	
}
