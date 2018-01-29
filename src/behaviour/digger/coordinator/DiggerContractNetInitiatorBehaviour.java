package behaviour.digger.coordinator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import agent.DiggerAgent;
import agent.DiggerCoordinatorAgent;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import map.Cell;
import map.FieldCell;
import onthology.MetalType;
import util.MetalDiscovery;
import util.Plan;

public class DiggerContractNetInitiatorBehaviour extends ContractNetInitiator{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private DiggerCoordinatorAgent agent;
	private int nResponders;
	private MetalDiscovery metal;

	public DiggerContractNetInitiatorBehaviour(Agent a, ACLMessage cfp) {
		super(a, cfp);
	}
	
	public DiggerContractNetInitiatorBehaviour(DiggerCoordinatorAgent a, ACLMessage cfp, int nResponders,
			MetalDiscovery metal) {
		super(a, cfp);
		this.nResponders = nResponders;
		this.agent = a;
		this.metal = metal;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected void handlePropose(ACLMessage propose, Vector v) {
        agent.log("Agent "+propose.getSender().getLocalName()+" proposed ");
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
        
        ACLMessage accept = null;
        Enumeration e = responses.elements();
        List<ACLMessage> msgs = new LinkedList<>();
        while (e.hasMoreElements()) {
            ACLMessage msg = (ACLMessage) e.nextElement();
            msgs.add(msg);
        }
        //We sort the list, since we may have to form coalitions. The list is sorted by price outcome
        msgs.sort((m1, m2) -> {
			try {
				return Double.compare(((Plan)m1.getContentObject()).getPriceOutcome(),
						((Plan)m2.getContentObject()).getPriceOutcome());
			} catch (UnreadableException e1) {
				e1.printStackTrace();
			}
			return 1;
		});
        
        Plan bestPlan = null;
        List<ACLMessage> acceptedMessages = new LinkedList<>();
        try {
			bestPlan = (Plan) msgs.get(0).getContentObject();
			if(((DiggerAgent)bestPlan.getAgent()).getCapacity() < metal.getCell().getMetal().get(metal.getType())) {
				//the digger cant do it on his own, he needs a coalition
				//we create coalitions and every other individual agents
				List<List<ACLMessage>> combinations = getCombinations(msgs, msgs.size());
				
				//now we look for the best coalition
				//int bestTime = Integer.MAX_VALUE;
				double bestPrice = 0.00;
		        List<ACLMessage> bestProposals = null;
				for(List<ACLMessage> coalition : combinations) {
					//int coalitionTime = 0;
					double coalitionPriceOutcome = 0.00;
					int totalCapacity = 0;
					for(ACLMessage coalMsg : coalition) {
						Plan agentPlan = ((Plan) coalMsg.getContentObject());
						//coalitionTime += agentPlan.getMovements().size();
						coalitionPriceOutcome += agentPlan.getPriceOutcome();
						totalCapacity += ((DiggerAgent) agentPlan.getAgent()).getCapacity();
					}
					//if its the best time, and the mine is going to be depleted
					/*if(coalitionTime < bestTime && totalCapacity >= metal.getCell().getMetal().get(metal.getType())) {
						bestTime = coalitionTime;
						bestProposals = coalition;
					}*/
					//if its the best price outcome,  and the mine is going to be depleted
					if(coalitionPriceOutcome > bestPrice && totalCapacity >= metal.getCell().getMetal().get(metal.getType())) {
						bestPrice = coalitionPriceOutcome;
						bestProposals = coalition;
					}
				}
				//maybe validate not null? but it shouldnt be
				acceptedMessages = bestProposals;
			} else {
				//the digger is fine on his own, hes all grown up
				acceptedMessages.add(msgs.get(0));
			}
		} catch (UnreadableException e1) {
			e1.printStackTrace();
		}
        
        //build replies
        for(ACLMessage msg : msgs) {
        	ACLMessage reply = msg.createReply();
            // Accept the proposal of the best proposer
        	if(acceptedMessages.stream().anyMatch(am -> am.getSender().equals(msg.getSender()))) {
        		reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        	} else {
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);	
        	}
            acceptances.addElement(reply);
        }
    }
	
	private List<List<ACLMessage>> getCombinations(List<ACLMessage> msgs, int size) {
		
		if (0 == size) {
	        return Collections.singletonList(Collections.<ACLMessage> emptyList());
	    }

	    if (msgs.isEmpty()) {
	        return Collections.emptyList();
	    }
		
		List<List<ACLMessage>> combinations = new LinkedList<>();

	    ACLMessage actual = msgs.iterator().next();

	    List<ACLMessage> subSet = new LinkedList<ACLMessage>(msgs);
	    subSet.remove(actual);

	    List<List<ACLMessage>> subSetCombination = getCombinations(subSet, size - 1);

	    for (List<ACLMessage> set : subSetCombination) {
	        List<ACLMessage> newSet = new LinkedList<>(set);
	        newSet.add(0, actual);
	        combinations.add(newSet);
	    }

	    combinations.addAll(getCombinations(subSet, size));

	    return combinations;
	}
	
	@Override
    protected void handleInform(ACLMessage inform) {
        agent.log("Agent " + inform.getSender().getLocalName() + " successfully performed the requested action");
    }
	
	@Override
	public int onEnd() {
		reset();
		return super.onEnd();
	}

}
