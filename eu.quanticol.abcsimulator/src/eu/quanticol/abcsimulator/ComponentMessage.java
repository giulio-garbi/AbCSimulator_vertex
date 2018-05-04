package eu.quanticol.abcsimulator;

public class ComponentMessage {
	private Object[] data;
	
	public ComponentMessage(Object ...data) {
		this.data = data;
	}
	
	public Object[] getData() {
		return data;
	}

	public static ComponentMessage from(AbCMessage peek) {
		return new ComponentMessage(peek.getData());
	}
	
	public String toString() {
		String[] sData = new String[data.length];
		for(int i=0; i<data.length; i++)
			sData[i] = data[i].toString();
		return "{" + String.join(", ", sData) + "}";
	}
}
