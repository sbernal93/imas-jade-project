package behaviour.prospector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import agent.ProspectorAgent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import map.PathCell;
import util.Movement;
import util.Plan;

public class ProspectorContractNetResponder extends ContractNetResponder{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ProspectorAgent agent;
	private Plan planProposed;
	private List<PathCell> pathsReceived;
	
	public ProspectorContractNetResponder(ProspectorAgent a, MessageTemplate mt) {
		super(a, mt);
		this.agent = a;
	}

	/**
	 * Proposal was accepted, so we set it as the current plan
	 */
	@Override
	protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept)
			throws FailureException {
		agent.log("Proposal accepted");
		ACLMessage inform = accept.createReply();
        inform.setPerformative(ACLMessage.INFORM);
		if(planProposed.getMovements().size()>= agent.getGame().getSimulationSteps() ) {
			//shouldnt enter here but just in case, it should ignore the plan 
			//TODO: validate that it never enters here, and if it does, then handle this better
			//since it means some cells arent going to be explored
			agent.log("DANGER: proposed plan was not supposed to be accepted.");
			return inform;
		}
		agent.addPlan(planProposed, pathsReceived);
		return inform;
	}

	/**
	 * Sends a proposal of a plan to the prospector coordinator based on the 
	 * {@linkplain PathCell} received
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
		try {
			if(agent.getPlans() != null && !agent.getPlans().isEmpty()) {
				//sets a plan with a high number of movements so its not considered
				//TODO: this could be done more pretty, kinda hacky
				List<Movement> movements = new ArrayList<>();
				for(Plan plan : agent.getPlans()) {
					movements.addAll(plan.getMovements());
				}
				agent.log("Proposing plan with high number of steps so its rejected");
				planProposed = new Plan(agent, movements);
			} else {
				this.pathsReceived = (List<PathCell>) cfp.getContentObject();
				List<Movement> path = this.agent.findShortestPath(this.pathsReceived.get(0));
				agent.log("Proposing a plan with: " + path.size() + " steps");
				planProposed = new Plan(agent, path);
			}
			ACLMessage proposal = cfp.createReply();
			proposal.setPerformative(ACLMessage.PROPOSE);
			proposal.setContentObject(planProposed);
			//proposal.setContent("Size: "+ planProposed.getMovements().size());
			proposal.setSender(agent.getAID());
			agent.log("Returning created proposal");
			return proposal;
		} catch (UnreadableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return super.handleCfp(cfp);
	}
	
	
	
	
}
