package behaviour.coordinator;

import agent.AgentType;
import agent.CoordinatorAgent;
import agent.ImasAgent;
import agent.ProspectorCoordinatorAgent;
import behaviour.BaseCreateAgentBehaviour;
import jade.core.AID;
import onthology.GameSettings;

public class CreateProspectorCoordinatorBehaviour  extends BaseCreateAgentBehaviour<ProspectorCoordinatorAgent>{

	public CreateProspectorCoordinatorBehaviour(ImasAgent agent, AgentType type) {
		super(agent, type);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Class<ProspectorCoordinatorAgent> getAgentToCreateClass() {
		return ProspectorCoordinatorAgent.class;
	}

	@Override
	public void addAgent(AID agent) {
		//((CoordinatorAgent) this.getAgent()).setProspectorCoordinatorAgent(agent);
	}

	@Override
	public GameSettings getGame() {
		return ((CoordinatorAgent) this.getAgent()).getGame();
	}
}
