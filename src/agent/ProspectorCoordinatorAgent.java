package agent;

import java.util.ArrayList;
import java.util.List;

import behaviour.BaseRequesterBehaviour;
import behaviour.prospector.coordinator.CreateProspectorAgentBehaviour;
import behaviour.prospector.coordinator.RequestResponseBehaviour;
import jade.core.AID;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import onthology.GameSettings;
import onthology.MessageContent;
import onthology.Movement;

/**
 * 
 * Prospector Coordinator Agent.
 * Gets the initial Game Settings from the CoordinatorAgent, 
 * initializes the Prospector Agents and sends directions to the 
 * them according to: New mines found and new mines created
 * Updates the CoordinatorAgent with the 
 * information that each Propspector did each turn
 *
 */
public class ProspectorCoordinatorAgent extends ImasAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private GameSettings game;
	
	private AID coordinatorAgent;
	

	private List<Movement> movements;
	/**
	 * The Agent has a list of all the prospector agents that 
	 * are currently in the map
	 */
	private List<AID> prospectorAgents;

	public ProspectorCoordinatorAgent() {
		super(AgentType.PROSPECTOR_COORDINATOR);
	}

	   /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {

    	this.setGame((GameSettings) this.getArguments()[0]);
    	this.setCoordinatorAgent((AID) this.getArguments()[1]);
        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.PROSPECTOR_COORDINATOR.toString());
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

        // search CoordinatorAgent
        //commented out, coordinator agent is passed as parameter
       /* ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.COORDINATOR.toString());
        this.coordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);*/
        // searchAgent is a blocking method, so we will obtain always a correct AID
        this.addBehaviour(new CreateProspectorAgentBehaviour(this, AgentType.PROSPECTOR));
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        this.addBehaviour(new RequestResponseBehaviour(this, mt));
    }
    
    public void informNewStep() {
    	this.movements = new ArrayList<>();
    	SequentialBehaviour seq = new SequentialBehaviour();
    	
    	for (AID agent : this.prospectorAgents) {
    		seq.addSubBehaviour(new BaseRequesterBehaviour<ProspectorCoordinatorAgent>(this,
    				buildSimStepMessageForProspectorAgent(agent)) {

    					private static final long serialVersionUID = 1L;
    					
    					@Override
    					protected void handleInform(ACLMessage msg) {
    						((ProspectorCoordinatorAgent) this.getAgent()).log("Inform received from: " + msg.getSender().getName());
        					
    					}
    		});
    	}
    	this.addBehaviour(seq);
    }
    
    private ACLMessage buildSimStepMessageForProspectorAgent(AID agent) {
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.clearAllReceiver();
		message.addReceiver(agent);
		message.setProtocol(InteractionProtocol.FIPA_REQUEST);
		this.log("Request message to a Digger agent");
        try {
        	message.setContent(MessageContent.NEW_STEP);
        	this.log("Request message content:" + message.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
	}
    
    /**
     * Update the game settings.
     *
     * @param game current game settings.
     */
    public void setGame(GameSettings game) {
        this.game = game;
    }

    /**
     * Gets the current game settings.
     *
     * @return the current game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }
    
    public void addProspectorAgent(AID prospectorAgent) {
    	if(this.prospectorAgents == null) {
    		this.prospectorAgents = new ArrayList<>();
    	}
    	this.prospectorAgents.add(prospectorAgent);
    }

    public List<AID> getProspectorAgents() {
    	return this.prospectorAgents;
    }
    
    public void setProspectorAgents(List<AID> prospectorAgents) {
    	this.prospectorAgents = prospectorAgents;
    }

    public void setCoordinatorAgent(AID coordinatorAgent) {
    	this.coordinatorAgent = coordinatorAgent;
    }
    
    public List<Movement> getMovements() {
		return movements;
	}

	public void setMovements(List<Movement> movements) {
		this.movements = movements;
	}
    
	public void addMovement(Movement movement) {
		if (this.movements == null) {
			this.movements = new ArrayList<>();
		}
		this.movements.add(movement);
	}
	
	public void addMovements(List<Movement> movements) {
		if (this.movements == null) {
			this.movements = new ArrayList<>();
		}
		this.movements.addAll(movements);
	}
    

}
