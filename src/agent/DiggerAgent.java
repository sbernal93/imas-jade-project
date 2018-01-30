package agent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Optional;

import behaviour.digger.DiggerContractNetResponder;
import behaviour.digger.RequestResponseBehaviour;
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
import onthology.DiggerInfoAgent;
import onthology.GameSettings;
import onthology.InfoAgent;
import util.Movement;
import util.Plan;
import util.Movement.MovementStatus;
import util.Movement.MovementType;

public class DiggerAgent extends ImasMobileAgent{

	public DiggerAgent() {
		super(AgentType.DIGGER);
	}
	

	  /**
     * Digger Coordinator agent id.
     */
    private AID diggerCoordinatorAgent;
    
    private boolean isDigging;
    
    private boolean isDroppingMetalOff;
    
    private int capacity;
    
    private int carrying;
	
    @Override
    protected void setup() {
    	this.setCell((Cell) this.getArguments()[1]);
    	this.setGame((GameSettings) this.getArguments()[0]);
    	this.setDiggerCoordinatorAgent((AID) this.getArguments()[2]);
    	this.setPlans(new ArrayList<>());
    	isDigging = false;
    	carrying = 0;
        this.setEnabledO2ACommunication(true, 1);

        // Registers the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType((String)this.getArguments()[3]);
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

        // Searches for the DiggerCoordinator Agent
        //commented out, coordinator agent is passed as param
        /*
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.DIGGER_COORDINATOR.toString());
        this.diggerCoordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);*/
        try {
    		//we update our cell so it has the correct AID 
			Optional<InfoAgent> infoAgent = ((PathCell) this.getCell()).getAgents().get(AgentType.DIGGER).stream().filter(a -> a.getAID() == null).findFirst();
			InfoAgent ia = infoAgent.orElse(null);
			if(ia!=null) {
				ia.setAID(this.getAID());
				this.setCapacity(((DiggerInfoAgent) ia).getCapacity());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        MessageTemplate cnmt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_CONTRACT_NET), MessageTemplate.MatchPerformative(ACLMessage.CFP));
        
        this.addBehaviour(new RequestResponseBehaviour(this, mt));
        this.addBehaviour(new DiggerContractNetResponder(this, cnmt));

        System.out.println("Digger agent setup finished");


    }
    
	public Movement informNewStep() {
		if(this.getPlans().size()>0) {
			return this.getPlans().get(0).getMovements().get(0);
		}
		return null;
	}
	
	public void applyNewStep(Movement movement) {
		//if this method is called, it means we attempted to make a movement,
		//so no need to validate that plans size
		Movement movementToMake = this.getPlans().get(0).getMovements().get(0);
		if(movement.getStatus().equals(MovementStatus.ACCEPTED)) {
			//first we delete it from the queue, it was accepted
			if(this.getPlans().get(0).getMovements().size() == 1) {
				this.getPlans().remove(0);
			} else {
				this.getPlans().get(0).getMovements().remove(0);
			}
			if(movementToMake.getType().equals(MovementType.NORMAL)) {
				isDigging = false;
				isDroppingMetalOff = false;
				this.setCell(movementToMake.getNewCell());
				log("Moving around");
			}
			if(movementToMake.getType().equals(MovementType.DIGGING)) {
				//TODO: substract from mine? or should system agent do that?
				//yes! exactly, system agent does that!
				log("Diggin the night away, my capacity is: " + capacity + " and Im carrying: " + carrying);
				isDigging = true;
				carrying++;
				if(carrying>=capacity) {
					isDigging = false;
				}
			}
			if(movementToMake.getType().equals(MovementType.DROP_OFF)) {
				//TODO: substract amount of metal being carried
				log("Dropping off a huge load: " + carrying);
				isDroppingMetalOff = true;
				carrying --;
				if(carrying <= 0) {
					isDroppingMetalOff = false;
				}
			}
		}
	}



	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public AID getDiggerCoordinatorAgent() {
		return diggerCoordinatorAgent;
	}

	public void setDiggerCoordinatorAgent(AID diggerCoordinatorAgent) {
		this.diggerCoordinatorAgent = diggerCoordinatorAgent;
	}

	public boolean isDigging() {
		return isDigging;
	}

	public void setDigging(boolean isDigging) {
		this.isDigging = isDigging;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getCarrying() {
		return carrying;
	}

	public void setCarrying(int carrying) {
		this.carrying = carrying;
	}

	public boolean isDroppingMetalOff() {
		return isDroppingMetalOff;
	}

	public void setDroppingMetalOff(boolean isDroppingMetalOff) {
		this.isDroppingMetalOff = isDroppingMetalOff;
	}


}
