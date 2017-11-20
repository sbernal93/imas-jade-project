package behaviour.prospector.coordinator;

import agent.ProspectorCoordinatorAgent;
import behaviour.BaseRequesterBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
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
            
        } catch (Exception e) {
            agent.errorLog("Incorrect content: " + e.toString());
        }
    }
    

}
