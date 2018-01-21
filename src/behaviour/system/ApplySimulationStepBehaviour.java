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
		this.agent.log("ApplySimulationStepBehaviour started");
		
	}
	
	@Override
	public int onEnd() {
		this.agent.log("ApplySimulationStepBehaviour finished" );
		//commented out while steps not yet implemented
		/*
		if(this.agent.getGame().getSimulationSteps() >= this.agent.getCurrentStep()) {
			this.agent.log("Simulation finished");
			return FINISHED_SIMULATION;
		}
		this.agent.log("Simulation continues");
		return CONTINUE_SIMULATION;*/
		
		return FINISHED_SIMULATION;
	}

}
