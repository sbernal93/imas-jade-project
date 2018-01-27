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
import map.PathCell;
import onthology.GameSettings;
import onthology.InfoAgent;
import util.Movement;
import util.Plan;

public class ProspectorAgent extends ImasMobileAgent {

	public ProspectorAgent() {
		super(AgentType.PROSPECTOR);
	}
	  /**
     * Prospector Coordinator agent id.
     */
    private AID prospectorCoordinatorAgent;
	
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
	
	public void applyNewStep(){
		Movement movementToMake = this.getPlans().get(0).getMovements().get(0);
		try {
			this.getGame().moveAgent(movementToMake);
			if(this.getPlans().get(0).getMovements().size() == 1) {
				this.getPlans().remove(0);
			} else {
				this.getPlans().get(0).getMovements().remove(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	@Override
	public void addPlan(Plan plan) {
		int simSteps = this.getGame().getSimulationSteps();
		int movementSteps = plan.getMovements().size();
		this.setPlans(new ArrayList<>());
		this.getPlans().add(plan);
		LinkedList<Movement> prevPlansMovements = (LinkedList<Movement>) plan.getMovements();
		while(movementSteps < simSteps) {
			//we create a copy so we dont modify the original list, we then reverse it
			LinkedList<Movement> copy = (LinkedList<Movement>) prevPlansMovements.clone();
			Collections.reverse(copy);
			this.getPlans().add(new Plan(this, copy));
			prevPlansMovements = copy;
			movementSteps += copy.size();
		}
	}



}
