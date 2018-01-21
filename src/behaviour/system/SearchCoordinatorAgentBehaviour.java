package behaviour.system;

import agent.AgentType;
import agent.ImasAgent;
import agent.SystemAgent;
import behaviour.BaseSearchAgentBehaviour;
import jade.core.AID;
import jade.core.Agent;

public class SearchCoordinatorAgentBehaviour extends BaseSearchAgentBehaviour{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SearchCoordinatorAgentBehaviour(ImasAgent agent, AgentType type) {
		super(agent, type);
		agent.log("Created SearchCoordinatorAgentBehaviour");
	}


	@Override
	public void setAgent(AID agent) {
		((SystemAgent) this.getAgent()).setCoordinatorAgent(agent);	
	}

}
