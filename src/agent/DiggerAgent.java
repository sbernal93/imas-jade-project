package agent;

import java.util.ArrayList;
import java.util.Optional;

import behaviour.digger.DiggerContractNetResponder;
import behaviour.digger.RequestResponseBehaviour;
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
import onthology.DiggerInfoAgent;
import onthology.GameSettings;
import onthology.InfoAgent;
import util.Movement;
import util.MovementStatus;
import util.MovementType;

/**
 * These agents can bring several types of metal, 
 * but only a single type of metal at a time. 
 * They also have a maximum capacity of units that they can bring.
 * They excavate for metal and bring it to a manufacturing center.
 * They can move, horizontally or vertically, 1 cell per turn.
 * In order to dig metal up, they must be situated in a cell adjacent
 * to the field cell containing metal (horizontally, vertically or diagonally)
 * and remain there for some time (1 turn per metal unit).
 * Each digger can excavate one or more kinds of metal but it can only carry
 * one kind of metal at the same time. Moreover, diggers will have a maximum
 * number of units of metal that they can carry. When they have excavated metal,
 * they can go to dig in another field cell if the maximum number 
 * of units has not been reached or they can go to manufacturing 
 * this amount of metal. To do this, a digger has to be situated in a 
 * cell adjacent to a manufacturing center (horizontally, vertically or diagonally)
 * that allows the kind of metal it is carrying and remain there for some time (1 turn per unit).
 * Several diggers can dispose metals to the same manufacturing center from the same cell,
 * but only a digger can be in a path cell excavating for security concerns.
 *
 */
public class DiggerAgent extends ImasMobileAgent{
	
	private static final long serialVersionUID = 1L;
	
    private AID diggerCoordinatorAgent;
  
    private boolean isDigging;
  
    private boolean isDroppingMetalOff;
  
    private int capacity;
  
    private int carrying;
	

	public DiggerAgent() {
		super(AgentType.DIGGER);
	}

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

	/**
	 * This method is executed once the agent becomes online, it setups the 
	 * basic behaviours and characteristics of the agent
	 */
    @Override
    protected void setup() {
    	//we get the cell that the agent belongs to, the game settings and 
    	//the creator agent as params
    	this.setCell((Cell) this.getArguments()[1]);
    	this.setGame((GameSettings) this.getArguments()[0]);
    	this.setDiggerCoordinatorAgent((AID) this.getArguments()[2]);
    	
    	//initialization
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
        
        //Setup behaviours to respond to FIPA requests and contract nets
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        MessageTemplate cnmt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_CONTRACT_NET), MessageTemplate.MatchPerformative(ACLMessage.CFP));
        
        this.addBehaviour(new RequestResponseBehaviour(this, mt));
        this.addBehaviour(new DiggerContractNetResponder(this, cnmt));

        System.out.println("Digger agent setup finished");
    }
    
    /**
     * A New step has begun, so we must inform what movement we should be doing in that step.
     * If no plan has been set, we return null
     * @return
     */
	public Movement informNewStep() {
		if(this.getPlans().size()>0) {
			return this.getPlans().get(0).getMovements().get(0);
		}
		return null;
	}
	
	/**
	 * The movement was applied. If it was accepted we update the agents info.
	 * If it was rejected, we dont do anyting
	 * @param movement
	 */
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
			}
			if(movementToMake.getType().equals(MovementType.DIGGING)) {
				isDigging = true;
				carrying++;
				if(carrying>=capacity) {
					isDigging = false;
				}
			}
			if(movementToMake.getType().equals(MovementType.DROP_OFF)) {
				isDroppingMetalOff = true;
				carrying --;
				if(carrying <= 0) {
					isDroppingMetalOff = false;
				}
			}
		}
	}


}
