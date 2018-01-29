package agent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import behaviour.prospector.ProspectorContractNetResponder;
import behaviour.prospector.RequestResponseBehaviour;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import map.Cell;
import map.FieldCell;
import map.PathCell;
import onthology.GameSettings;
import onthology.InfoAgent;
import util.Movement;
import util.Plan;
import util.Movement.MovementStatus;

public class ProspectorAgent extends ImasMobileAgent {

	public ProspectorAgent() {
		super(AgentType.PROSPECTOR);
	}
	  /**
     * Prospector Coordinator agent id.
     */
    private AID prospectorCoordinatorAgent;
    
    private List<FieldCell> foundMines;
	
    @Override
    protected void setup() {
    	this.setCell((Cell) this.getArguments()[1]);
    	this.setGame((GameSettings) this.getArguments()[0]);
    	this.setProspectorCoordinatorAgent((AID) this.getArguments()[2]);
    	
        this.setEnabledO2ACommunication(true, 1);

        // Registers the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType((String) this.getArguments()[3]);
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
            log("Registered to the DF");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " registration with DF unsucceeded. Reason: " + e.getMessage());
            doDelete();
        }

        // Searches for the ProspectorCoordinator Agent
        //commented out, not needed coordinator agent is passed as param
       /* ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.PROSPECTOR_COORDINATOR.toString());
        this.prospectorCoordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);*/
        try {
    		//we update our cell so it has the correct AID 
			Optional<InfoAgent> infoAgent = ((PathCell) this.getCell()).getAgents().get(AgentType.PROSPECTOR).stream().filter(a -> a.getAID() == null).findFirst();
			InfoAgent ia = infoAgent.orElse(null);
			if(ia!=null) {
				ia.setAID(this.getAID());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("Prospector agent setup finished");
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        MessageTemplate cnmt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_CONTRACT_NET), MessageTemplate.MatchPerformative(ACLMessage.CFP));
        this.addBehaviour(new RequestResponseBehaviour(this, mt));
        this.addBehaviour(new ProspectorContractNetResponder(this, cnmt));
    }
    

	public Movement informNewStep() {
		//TODO: this method tells us that a new step has started, prospector agent should 
		//send based on his current plan, his next move to the Prospector Coordinator
		return this.getPlans().get(0).getMovements().get(0);
	}
	
	public List<FieldCell> applyNewStep(Movement movement){
		this.foundMines = new ArrayList<>();
		Movement movementToMake = this.getPlans().get(0).getMovements().get(0);
		if(movement.getStatus().equals(MovementStatus.ACCEPTED)) {
			if(this.getPlans().get(0).getMovements().size() == 1) {
				this.getPlans().remove(0);
			} else {
				this.getPlans().get(0).getMovements().remove(0);
			}
			this.setCell(movementToMake.getNewCell());
			this.getGame().getFieldCellsNextTo(this.getCell()).forEach( c -> {
				c.detectMetal();
				if(c.isFound()) {
					this.log("I found a mine!!");
					this.foundMines.add(c);
				}
			});
		} else {
			//TODO: should do something so movement is validated
			this.log("Movement not valid");
		}
		
		return this.foundMines;
	}
	
	
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public void setProspectorCoordinatorAgent(AID prospectorCoordinatorAgent) {
		this.prospectorCoordinatorAgent = prospectorCoordinatorAgent;
	}


	/**
	 * We override the method because we want the prospector agent to repeat the plan set
	 * until the simulation ends
	 */
	@SuppressWarnings("unchecked")
	public void addPlan(Plan plan, List<PathCell> pathReceived) {
		int simSteps = this.getGame().getSimulationSteps();
		
		this.setPlans(new ArrayList<>());
		Movement prevLastMovement = null;
		int movementSteps = 0;
		Plan newPlan = null;
		while(movementSteps <= simSteps) {
			for(PathCell cell :  pathReceived) {
				if(prevLastMovement == null) {
					newPlan = new Plan(this, this.findShortestPath(cell));
					this.getPlans().add(newPlan);
					//prevLastMovement = newPlan.getMovements().get(newPlan.getMovements().size() - 1);
				} else {
					newPlan = new Plan(this, this.findShortestPath(prevLastMovement.getNewCell(), cell));
					this.getPlans().add(newPlan);
				}
				prevLastMovement = newPlan.getMovements().get(newPlan.getMovements().size() - 1);
				movementSteps += newPlan.getMovements().size();
				if(movementSteps >= simSteps) {
					break;
				}
			}
		}
		//this.getPlans().add(plan);
		
/*		int countBacktrack = 1;
		while(movementSteps < simSteps) {
			//we create a copy so we dont modify the original list, we then reverse it
			LinkedList<Movement> prevPlansMovements = (LinkedList<Movement>) this.getPlans().get(this.getPlans().size()-countBacktrack).getMovements();
			LinkedList<Movement> copy = (LinkedList<Movement>) prevPlansMovements.clone();
			Collections.reverse(copy);
			this.getPlans().add(new Plan(this, copy));
			prevPlansMovements = copy;
			movementSteps += copy.size();
			if(countBacktrack - this.getPlans().size() - 2 >= 0) {
				countBacktrack = countBacktrack - 2;
			}
		}*/
		this.log("Plan made");
	}
	


	public List<FieldCell> getFoundMines() {
		return foundMines;
	}


	public void setFoundMines(List<FieldCell> foundMines) {
		this.foundMines = foundMines;
	}



}
