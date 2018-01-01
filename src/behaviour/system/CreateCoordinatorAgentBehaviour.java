package behaviour.system;

import agent.AgentType;
import agent.CoordinatorAgent;
import agent.ImasAgent;
import agent.SystemAgent;
import behaviour.BaseCreateAgentBehaviour;
import onthology.GameSettings;

public class CreateCoordinatorAgentBehaviour extends BaseCreateAgentBehaviour<CoordinatorAgent>{

	public CreateCoordinatorAgentBehaviour(ImasAgent agent, AgentType type) {
		super(agent, type);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Class<CoordinatorAgent> getAgentToCreateClass() {
		return CoordinatorAgent.class;
	}

	@Override
	public void addAgent(CoordinatorAgent agent) {
		//((SystemAgent) this.getAgent()).setCoordinatorAgent(agent);
		
	}

	@Override
	public GameSettings getGame() {
		return ((SystemAgent) this.getAgent()).getGame();
	}

}
