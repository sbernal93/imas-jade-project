package behaviour.prospector.coordinator;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import agent.ImasAgent;
import agent.ProspectorCoordinatorAgent;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import util.Plan;

public class ProspectorContractNetInitiatorBehaviour extends SimpleBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ProspectorCoordinatorAgent agent;
	private int nResponders;

	public ProspectorContractNetInitiatorBehaviour(ProspectorCoordinatorAgent agent) {
		super(agent);
		this.agent = agent;
	}

	@Override
	public void action() {
		ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setLanguage(ImasAgent.LANGUAGE);
        msg.setOntology(ImasAgent.ONTOLOGY);
        for (AID prospectorAgent : agent.getProspectorAgents()) {
            msg.addReceiver(prospectorAgent);
        }
        nResponders = agent.getProspectorAgents().size();
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
        //TODO: define objects to send
        //msg.setContentObject();
        agent.addBehaviour(new ContractNetInitiator(agent, msg) {

            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			protected void handlePropose(ACLMessage propose, Vector v) {
                agent.log("Agent "+propose.getSender().getLocalName()+" proposed "+propose.getContent());
            }

            protected void handleRefuse(ACLMessage refuse) {
                agent.log("Agent "+refuse.getSender().getLocalName()+" refused");
            }

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
                        Plan proposal = (Plan) msg.getContentObject();
                        int time = proposal.getMovements().size();
                        if (time < bestTime) {
                            bestTime = time;
                            bestProposal = proposal;
                            accept = reply;
                        }
                    }
                }
                // Accept the proposal of the best proposer
                if (accept != null) {
                    agent.log("Accepting proposal " + bestProposal + " from " + bestProposal.getAgent().getLocalName());
                    accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                }
            }

            protected void handleInform(ACLMessage inform) {
                agent.log("Agent " + inform.getSender().getLocalName() + " successfully performed the requested action");
            }
        });
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
