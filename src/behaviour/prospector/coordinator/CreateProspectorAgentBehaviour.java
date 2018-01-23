package behaviour.prospector.coordinator;

import agent.AgentType;
import agent.ProspectorAgent;
import agent.ProspectorCoordinatorAgent;
import behaviour.BaseCreateAgentBehaviour;
import jade.core.AID;
import onthology.GameSettings;

public class CreateProspectorAgentBehaviour extends BaseCreateAgentBehaviour<ProspectorAgent> {

	public CreateProspectorAgentBehaviour(ProspectorCoordinatorAgent agent, AgentType type) {
		super(agent, type);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Class<ProspectorAgent> getAgentToCreateClass() {
		return ProspectorAgent.class;
	}

	@Override
	public void addAgent(AID agent) {
		ProspectorCoordinatorAgent pca = (ProspectorCoordinatorAgent) this.getAgent();
		pca.addProspectorAgent(agent);
	}

	@Override
	public GameSettings getGame() {
		return ((ProspectorCoordinatorAgent) this.getAgent()).getGame();
	}



}
