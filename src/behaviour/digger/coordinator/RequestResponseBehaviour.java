package behaviour.digger.coordinator;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import agent.CoordinatorAgent;
import agent.DiggerCoordinatorAgent;
import agent.ProspectorAgent;
import agent.ProspectorCoordinatorAgent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import map.Cell;
import onthology.MessageContent;
import util.MetalDiscovery;
import util.Movement;

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
    @SuppressWarnings("unchecked")
	@Override
    protected ACLMessage prepareResponse(ACLMessage msg) {
    	DiggerCoordinatorAgent agent = (DiggerCoordinatorAgent)this.getAgent();
        ACLMessage reply = msg.createReply();
        try {
            Object content = (Object) msg.getContent();
            agent.log("Request received");
            boolean found = false;
            if(content!=null) {
	            if(content.equals(MessageContent.NEW_STEP)) {
	            	agent.log("NEW_STEP request message received");
	            	reply.setPerformative(ACLMessage.AGREE);
	            	agent.informNewStep();
	            	found = true;
	            }
	            if(content.equals(MessageContent.STEP_RESULT)) {
	            	agent.log("STEP_RESULT request message received");
	            	reply.setPerformative(ACLMessage.AGREE);
	            	found = true;
	            }
	            if(!found) {
	            	Object contentObj = (Object) msg.getContentObject();
	            	if(contentObj instanceof List<?>) {
	            		List<?> list = (List<?>) contentObj;
	            		if(list != null && list.size()>0) {
	            			if(list.get(0) instanceof Movement) {
	            				agent.log("APPLY_STEP request message received");
	        	            	reply.setPerformative(ACLMessage.AGREE);
	        	            	agent.informApplyStep((List<Movement>) list);	
	            			}
	            			if(list.get(0) instanceof MetalDiscovery) {
	            				agent.log("MINE_DISCOVERY request message received");
	        	            	reply.setPerformative(ACLMessage.AGREE);;
	        	            	agent.log("new mines size is: " + list.size());
	        	            	agent.informNewMines((List<Cell>) list);
	            				
	            			}
	            		}
	            	}
	            }
            } else {
            	agent.log("Message with null content was from: " + msg.getSender());
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
        	if (reply.getPerformative() != ACLMessage.FAILURE) {
            	if(msg.getContent().equals(MessageContent.STEP_RESULT)) {
    		        reply.setPerformative(ACLMessage.INFORM);
            		try {
            			agent.log("Sending INFORM with: " + agent.getMovements().size());
    					reply.setContentObject((Serializable) agent.getMovements());
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
            	} else {
    		        reply.setPerformative(ACLMessage.INFORM);
    		        agent.log("INFORM message sent");
            	}
            }
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
