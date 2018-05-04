package eu.quanticol.abcsimulator;

public abstract class ComponentBehaviour {
	public abstract boolean onStart(ComponentMessage msg);
	public abstract boolean onMessage(ComponentMessage msg);
	public abstract GetMessage getMessage();
	public boolean onStart(){
		return onStart(null);
	}
	
	public class GetMessage {
		public final ComponentMessage message;
		public final boolean wantSend;
		
		public GetMessage(ComponentMessage message, boolean wantSend) {
			this.message = message;
			this.wantSend = wantSend;
		}
	}
}