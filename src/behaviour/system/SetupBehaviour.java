package behaviour.system;

import agent.SystemAgent;
import jade.core.behaviours.OneShotBehaviour;

public class SetupBehaviour extends OneShotBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private SystemAgent agent;
	
	public SetupBehaviour(SystemAgent agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {
		//TODO: maybe validate setup?
	}

}
