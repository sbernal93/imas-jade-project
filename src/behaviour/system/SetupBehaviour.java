package behaviour.system;

import agent.AgentType;
import agent.SystemAgent;
import behaviour.BaseSearchAgentBehaviour;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;

public class SetupBehaviour extends SequentialBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private SystemAgent agent;
	
	public SetupBehaviour(SystemAgent agent) {
		super(agent);
		this.agent = agent;
		this.addSubBehaviour(new CreateCoordinatorAgentBehaviour(agent, AgentType.COORDINATOR));
		this.addSubBehaviour(new SearchCoordinatorAgentBehaviour(agent, AgentType.COORDINATOR));
		
	}

}
