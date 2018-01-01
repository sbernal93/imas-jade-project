package behaviour.coordinator;

import agent.AgentType;
import agent.CoordinatorAgent;
import agent.DiggerCoordinatorAgent;
import agent.ImasAgent;
import behaviour.BaseCreateAgentBehaviour;
import onthology.GameSettings;

public class CreateDiggerCoordinatorBehaviour extends BaseCreateAgentBehaviour<DiggerCoordinatorAgent>{

	public CreateDiggerCoordinatorBehaviour(ImasAgent agent, AgentType type) {
		super(agent, type);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Class<DiggerCoordinatorAgent> getAgentToCreateClass() {
		return DiggerCoordinatorAgent.class;
	}

	@Override
	public void addAgent(DiggerCoordinatorAgent agent) {
		//((CoordinatorAgent) this.getAgent()).setDiggerCoordinatorAgent(agent);
	}

	@Override
	public GameSettings getGame() {
		return ((CoordinatorAgent) this.getAgent()).getGame();
	}

}
