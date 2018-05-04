package eu.quanticol.abcsimulator;

public class Counting extends ComponentBehaviour {
	private int id;
	
	public Counting(int id) {
		this.id = id;
	}

	@Override
	public boolean onStart(ComponentMessage msg) {
		if(this.id == 0) {
			System.out.println(this.id + " starts!");
			return true;
		}
		else if (msg != null)
			return onMessage(msg);
		else
			return false;
	}

	@Override
	public boolean onMessage(ComponentMessage msg) {
		Object[] data = msg.getData();
		
		if(data.length == 1 && data[0].equals(this.id - 1)) {
			System.out.println(this.id + " got "+data[0].toString());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public GetMessage getMessage() {
		return new GetMessage(new ComponentMessage(this.id), false);
	}

}
