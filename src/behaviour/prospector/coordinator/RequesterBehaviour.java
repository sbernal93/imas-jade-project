package behaviour.prospector.coordinator;

import java.util.List;

import agent.AgentType;
import agent.DiggerAgent;
import agent.ProspectorAgent;
import agent.ProspectorCoordinatorAgent;
import behaviour.BaseRequesterBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import map.Cell;
import onthology.GameSettings;

public class RequesterBehaviour extends BaseRequesterBehaviour<ProspectorCoordinatorAgent>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RequesterBehaviour(ProspectorCoordinatorAgent a, ACLMessage msg) {
		super(a, msg);
	}


    /**
     * Overriding Inform messages
     *
     * @param msg Message
     */
    @Override
    protected void handleInform(ACLMessage msg) {
    	ProspectorCoordinatorAgent agent = (ProspectorCoordinatorAgent) this.getAgent();
        agent.log("INFORM received from " + ((AID) msg.getSender()).getLocalName());
        try {
            GameSettings game = (GameSettings) msg.getContentObject();
            agent.setGame(game);
            agent.log(game.getShortString());
            
            // After we get the game settings, we initialize each digger agent
            //	on the corresponding cell
            int count = 0;
            List<Cell> agentCells = game.getAgentList().get(AgentType.PROSPECTOR);
           for(Cell cell : agentCells) {
            	ProspectorAgent newProspectorAgent = createProspectorAgent(cell, count, agent.getContainerController());
            	agent.addProspectorAgent(newProspectorAgent);
            	count++;	
            }
            
        } catch (Exception e) {
            agent.errorLog("Incorrect content: " + e.toString());
        }
    }
    
    
    private ProspectorAgent createProspectorAgent(Cell cell, int count, AgentContainer container) {
    	Object[] args = {cell};
    	try {
    		//TODO: figure out how to pass the cell to the digger agent
    		AgentController controller = container.createNewAgent(AgentType.PROSPECTOR.name() + count, "agent.ProspectorAgent", args);
    		controller.start();
    		return controller.getO2AInterface(ProspectorAgent.class);
		} catch (StaleProxyException e) {
			e.printStackTrace();
		} 
    	return null;
    }

}
