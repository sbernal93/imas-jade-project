package behaviour.digger.coordinator;

import agent.AgentType;
import agent.DiggerAgent;
import agent.DiggerCoordinatorAgent;
import behaviour.BaseCreateAgentBehaviour;
import onthology.GameSettings;

public class CreateDiggerAgentBehaviour extends BaseCreateAgentBehaviour<DiggerAgent> {

	public CreateDiggerAgentBehaviour(DiggerCoordinatorAgent agent, AgentType type) {
		super(agent, type);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Class<DiggerAgent> getAgentToCreateClass() {
		return DiggerAgent.class;
	}

	@Override
	public void addAgent(DiggerAgent agent) {
		((DiggerCoordinatorAgent) this.getAgent()).addDiggerAgent(agent);;
	}

	@Override
	public GameSettings getGame() {
		return ((DiggerCoordinatorAgent) this.getAgent()).getGame();
	}

}
