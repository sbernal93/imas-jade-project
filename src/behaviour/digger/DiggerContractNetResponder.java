package behaviour.digger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import agent.DiggerAgent;
import agent.ProspectorAgent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import map.FieldCell;
import map.PathCell;
import util.Movement;
import util.Plan;

public class DiggerContractNetResponder extends ContractNetResponder{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private DiggerAgent agent;
	private Plan planProposed;
	
	public DiggerContractNetResponder(DiggerAgent a, MessageTemplate mt) {
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
		agent.addPlan(planProposed);
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
			FieldCell mine = (FieldCell) cfp.getContentObject();
			List<PathCell> pathCells = this.agent.getGame().getPathCellsNextTo(mine);
			
			List<Movement> movements = this.agent.findShortestPath(pathCells.get(0));
			//TODO: add movement to manufacturing centers
			//finds closest path cell
			for(PathCell pc : pathCells) {
				List<Movement> pcMovements = this.agent.findShortestPath(pc);
				if(pcMovements.size()<movements.size()) {
					movements = pcMovements;
				}
			}
			planProposed = new Plan(agent, movements);
			
			ACLMessage proposal = cfp.createReply();
			proposal.setPerformative(ACLMessage.PROPOSE);
			proposal.setSender(agent.getAID());
			agent.log("Returning created proposal");
			if(this.agent.getPlans() == null || this.agent.getPlans().isEmpty()) {
				proposal.setContentObject(planProposed);
			} else {
				List<Movement> allMovs = new LinkedList<>();
				this.agent.getPlans().stream().forEach(p -> {
					allMovs.addAll(p.getMovements());
				});
				proposal.setContentObject(new Plan(this.agent, allMovs));
			}
			return proposal;
		} catch (UnreadableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return super.handleCfp(cfp);
	}
	
	
	
	
}
