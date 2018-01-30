package behaviour.system;

import agent.SystemAgent;
import jade.core.behaviours.OneShotBehaviour;
import util.Movement;
import util.MovementStatus;

public class ValidateSimulationStepBehaviour extends OneShotBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
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

}
