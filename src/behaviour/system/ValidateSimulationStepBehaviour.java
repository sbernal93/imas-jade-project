package behaviour.system;

import agent.SystemAgent;
import jade.core.behaviours.OneShotBehaviour;
import util.Movement;
import util.Movement.MovementStatus;

public class ValidateSimulationStepBehaviour extends OneShotBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int FINISHED_SIMULATION = 1;
	public static final int CONTINUE_SIMULATION = 0;
	
	private SystemAgent agent;
	
	public ValidateSimulationStepBehaviour(SystemAgent agent) {
		super(agent);
		this.agent = agent;
	}

	@Override
	public void action() {
		//validate movements
		this.agent.log("ValidateSimulationStepBehaviour started");
		for(Movement movement : this.agent.getMovementsProposed()) {
			if(this.agent.getGame().isValidMovement(movement)) {
				movement.setStatus(MovementStatus.ACCEPTED);
			} else {
				movement.setStatus(MovementStatus.REJECTED);
			}
		}
	}
	
	@Override
	public int onEnd() {
		this.agent.log("ApplySimulationStepBehaviour finished" );
		if(this.agent.getGame().getSimulationSteps() >= this.agent.getCurrentStep()) {
			this.agent.log("Simulation finished");
			this.agent.setCurrentStep(this.agent.getCurrentStep() + 1);
			return FINISHED_SIMULATION;
		}
		this.agent.log("Simulation continues");
		return CONTINUE_SIMULATION;
	}

}
