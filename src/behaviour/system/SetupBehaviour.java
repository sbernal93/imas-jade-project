package behaviour.system;

import agent.AgentType;
import agent.SystemAgent;
import jade.core.behaviours.SequentialBehaviour;

public class SetupBehaviour extends SequentialBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SetupBehaviour(SystemAgent agent) {
		super(agent);
		//this.agent = agent;
		this.addSubBehaviour(new CreateCoordinatorAgentBehaviour(agent, AgentType.COORDINATOR));
		this.addSubBehaviour(new SearchCoordinatorAgentBehaviour(agent, AgentType.COORDINATOR));
		
	}

}
