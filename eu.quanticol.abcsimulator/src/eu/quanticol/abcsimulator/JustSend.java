package eu.quanticol.abcsimulator;

public class JustSend extends ComponentBehaviour {

	@Override
	public boolean onStart(ComponentMessage msg) {
		return true;
	}

	@Override
	public boolean onMessage(ComponentMessage msg) {
		return true;
	}

	@Override
	public GetMessage getMessage() {
		return new GetMessage(new ComponentMessage(), true);
	}

}
