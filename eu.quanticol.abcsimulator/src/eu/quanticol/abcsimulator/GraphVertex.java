package eu.quanticol.abcsimulator;

import java.util.Random;
import java.util.Vector;

public class GraphVertex extends ComponentBehaviour {
	
	private final int id;
	private final int[] N;
	private int colour;
	private int round;
	private boolean assigned;
	private int[] constraints;
	private int counter;
	private int[] used;
	private int done;
	private boolean sendTry;

	public GraphVertex(int id, int[] N) {
		this.id = id;
		this.N = N;
		this.colour = 0;
		this.round = 0;
		this.assigned = false;
		this.constraints = new int[] {};
		this.counter = 0;
		this.used = new int[] {};
		this.done = 0;
		this.sendTry = true;
	}
	
	private static boolean in(int[] v, int x) {
		for(int i=0; i<v.length; i++) {
			if(v[i] == x)
				return true;
		}
		return false;
	}
	
	private static int[] add(int[] v, int x) {
		int[] newV = new int[v.length+1];
		int i;
		for(i=0; i<v.length && v[i] < x; i++)
			newV[i] = v[i];
		if(i < v.length && v[i] == x)
			return v;
		newV[i] = x;
		for(; i<v.length; i++)
			newV[i+1] = v[i];
		return newV;
	}
	
	private boolean wantToSend() {
		return (!assigned && (sendTry || (counter == N.length - done && !in(used, colour) && !in(constraints, colour)))); 
	}
	
	public GetMessage getMessage(){
		ComponentMessage message = null;
		if(!assigned && sendTry) {
			colour = 0;
			while(colour < used.length && used[colour] == colour) colour++;
			sendTry = false;
			if(counter == N.length - done &&  !in(constraints, colour)) {
				assigned = true;
				//System.out.format("Assign %03d %d\n", id, colour);
				//System.out.println("Assign "+id+" "+colour);
				message = new ComponentMessage("done", colour, round+1, id);
			} else {
				//System.out.println("Not yet "+id+" : "+counter +" "+ N.length +" "+ done+" "+in(used, colour) +" "+in(constraints, colour));
				message = new ComponentMessage("try", colour, round, id);
			}
		} 
		else {
			if(!assigned && counter == N.length - done && !in(used, colour) && !in(constraints, colour)) {
				assigned = true;
				//System.out.format("Assign %03d %d\n", id, colour);
				//System.out.println("Assign "+id+" "+colour);
				message = new ComponentMessage("done", colour, round+1, id);
			}//else if(!assigned)
				//System.out.println("Not yet "+id+" : "+counter +" "+ N.length +" "+ done+" "+in(used, colour) +" "+in(constraints, colour));
		}
		return new GetMessage(message, wantToSend());	
	}
	
	private boolean onDone(int doneCol, int doneRound){
		done++;
		used = add(used, doneCol);
		if(round < doneRound) {
			round = doneRound;
			constraints = new int[] {};
			sendTry = true;
			counter = 0;
		}
		return wantToSend();
	}
	
	private boolean onTry(int tryCol, int tryRound, int tryId){
		if(id > tryId && round == tryRound) {
			counter++;
		} else if(id > tryId && round < tryRound) {
			round = tryRound;
			sendTry = true;
			counter = 1;
			constraints = new int[] {};
		} else if(id < tryId && round == tryRound) {
			counter++;
			constraints = add(constraints, tryCol);
		} else if(id < tryId && round < tryRound) {
			round = tryRound;
			sendTry = true;
			counter = 1;
			constraints = new int[] {tryCol};
		}
		return wantToSend();
	}

	@Override
	public boolean onStart(ComponentMessage msg) {
		if(msg == null) {
			return wantToSend();
		} else {
			return onMessage(msg);
		}
	}

	@Override
	public boolean onMessage(ComponentMessage msg) {
		if(msg.getData().length == 4) {
			String type = (String)(msg.getData()[0]);
			int col = (int)(msg.getData()[1]);
			int rnd = (int)(msg.getData()[2]);
			int senderId = (int)(msg.getData()[3]);
			if(in(N, senderId)) {
				//System.out.println(id+" got "+msg.toString());
				switch(type) {
				case "try":
					return onTry(col, rnd, senderId);
				case "done":
					return onDone(col, rnd);
				default:
					return wantToSend();
				}	
			} else {
				return wantToSend();
			}
		} else {
			return wantToSend();
		}
	}
	
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
	
	public static boolean check(int[][] graph, int[] assignments) {
		for(int i=0; i<graph.length; i++) {
			for(int j: graph[i]) {
				if(assignments[i] == assignments[j])
					return false;
			}
		}
		return true;
	}
}
