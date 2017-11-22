package behaviour;

import java.util.List;

import agent.AgentType;
import agent.ImasAgent;
import jade.core.behaviours.SimpleBehaviour;
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

	@Override
	public void action() {
		if(this.getGame() == null) {
			this.agent.log("No game set yet, cant create agents");
			finished = true;
		} else {
			try {
				 int count = 0;
				 List<Cell> agentCells = getGame().getAgentList().get(type);
				 for(Cell cell : agentCells) {
					 Object[] args = {cell};
					 AgentController controller = this.agent.getContainerController()
							 .createNewAgent(type.name() + count, "agent." + getAgentToCreateClass().getSimpleName(), args);
					 controller.start();
		       		 addAgent(controller.getO2AInterface(getAgentToCreateClass()));
		       		 count++;	
				 }
				 this.agent.log("Sub agents created");
			} catch (Exception e) {
				this.agent.errorLog("Incorrect content: " + e.toString());
			} finally {
				finished = true;
			}
		}
	}
	
	@Override
	public boolean done() {
		return finished;
	}
	
	public abstract Class<T> getAgentToCreateClass();
	
	public abstract void addAgent(T agent);
	
	public abstract GameSettings getGame();
	
}
