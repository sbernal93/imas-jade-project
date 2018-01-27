package agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import behaviour.BaseRequesterBehaviour;
import behaviour.prospector.coordinator.CreateProspectorAgentBehaviour;
import behaviour.prospector.coordinator.ProspectorContractNetInitiatorBehaviour;
import behaviour.prospector.coordinator.RequestResponseBehaviour;
import jade.core.AID;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import map.PathCell;
import onthology.GameSettings;
import onthology.MessageContent;
import util.Movement;

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
	
	private boolean firstStep;
	

	private List<Movement> movements;
	/**
	 * The Agent has a list of all the prospector agents that 
	 * are currently in the map
	 */
	private List<AID> prospectorAgents;

	public ProspectorCoordinatorAgent() {
		super(AgentType.PROSPECTOR_COORDINATOR);
		this.firstStep = true;
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
    	if(!firstStep) {
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
    	} else {
    		try {
    			this.log("Attempting contract net");
				performContractNet();
				this.firstStep = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }
    
    /**
     * How it works: For contract net in the exploration of field cells, we would have the following structure:
	 *  -Recognition: Prospector coordinator groups the path cells next to field cells by the amount of prospector agents
	 *  -Announcement: For each group, make a broadcast to the prospectors
	 *  -Bidding: Each prospector responds with a “tender” (a {@link Plan}) if they currently don’t have a task
	 *  -Awarding: Assign the group of field cells to the best bidder
	 *  -Expediting: Prospector coordinator tells the agent to do the task
	 *  -Announce the next group of field cells 
	 * Note: prospector agents will explore its assigned group of field cells indefinitely 
	 * Pros: All the prospectors will have a list of field cells to discover so they won’t need to do any extra calculations
	 * Cons: Not optimal (shortest total distance), may not choose the optimal distance to a group since the 
	 * choice is selected one by one. But for this case this isn’t very important since the priority is to explore all the field cells.
     * @throws IOException 
     */
    public void performContractNet() throws IOException{
    	SequentialBehaviour seq = new SequentialBehaviour();
        int nResponders = this.getProspectorAgents().size();
    	List<PathCell> pathCellsToExplore = game.getPathCellsNextToFieldCells();
    	double size = Math.ceil(pathCellsToExplore.size() / Double.valueOf(nResponders));
    	List<List<PathCell>> splitted = UtilsAgents.splitList(pathCellsToExplore, (int) size);
    	for(List<PathCell> group : splitted) {
    		ACLMessage msg = new ACLMessage(ACLMessage.CFP);
            msg.setLanguage(ImasAgent.LANGUAGE);
            msg.setOntology(ImasAgent.ONTOLOGY);
            for (AID prospectorAgent : this.getProspectorAgents()) {
                msg.addReceiver(prospectorAgent);
            }
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
            //Sets the first path cell of the group to be sent, the prospectors will respond with
            //their distance to that field cell
            msg.setContentObject(group.get(0));
            seq.addSubBehaviour(new ProspectorContractNetInitiatorBehaviour(this, msg, nResponders));
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
