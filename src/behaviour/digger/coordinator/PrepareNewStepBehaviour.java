package behaviour.digger.coordinator;

import java.util.ArrayList;

import agent.DiggerCoordinatorAgent;
import behaviour.BaseRequesterBehaviour;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import onthology.MessageContent;
import onthology.Movement;

public class PrepareNewStepBehaviour extends SimpleBehaviour{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean finished;
	private boolean sentMessages;
	private DiggerCoordinatorAgent agent;
	
	public PrepareNewStepBehaviour(DiggerCoordinatorAgent agent) {
		super(agent);
		this.agent = agent;
		finished = false;
		sentMessages = false;
	}

	@Override
	public void action() {
		if(!sentMessages) {
			this.agent.setMovements(new ArrayList<>());
	    	for (AID agent : this.agent.getDiggerAgents()) {
	    		this.agent.addBehaviour(new BaseRequesterBehaviour<DiggerCoordinatorAgent>(this.agent,
	    				buildSimStepMessageForDiggerAgent(agent)) {
	
	    					private static final long serialVersionUID = 1L;
	    					
	    					@Override
	    					protected void handleInform(ACLMessage msg) {
	    						try {
	    							((DiggerCoordinatorAgent) this.getAgent()).addMovement((Movement) msg.getContentObject());
	    						} catch (UnreadableException e) {
	    							e.printStackTrace();
	    						}
	    					}
	    		});
	    	}
	    	sentMessages = true;
		} else {
			if(this.agent.getMovements().size() == this.agent.getDiggerAgents().size()) {
				finished = true;
			}
		}
    	
	}
	
	private ACLMessage buildSimStepMessageForDiggerAgent(AID agent) {
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.clearAllReceiver();
		message.addReceiver(agent);
		message.setProtocol(InteractionProtocol.FIPA_REQUEST);
		this.agent.log("Request message to a Digger agent");
        try {
        	message.setContent(MessageContent.NEW_STEP);
        	this.agent.log("Request message content:" + message.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
	}

	@Override
	public boolean done() {
		return finished;
	}

}
