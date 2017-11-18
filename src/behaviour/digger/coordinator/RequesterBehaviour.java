package behaviour.digger.coordinator;

import java.util.List;

import agent.AgentType;
import agent.DiggerAgent;
import agent.DiggerCoordinatorAgent;
import jade.core.AID;
import jade.domain.FIPANames.InteractionProtocol;
import jade.wrapper.AgentContainer;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import map.Cell;
import onthology.GameSettings;
import onthology.MessageContent;

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
public class RequesterBehaviour extends AchieveREInitiator{

	public RequesterBehaviour(DiggerCoordinatorAgent agent, ACLMessage msg) {
		super(agent, msg);
        agent.log("Started behaviour to deal with AGREEs");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

    /**
     * Handle AGREE messages
     *
     * @param msg Message to handle
     */
    @Override
    protected void handleAgree(ACLMessage msg) {
        DiggerCoordinatorAgent agent = (DiggerCoordinatorAgent) this.getAgent();
        agent.log("AGREE received from " + ((AID) msg.getSender()).getLocalName());
    }

    /**
     * Handle INFORM messages
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

    /**
     * Handle NOT-UNDERSTOOD messages
     *
     * @param msg Message
     */
    @Override
    protected void handleNotUnderstood(ACLMessage msg) {
    	DiggerCoordinatorAgent agent = (DiggerCoordinatorAgent) this.getAgent();
        agent.log("This message NOT UNDERSTOOD.");
    }

    /**
     * Handle FAILURE messages
     *
     * @param msg Message
     */
    @Override
    protected void handleFailure(ACLMessage msg) {
    	DiggerCoordinatorAgent agent = (DiggerCoordinatorAgent) this.getAgent();
        agent.log("The action has failed.");
       /* ACLMessage initialRequest = new ACLMessage(ACLMessage.REQUEST);
        initialRequest.clearAllReceiver();
        initialRequest.addReceiver(agent.getAID());
        initialRequest.setProtocol(InteractionProtocol.FIPA_REQUEST);
        agent.log("Request message to agent");
        try {
            initialRequest.setContent(MessageContent.GET_MAP);
            agent.log("Request message content:" + initialRequest.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        reset(initialRequest);
        agent.addBehaviour(this);*/

    } //End of handleFailure

    /**
     * Handle REFUSE messages
     *
     * @param msg Message
     */
    @Override
    protected void handleRefuse(ACLMessage msg) {
    	DiggerCoordinatorAgent agent = (DiggerCoordinatorAgent) this.getAgent();
        agent.log("Action refused.");
    }

}
