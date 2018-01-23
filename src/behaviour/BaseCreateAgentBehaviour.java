package behaviour;

import java.util.List;

import agent.AgentType;
import agent.ImasAgent;
import agent.UtilsAgents;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.wrapper.AgentController;
import map.Cell;
import onthology.GameSettings;

public abstract class BaseCreateAgentBehaviour<T> extends SimpleBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ImasAgent agent;
	private AgentType type;
	
	boolean finished =false;
	
	public BaseCreateAgentBehaviour(ImasAgent agent, AgentType type) {
		this.agent = agent;
		this.type = type;
	}
	
	private void createMultipleAgents() {
		try {
			 int count = 0;
			 List<Cell> agentCells = getGame().getAgentList().get(type);
			 for(Cell cell : agentCells) {
				 agent.log("Creating new agent");
				 Object[] args = {getGame(), cell, this.agent.getAID(), type.name() + count};
				 AgentController controller = this.agent.getContainerController()
						 .createNewAgent(type.name() + count, "agent." + getAgentToCreateClass().getSimpleName(), args);
				 controller.start(); 
				
	       		 //addAgent(controller.getO2AInterface(getAgentToCreateClass()));
				 agent.log("Started agent, searching for it");
				 ServiceDescription searchCriterion = new ServiceDescription();
			     searchCriterion.setType(type.name() + count);
			     addAgent(UtilsAgents.searchAgent(this.agent, searchCriterion));
			     agent.log("Agent found");
	       		 count++;	
			 }
			 this.agent.log("Sub agents created");
		} catch (Exception e) {
			this.agent.errorLog("Incorrect content: " + e.toString());
		} finally {
			finished = true;
		}
	}
	
	private void createIndividualAgent() {
		try {
			Object[] args = {getGame(), this.agent.getAID()};
			AgentController controller = this.agent.getContainerController()
					.createNewAgent(type.name(), "agent." + getAgentToCreateClass().getSimpleName(), args);
			this.agent.log("Starting agent");
			controller.start();
			//addAgent(controller.getO2AInterface(getAgentToCreateClass()));
			this.agent.log("Agent succesfully created");
		} catch (Exception e) {
			this.agent.errorLog("Incorrect content: " + e.toString());
		} finally {
			finished = true;
		}
	}

	@Override
	public void action() {
		//Do all agents need a game settings? this validation could be done
		//for some type of agents
		if(this.getGame() == null) {
			this.agent.log("No game set yet, cant create agents");
			finished = true;
		} else {
			agent.log("Attempting to create agent: " + type.name());
			if(type.equals(AgentType.DIGGER) || type.equals(AgentType.PROSPECTOR)) {
				createMultipleAgents();
			} else {
				createIndividualAgent();
			}
		}
	}
	
	@Override
	public boolean done() {
		return finished;
	}
	
	public abstract Class<T> getAgentToCreateClass();
	
	public abstract void addAgent(AID agent);
	
	public abstract GameSettings getGame();
	
}
