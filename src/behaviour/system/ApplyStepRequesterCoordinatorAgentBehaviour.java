package behaviour.system;

import java.util.Vector;

import agent.SystemAgent;
import behaviour.BaseRequesterBehaviour;
import jade.core.AID;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.ACLMessage;
import onthology.MessageContent;

public class ApplyStepRequesterCoordinatorAgentBehaviour extends BaseRequesterBehaviour<SystemAgent>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ApplyStepRequesterCoordinatorAgentBehaviour(SystemAgent a, ACLMessage msg) {
		super(a, msg);
	}
	
	public ApplyStepRequesterCoordinatorAgentBehaviour(SystemAgent a) {
		super(a, null);
	}

	@Override
	protected void handleInform(ACLMessage msg) {
		//TODO: implement handle of message type. INFORM messages should be handled 
		//receiving the new actions that where attempted during the simulation steps
		//this actions should be stored in the systemAgent in order to be validated
		//in the other FSM state
    	SystemAgent agent = (SystemAgent) this.getAgent();
        agent.log("INFORM received from " + ((AID) msg.getSender()).getLocalName());
	}

	@Override
	protected Vector<ACLMessage> prepareRequests(ACLMessage request) {
		//This can be changed to return various request if necessary
		Vector<ACLMessage> v = new Vector<>();
		v.add(buildMessageForCoordinatorAgent());
		return v;
	}

	private ACLMessage buildMessageForCoordinatorAgent() {
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.clearAllReceiver();
		((SystemAgent) this.myAgent).log("AID: " +  ((SystemAgent) this.myAgent).getCoordinatorAgent());
		message.addReceiver(((SystemAgent) this.myAgent).getCoordinatorAgent());
		message.setProtocol(InteractionProtocol.FIPA_REQUEST);
		((SystemAgent) this.myAgent).log("Request message to Coordinator agent");
        try {
        	message.setContent(MessageContent.APPLY_STEP);
        	((SystemAgent) this.myAgent).log("Request message content:" + message.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
	}
	

	@Override
	public int onEnd() {
		reset();
		return super.onEnd();
	}
}
