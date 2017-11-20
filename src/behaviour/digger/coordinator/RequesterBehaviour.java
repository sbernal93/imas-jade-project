package behaviour.digger.coordinator;

import java.util.List;

import agent.AgentType;
import agent.DiggerAgent;
import agent.DiggerCoordinatorAgent;
import behaviour.BaseRequesterBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import map.Cell;
import onthology.GameSettings;

/**
 * The requester behaviour for the Digger Coordinator Agent. 
 * The Digger Coordinator Agent requests the game settings from the 
 * Coordinator Agent to establish the following:
 * 1.- Using the map, it will know how many digger agents are to be created
 * 2.- Using the configuration, it will know how to initialize the digger agents 
 * (the amount of metal it can carry,etc)
 * 
 *
 */
public class RequesterBehaviour extends BaseRequesterBehaviour<DiggerCoordinatorAgent>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RequesterBehaviour(DiggerCoordinatorAgent agent, ACLMessage msg) {
		super(agent, msg);
        agent.log("Started behaviour to deal with AGREEs");
	}

    /**
     * Overriding Inform messages
     *
     * @param msg Message
     */
    @Override
    protected void handleInform(ACLMessage msg) {
    	DiggerCoordinatorAgent agent = (DiggerCoordinatorAgent) this.getAgent();
        agent.log("INFORM received from " + ((AID) msg.getSender()).getLocalName());
        try {
            GameSettings game = (GameSettings) msg.getContentObject();
            agent.setGame(game);
            agent.log(game.getShortString());
            
            // After we get the game settings, we initialize each digger agent
            //	on the corresponding cell
            List<Cell> agentCells = game.getAgentList().get(AgentType.DIGGER);
            int count = 0;
            for(Cell cell : agentCells) {
            	DiggerAgent newDiggerAgent = createDiggerAgent(cell, count, agent.getContainerController());
            	agent.addDiggerAgent(newDiggerAgent);
            	count++;	
            }
            
        } catch (Exception e) {
            agent.errorLog("Incorrect content: " + e.toString());
        }
    }
    
    private DiggerAgent createDiggerAgent(Cell cell, int count, AgentContainer container) {
    	Object[] args = {cell};
    	try {
    		//TODO: figure out how to pass the cell to the digger agent
    		AgentController controller = container.createNewAgent(AgentType.DIGGER.name() + count, "agent.DiggerAgent", args);
    		controller.start();
    		return controller.getO2AInterface(DiggerAgent.class);
		} catch (StaleProxyException e) {
			e.printStackTrace();
		} 
    	return null;
    }


}
