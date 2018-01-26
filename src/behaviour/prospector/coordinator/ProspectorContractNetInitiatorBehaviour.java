package behaviour.prospector.coordinator;

import java.util.Enumeration;
import java.util.Vector;

import agent.ProspectorCoordinatorAgent;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import util.Plan;

public class ProspectorContractNetInitiatorBehaviour extends ContractNetInitiator{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ProspectorCoordinatorAgent agent;
	private int nResponders;

	public ProspectorContractNetInitiatorBehaviour(Agent a, ACLMessage cfp) {
		super(a, cfp);
	}
	
	public ProspectorContractNetInitiatorBehaviour(Agent a, ACLMessage cfp, int nResponders) {
		super(a, cfp);
		this.nResponders = nResponders;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected void handlePropose(ACLMessage propose, Vector v) {
        agent.log("Agent "+propose.getSender().getLocalName()+" proposed "+propose.getContent());
    }
	
	@Override
    protected void handleRefuse(ACLMessage refuse) {
        agent.log("Agent "+refuse.getSender().getLocalName()+" refused");
    }

	@Override
    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            // FAILURE notification from the JADE runtime: the receiver
            // does not exist
            agent.log("Responder does not exist");
        }
        else {
            agent.log("Agent "+failure.getSender().getLocalName()+" failed");
        }
        // Immediate failure --> we will not receive a response from this agent
        nResponders--;
    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        if (responses.size() < nResponders) {
            // Some responder didn't reply within the specified timeout
            agent.log("Timeout expired: missing "+(nResponders - responses.size())+" responses");
        }
        // Evaluate proposals.
        int bestTime = Integer.MAX_VALUE;
        Plan bestProposal = null;
        ACLMessage accept = null;
        Enumeration e = responses.elements();
        while (e.hasMoreElements()) {
            ACLMessage msg = (ACLMessage) e.nextElement();
            if (msg.getPerformative() == ACLMessage.PROPOSE) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                acceptances.addElement(reply);
                try {
					Plan proposal = (Plan) msg.getContentObject();
					
	                int time = proposal.getMovements().size();
	                if (time < bestTime) {
	                    bestTime = time;
	                    bestProposal = proposal;
	                    accept = reply;
	                }
                } catch (UnreadableException e1) {
					e1.printStackTrace();
				}
            }
        }
        // Accept the proposal of the best proposer
        if (accept != null) {
            agent.log("Accepting proposal " + bestProposal + " from " + bestProposal.getAgent().getLocalName());
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        }
    }

	@Override
    protected void handleInform(ACLMessage inform) {
        agent.log("Agent " + inform.getSender().getLocalName() + " successfully performed the requested action");
    }

}
