package agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import behaviour.prospector.ProspectorContractNetResponder;
import behaviour.prospector.RequestResponseBehaviour;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames.InteractionProtocol;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import map.Cell;
import map.PathCell;
import onthology.GameSettings;
import onthology.InfoAgent;
import util.MetalDiscovery;
import util.Movement;
import util.MovementStatus;
import util.Plan;

/**
 * Prospector Agents are the agents that are going to be exploring certain 
 * path cells. If the find themselves next to a mine that hasnt been found,
 * it informs the ProspectorCoordinator of the metal discovery
 *
 */
public class ProspectorAgent extends ImasMobileAgent {

	private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
	private AID prospectorCoordinatorAgent;
    private List<MetalDiscovery> foundMines;

	public ProspectorAgent() {
		super(AgentType.PROSPECTOR);
	}
	
	public List<MetalDiscovery> getFoundMines() {
		return foundMines;
	}

	public void setFoundMines(List<MetalDiscovery> foundMines) {
		this.foundMines = foundMines;
	}
	
	public void setProspectorCoordinatorAgent(AID prospectorCoordinatorAgent) {
		this.prospectorCoordinatorAgent = prospectorCoordinatorAgent;
	}

	/**
	 * Setup for the agent when it becomes online, also setups starter behaviours to handle with
	 * incoming messages
	 */
    @Override
    protected void setup() {
    	//Receives the cell he is on, the game settings and the creator as arguments
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
        //Setup listeners to FIPA request and contract net
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        MessageTemplate cnmt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_CONTRACT_NET), MessageTemplate.MatchPerformative(ACLMessage.CFP));
        this.addBehaviour(new RequestResponseBehaviour(this, mt));
        this.addBehaviour(new ProspectorContractNetResponder(this, cnmt));
    }
    

    /**
     * this method tells us that a new step has started, prospector agent should 
	 * send based on his current plan, his next move to the Prospector Coordinator
     * @return
     */
	public Movement informNewStep() {
		return this.getPlans().get(0).getMovements().get(0);
	}
	
	/**
	 * Applies a new step if validated, checks if there are any new mines
	 * to be found
	 * @param movement
	 * @return
	 */
	public List<MetalDiscovery> applyNewStep(Movement movement){
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
				if(!c.isFound()) {
					c.detectMetal();
					if(c.isFound()) {
						this.log("I found a mine!!");
						c.getMetal().forEach((k,v) -> {
							this.foundMines.add(new MetalDiscovery(c, k, v));
						});
					}
				}
			});
		} else {
			this.log("Movement not valid");
		}
		return this.foundMines;
	}
	
	/**
	 * We override the method because we want the prospector agent to repeat the plan set
	 * until the simulation ends
	 */
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
		this.log("Plan made");
	}


}
