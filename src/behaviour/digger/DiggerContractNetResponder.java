package behaviour.digger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import agent.AgentType;
import agent.DiggerAgent;
import agent.ProspectorAgent;
import agent.UtilsAgents;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import map.Cell;
import map.FieldCell;
import map.ManufacturingCenterCell;
import map.PathCell;
import util.MetalDiscovery;
import util.Movement;
import util.MovementType;
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
			
			MetalDiscovery mine = (MetalDiscovery) cfp.getContentObject();
			List<PathCell> pathCells = this.agent.getGame().getPathCellsNextTo(mine.getCell());
			
			List<Movement> movements = null;
			//TODO: validate metal type. Although the way we are doing this is that 
			//an agents plan is moving to the mine, digging and dropping off,
			//so an agent will be able to dig any mine received because he isnt going to be 
			//carrying anything after the plan is finished
			
			//finds closest path cell
			//if he has a plan already set, the plan should be made starting from
			//the last cell in that plan
			Cell currentCell = null;
			if(this.agent.getPlans() != null && this.agent.getPlans().size()>0) {
				Plan lastPlan =  this.agent.getPlans().get(this.agent.getPlans().size() - 1);
				currentCell = lastPlan.getMovements().get(lastPlan.getMovements().size() - 1).getNewCell();
			} else {
				currentCell = this.agent.getCell();
			}
			//finds the closest path cell to the mine
			for(PathCell pc : pathCells) {
				List<Movement> pcMovements = this.agent.findShortestPath(currentCell, pc);
				if(movements == null) {
					movements = pcMovements;
				} else {
					if(pcMovements.size()<movements.size()) {
						movements = pcMovements;
					}
				}
			}
			//now that movement to the mine is made, we need to add movements for digging the metal
			Cell diggingCell = null;
			if(movements.size()>0) {
				diggingCell = movements.get(movements.size() - 1).getNewCell();
			} else {
				diggingCell = currentCell;
			}
			int unitsDugUp = 0;
			for(int i=1; i<= mine.getAmount(); i++) {
				if(unitsDugUp>=(this.agent.getCapacity() - this.agent.getCarrying())) {
					break;
				}
				movements.add(new Movement(this.agent.getAID(), diggingCell, diggingCell,  MovementType.DIGGING, mine, AgentType.DIGGER));
				unitsDugUp++;
			}
			if(unitsDugUp<mine.getAmount()) {
				//TODO: considering we are using coalitions, we shouldnt need this,
				//leaving this here still in case we want to do something clever.
				//maybe skip going to MC? but then what price should we bid
			} 
			//the digger dug up all he could, now to go to the nearest manufacturing center
			//consider best manufacturing center based on price/movement
			List<Cell> manufacturingCenters = this.agent.getGame().getManufacturingCenters(mine.getType());
			
			List<Movement> movementsToManufacturingCenter = new ArrayList<>();
			double bestPrice = 0.0;
			Cell bestMc = null;
			
			List<Movement> allMovs = new LinkedList<>();
			this.agent.getPlans().stream().forEach(p -> {
				allMovs.addAll(p.getMovements());
			});
			
			//we look for the best manufacturing center to go based on price/distance

			List<Movement> closestPath = null;
			List<PathCell> pathCellsNextToManCenter = null ;
			List<Movement> movementsToManCenter = null;
			int price = 0;
			Double calcPrice = 0.000;
			
			for(Cell manCenter : manufacturingCenters) {
				pathCellsNextToManCenter = this.agent.getGame().getPathCellsNextTo(manCenter);
				closestPath = null;
				movementsToManCenter = new LinkedList<>();
				//first we need the fastest way to get there
				for(PathCell pc : pathCellsNextToManCenter) {
					if(pc.getRow() != diggingCell.getRow() || pc.getCol() != diggingCell.getCol()) {
						movementsToManCenter = this.agent.findShortestPath(diggingCell, pc);
					} 
					if(closestPath == null) {
						closestPath = movementsToManCenter;
					} else {
						if(movementsToManCenter.size() < closestPath.size()) {
							closestPath = movementsToManCenter;
						}
					}
				}
				//now we look for the best price/distance
				price = ((ManufacturingCenterCell) manCenter).getPrice();
				//we add also all the moves we have to make currently, since its going to change
				//the price/movement outcome
			/*	this.agent.log("AllMovs: " + allMovs.size() + " movs: " +movements.size() + " closest" + closestPath.size());
				this.agent.log("mine amount: " + mine.getAmount());
				this.agent.log("price: " + price);*/
				calcPrice = UtilsAgents.calculatePrice(allMovs.size() + movements.size() + closestPath.size(), unitsDugUp, price);
				//this.agent.log("Calculated price outcome: " + calcPrice);
				if( calcPrice > bestPrice) {
					bestPrice = calcPrice;
					movementsToManufacturingCenter = closestPath;
					bestMc = manCenter;
				}
				
			}
			movements.addAll(movementsToManufacturingCenter);
			Cell dropOffCell = diggingCell;
			if(movementsToManufacturingCenter.size() > 0) {
				dropOffCell =  movementsToManufacturingCenter.get(movementsToManufacturingCenter.size() - 1).getNewCell();
			}
			
			//add all the movements to drop off metals
			for(int i=1; i<= unitsDugUp; i++) {
				movements.add(new Movement(this.agent.getAID(), dropOffCell, dropOffCell, bestMc,  MovementType.DROP_OFF, AgentType.DIGGER));
			}
			
			//we propose the plan with the movements to the mine and the MC, and the amount earned
			planProposed = new Plan(agent, movements, bestPrice);
			
			ACLMessage proposal = cfp.createReply();
			proposal.setPerformative(ACLMessage.PROPOSE);
			proposal.setSender(agent.getAID());
			agent.log("Returning created proposal");
			
			//the plan sent doesnt need movements, could lighten the load sent
			proposal.setContentObject(new Plan(agent, null, bestPrice));
			/*if(this.agent.getPlans() == null || this.agent.getPlans().isEmpty()) {
				proposal.setContentObject(planProposed);
			} else {
				allMovs.addAll(movements);
				proposal.setContentObject(new Plan(this.agent, allMovs, bestPrice));
			}*/
			agent.log("Proposal with price: " + bestPrice);
			return proposal;
		} catch (UnreadableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return super.handleCfp(cfp);
	}
	
	@Override
	public int onEnd() {
		reset();
		return super.onEnd();
	}
	

}
