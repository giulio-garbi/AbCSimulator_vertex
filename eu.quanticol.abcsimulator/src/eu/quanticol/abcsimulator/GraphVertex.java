package eu.quanticol.abcsimulator;

import java.util.Random;
import java.util.Vector;

import org.apache.commons.math3.random.RandomGenerator;

public class GraphVertex {
	int id;
	int[] N;
	int colour;
	int round;
	boolean assigned;
	int[] constraints;
	int counter;
	int[] used;
	int done;
	boolean sendTry;
	
	private static int[] toIntA(Vector<Integer> v) {
		int[] vA = new int[v.size()];
		for(int i=0; i<v.size(); i++)
			vA[i] = v.get(i);
		return vA;
	}
	public static int[][] makeGraph(Random random, int n, float p){
		@SuppressWarnings("unchecked")
		Vector<Integer>[] adj = new Vector[n];
		for(int i=0; i<n; i++)
			adj[i] = new Vector<Integer>();
		for(int i=0; i<n; i++)
			for(int j=i+1; j<n; j++)
				if(random.nextFloat()<p) {
					adj[i].add(j);
					adj[j].add(i);
				}
		int[][] adjA = new int[n][];
		for(int i=0; i<n; i++)
			adjA[i] = toIntA(adj[i]);
		return adjA;
	}
	
	public GraphVertex(int id, int[] N) {
		this.id = id;
		this.N = N;
		colour = 0;
		round = 0;
		assigned = false;
		constraints = new int[] {};
		counter = 0;
		used = new int[] {};
		done = 0;
		sendTry = true;
	}
	
	public Object[] onStart(Object[] message) {
		if(N.length == 0) {
			return testDone();
		} else if (message != null){
			Object[] msg = onMessage(message);
			if(msg != null)
				return msg;
			return testTry();
		} else {
			return testTry();
		}
	}
	
	private Object[] chooseColour() {
		colour = 0;
		while(colour < used.length && used[colour] == colour) colour++;
		return new Object[] {"try", colour, round, id};
	}
	
	public Object[] onMessage(Object[] message) {
		if(assigned)
			return null;
		switch(message[0].toString()) {
			case "try":{
				return onTry(message);
			}
			case "done":{
				return onDone(message);
			}
			default: {
				return null;
			}
		}
	}

	private Object[] onTry(Object[] message) {
		int y = (int)message[1];
		int z = (int)message[2];
		int tid = (int)message[3];
		
		if(!isNeighbour(tid)) {
			return null;
		}
		
		if (round == z) {
			if (id > tid) {
			counter++;
			return testDone();
			} else if (id < tid) {
			counter++;
			newConstraint(y);
			return testDone();
			}
		} else if (round < z) {
			if (id > tid) {
				round = z;
				sendTry = true;
				counter = 1;
				constraints = new int[]{};
				Object[] doneMsg = testDone();
				if (doneMsg != null)
					return doneMsg;
				else
					return testTry();
			} else if (id < tid) {
				round = z;
				sendTry = true;
				counter = 1;
				constraints = new int[]{y};
				Object[] doneMsg = testDone();
				if (doneMsg != null)
					return doneMsg;
				else
					return testTry();
			}
		}
		return null;
	}

	private Object[] onDone(Object[] message) {
		int y = (int)message[1];
		int z = (int)message[2];
		if (round < z) {
			round = z;
			constraints = new int[]{};
			sendTry = true;
			counter = 0;
		}
		done++;
		newUsed(y);
		Object[] doneMsg = testDone();
		if (doneMsg != null)
			return doneMsg;
		else
			return testTry();
	}
	
	private static int[] insertInto(int[] v, int y) {
		int[] vNew = new int[v.length + 1];
		int i = 0;
		while(i<v.length && v[i] < y) {
			vNew[i] = v[i];
			i++;
		}
		if(i<v.length && v[i] == y)
			return v;
		vNew[i] = y;
		while(i<v.length) {
			vNew[i+1] = v[i];
			i++;
		}
		return vNew;
	}
	
	private static boolean in(int[] v, int y) {
		for(int i=0; i<v.length && v[i]>=y; i++)
			if (v[i] == y)
				return true;
		return false;
	}

	private void newUsed(int y) {
		used = insertInto(used, y);
	}

	private Object[] testTry() {
		if(!assigned && sendTry) {
			sendTry = false;
			return chooseColour();
		} else {
			return null;
		}
	}

	private void newConstraint(int y) {
		constraints = insertInto(constraints, y);
	}

	private Object[] testDone() {
		if(counter == N.length - done && !in(constraints, colour) && !in(used, colour)) {
			assigned = true;
			return new Object[] {"done", colour, round + 1, id};
		} else
			return null;
	}

	private boolean isNeighbour(int tid) {
		for(int i=0; i < N.length && N[i] >= tid; i++)
			if(N[i] == tid)
				return true;
		return false;
	}
}
