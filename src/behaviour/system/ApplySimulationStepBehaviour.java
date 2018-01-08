package behaviour.system;

import agent.SystemAgent;
import jade.core.behaviours.OneShotBehaviour;

public class ApplySimulationStepBehaviour extends OneShotBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int FINISHED_SIMULATION = 1;
	public static final int CONTINUE_SIMULATION = 0;
	
	private SystemAgent agent;
	
	public ApplySimulationStepBehaviour(SystemAgent agent) {
		super(agent);
		this.agent = agent;
	}

	@Override
	public void action() {
		//TODO: execute step, should check and apply actions, and notify coordinator when finished
		
	}
	
	@Override
	public int onEnd() {
		// TODO should check if game is ended
		return FINISHED_SIMULATION;
	}

}
