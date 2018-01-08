package behaviour.system;

import agent.SystemAgent;
import jade.core.behaviours.OneShotBehaviour;

public class EndSimulationBehaviour extends OneShotBehaviour{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private SystemAgent agent;
	
	public EndSimulationBehaviour(SystemAgent agent) {
		super(agent);
		this.agent = agent;
	}

	@Override
	public void action() {
		//TODO: finish simulation
		
	}
}
