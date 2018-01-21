package behaviour.coordinator;

import agent.CoordinatorAgent;
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
     * Sets up the System agent and the template of messages to catch.
     *
     * @param agent The agent owning this behaviour
     * @param mt Template to receive future responses in this conversation
     */
    public RequestResponseBehaviour(CoordinatorAgent agent, MessageTemplate mt) {
        super(agent, mt);
        agent.log("Waiting REQUESTs from authorized agents");
    }

    /**
     * When the Coordinator Agent receives a REQUEST message, it agrees. Only if
     * message type is AGREE, method prepareResultNotification() will be invoked.
     *
     * @param msg message received.
     * @return AGREE message when all was ok, or FAILURE otherwise.
     */
    @Override
    protected ACLMessage prepareResponse(ACLMessage msg) {
    	CoordinatorAgent agent = (CoordinatorAgent)this.getAgent();
        ACLMessage reply = msg.createReply();
        try {
            Object content = (Object) msg.getContent();
            agent.log("Request received");
            if (content.equals(MessageContent.GET_MAP)) {
                agent.log("GET MAP received");
                //deprecated
                /*if(agent.getGame() == null) {
                	agent.log("Game is null, need to get it from SystemAgent first");
                    reply.setPerformative(ACLMessage.FAILURE);
                } else {
                    reply.setPerformative(ACLMessage.AGREE);
                }*/
                reply.setPerformative(ACLMessage.AGREE);
            }
            if(content.equals(MessageContent.NEW_STEP)) {
            	//TODO: respond with simulation step
            	agent.log("NEW_STEP request message received");
            	reply.setPerformative(ACLMessage.AGREE);
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
    	CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();
        ACLMessage reply = msg.createReply();
        if (reply.getPerformative() != ACLMessage.FAILURE) {
	        reply.setPerformative(ACLMessage.INFORM);
	
	       /* try {
	            reply.setContentObject(agent.getGame());
	        } catch (Exception e) {
	            reply.setPerformative(ACLMessage.FAILURE);
	            agent.errorLog(e.toString());
	            e.printStackTrace();
	        }*/
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
