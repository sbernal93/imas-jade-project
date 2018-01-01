package behaviour;

import agent.AgentType;
import agent.ImasAgent;
import agent.UtilsAgents;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public abstract class BaseSearchAgentBehaviour extends SimpleBehaviour {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ImasAgent agent;
	private AgentType type;
	
	boolean finished =false;
	
	public BaseSearchAgentBehaviour(ImasAgent agent, AgentType type) {
		this.agent = agent;
		this.type = type;
	}
	
	@Override
	public void action() {
		this.agent.log("Searching for agent: " + this.type.toString());
		ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(type.toString());
        UtilsAgents.searchAgent(this.agent, searchCriterion);
        finished = true;
	}
	
	@Override
	public boolean done() {
		return finished;
	}
	
	@Override
	public Agent getAgent() {
		return this.agent;
	}
	
	public abstract void setAgent(Agent agent);
	
}
