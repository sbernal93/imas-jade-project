package behaviour.digger.coordinator;

import agent.DiggerCoordinatorAgent;
import behaviour.BaseRequesterBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
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
        } catch (Exception e) {
            agent.errorLog("Incorrect content: " + e.toString());
        }
    }


}
