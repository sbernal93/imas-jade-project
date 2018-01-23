package behaviour.digger.coordinator;

import java.io.IOException;
import java.io.Serializable;

import agent.CoordinatorAgent;
import agent.DiggerCoordinatorAgent;
import agent.ProspectorAgent;
import agent.ProspectorCoordinatorAgent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import onthology.MessageContent;

/**.
 */
public class RequestResponseBehaviour extends AchieveREResponder {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Sets up the DiggerCoordinatorAgent and the template of messages to catch.
     *
     * @param agent The agent owning this behaviour
     * @param mt Template to receive future responses in this conversation
     */
    public RequestResponseBehaviour(DiggerCoordinatorAgent agent, MessageTemplate mt) {
        super(agent, mt);
        agent.log("Waiting REQUESTs from authorized agents");
    }

    /**
     * When the ProspectorAgent Agent receives a REQUEST message, it agrees. Only if
     * message type is AGREE, method prepareResultNotification() will be invoked.
     *
     * @param msg message received.
     * @return AGREE message when all was ok, or FAILURE otherwise.
     */
    @Override
    protected ACLMessage prepareResponse(ACLMessage msg) {
    	DiggerCoordinatorAgent agent = (DiggerCoordinatorAgent)this.getAgent();
        ACLMessage reply = msg.createReply();
        try {
            Object content = (Object) msg.getContent();
            agent.log("Request received");
            if(content.equals(MessageContent.NEW_STEP)) {
            	agent.log("NEW_STEP request message received");
            	reply.setPerformative(ACLMessage.AGREE);
            	agent.newStepResult();
            }
        } catch (Exception e) {
            reply.setPerformative(ACLMessage.FAILURE);
            agent.errorLog(e.getMessage());
            e.printStackTrace();
        }
        agent.log("Response being prepared");
        return reply;
    }

    /**
     * After sending an AGREE message on prepareResponse(), this method is executed
     *
     * NOTE: This method is called after the response has been sent and only when one
     * of the following two cases arise: the response was an agree message OR no
     * response message was sent.
     *
     * @param msg ACLMessage the received message
     * @param response ACLMessage the previously sent response message
     * @return ACLMessage to be sent as a result notification, of type INFORM
     * when all was ok, or FAILURE otherwise.
     */
    @Override
    protected ACLMessage prepareResultNotification(ACLMessage msg, ACLMessage response) {

        // it is important to make the createReply in order to keep the same context of
        // the conversation
    	DiggerCoordinatorAgent agent = (DiggerCoordinatorAgent) this.getAgent();
        ACLMessage reply = msg.createReply();
        if (reply.getPerformative() != ACLMessage.FAILURE) {
	        reply.setPerformative(ACLMessage.INFORM);
	        try {
	        	
				reply.setContentObject( (Serializable) agent.getMovements());
				agent.log("INFORM response prepared");
		        //agent.log("Content: " + reply.getContent());
			} catch (IOException e) {
				e.printStackTrace();
			}
	        agent.log("INFORM message sent");
        }
        return reply;

    }

    /**
     * No need for any specific action to reset this behaviour
     */
    @Override
    public void reset() {
    }

}
