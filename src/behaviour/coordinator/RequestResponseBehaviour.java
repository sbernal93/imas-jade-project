package behaviour.coordinator;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import agent.CoordinatorAgent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import map.Cell;
import onthology.MessageContent;
import util.Movement;

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
    @SuppressWarnings("unchecked")
	@Override
    protected ACLMessage prepareResponse(ACLMessage msg) {
    	CoordinatorAgent agent = (CoordinatorAgent)this.getAgent();
        ACLMessage reply = msg.createReply();
        try {
            Object content = (Object) msg.getContent();
            boolean found = false;
            agent.log("Request received");
            if(content!=null) {
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
	                found = true;
	            }
	            if(content.equals(MessageContent.NEW_STEP)) {
	            	agent.log("NEW_STEP request message received");
	            	reply.setPerformative(ACLMessage.AGREE);
	                found = true;
	            }
	            if(content.equals(MessageContent.STEP_FINISHED)) {
	            	agent.log("STEP_FINISHED request message received");
	            	reply.setPerformative(ACLMessage.AGREE);
	                found = true;
	            }
	            if(content.equals(MessageContent.STEP_RESULT)) {
	            	agent.log("STEP_RESULT request message received");
	            	reply.setPerformative(ACLMessage.AGREE);
	                found = true;
	            }
	            /*if(content.equals(MessageContent.APPLY_STEP)) {
	            	agent.log("APPLY_STEP request message received");
	            	reply.setPerformative(ACLMessage.AGREE);
	            	agent.informApplyStep();
	            }*/
	            if(content.equals(MessageContent.APPLY_STEP_FINISHED)) {
	            	agent.log("APPLY_STEP_FINISHED request message received");
	            	reply.setPerformative(ACLMessage.AGREE);
	            	if(msg.getSender().equals(agent.getDiggerCoordinatorAgent())){
	            		agent.setDcApplyStepFinished(true);
	            	} else {
	            		agent.setPcApplyStepFinished(true);
	            	}
	            	agent.applyStepFinished();
	                found = true;
	            }
	            if(content.equals(MessageContent.MINE_DISCOVERY)) {
	            	//TODO
	            	agent.log("MINE_DISCOVERY request message received");
	            	reply.setPerformative(ACLMessage.AGREE);
	            	if(msg.getSender().equals(agent.getDiggerCoordinatorAgent())) {
	            		agent.setDcInformNewMinesFinished(true);
	            		agent.applyStepFinished();
	            	}
	            	if(msg.getSender().equals(agent.getProspectorCoordinatorAgent())) {
	            		agent.setDcInformNewMinesFinished(true);
	            		agent.applyStepFinished();
	            	}
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
	            			if(list.get(0) instanceof Cell) {
	            				agent.log("MINE_DISCOVERY request message received");
	        	            	reply.setPerformative(ACLMessage.AGREE);;
	        	            	agent.log("new mines size is: " + list.size());
	        	            	agent.informNewMines((List<Cell>) list);
	            				
	            			}
	            		} else {
	            			//TODO validate this, no new mines received so its finished
	            			if(msg.getSender().equals(agent.getProspectorCoordinatorAgent())) {
		            			agent.setDcInformNewMinesFinished(true);
		            			agent.applyStepFinished();
	            			}
	            		}
	            	}
	            }
            } else {
            	agent.log("Got null content from: " + msg.getSender());
            	agent.log("failure message");
                reply.setPerformative(ACLMessage.AGREE);
            }
        } catch (Exception e) {
        	agent.log("failure message");
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
    	Object content = (Object) msg.getContent();
        ACLMessage reply = msg.createReply();
        if (reply.getPerformative() != ACLMessage.FAILURE) {
	        reply.setPerformative(ACLMessage.INFORM);
	        if(content.equals(MessageContent.NEW_STEP)) {
	        	agent.informNewStep();
	        }
	        if(content.equals(MessageContent.STEP_FINISHED)) {
	        	if(msg.getSender().equals(agent.getProspectorCoordinatorAgent())) {
		        	agent.requestStepResultFromProspectorCoordinator();
	        	} else {
	        		agent.requestStepResultFromDiggerCoordinator();
	        	}
	        }
	        if(content.equals(MessageContent.STEP_RESULT)) {
	        	try {
	        		reply.setContentObject((Serializable) agent.getMovements());
				} catch (IOException e) {
					e.printStackTrace();
				}
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
