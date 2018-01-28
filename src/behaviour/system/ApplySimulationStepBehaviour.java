package behaviour.system;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import agent.SystemAgent;
import jade.core.behaviours.OneShotBehaviour;
import util.Movement;
import util.Movement.MovementStatus;

public class ApplySimulationStepBehaviour extends OneShotBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private SystemAgent agent;
	
	public ApplySimulationStepBehaviour(SystemAgent agent) {
		super(agent);
		this.agent = agent;
	}

	@Override
	public void action() {
		this.agent.log("ApplySimulationStepBehaviour started");
		this.agent.getMovementsProposed().stream().filter(m -> m.getStatus().equals(MovementStatus.ACCEPTED)).forEach(m -> {
			try {
				this.agent.getGame().moveAgent(m);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		this.agent.updateGUI();
		this.agent.getGui().showStatistics("Current step: " + this.agent.getCurrentStep() );
		this.agent.setMovementsProposed(new ArrayList<>());

	}
	
	
}
