package agent;

import java.util.ArrayList;
import java.util.List;

import behaviour.TimeoutBehaviour;
import behaviour.digger.coordinator.RequesterBehaviour;
import jade.core.AID;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.ACLMessage;
import onthology.GameSettings;
import onthology.MessageContent;

/**
 * 
 * Digger Coordinator Agent.
 * Gets the initial Game Settings from the CoordinatorAgent, 
 * initializes the Digger Agents and sends directions to the 
 * DiggerAgents according to: New mines found or when a DiggerAgent
 * finishes digging. 
 * Updates the CoordinatorAgent with the 
 * information that each Digger did each turn
 *
 */
public class DiggerCoordinatorAgent extends ImasAgent{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private GameSettings game;
	
	private AID coordinatorAgent;
	
	/**
	 * The Agent has a list of all the digger agents that 
	 * are currently in the map
	 */
	private List<DiggerAgent> diggerAgents;

	public DiggerCoordinatorAgent() {
		super(AgentType.DIGGER_COORDINATOR);
	}

	   /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {

        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.DIGGER_COORDINATOR.toString());
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
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.COORDINATOR.toString());
        this.coordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        // searchAgent is a blocking method, so we will obtain always a correct AID
        setupBehaviours();
        
    }
    
    private void setupBehaviours(){
        ACLMessage initialRequest = new ACLMessage(ACLMessage.REQUEST);
        initialRequest.clearAllReceiver();
        initialRequest.addReceiver(this.coordinatorAgent);
        initialRequest.setProtocol(InteractionProtocol.FIPA_REQUEST);
        log("Request message to agent");
        try {
            initialRequest.setContent(MessageContent.GET_MAP);
            log("Request message content:" + initialRequest.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }

        /**
         * There is a chance that when we connect with the Coordinator agent 
         * he doesnt have the game setting available, for this case
         * we wait using the custom timeout behaviour, and we try again
         * by running the setup.
         */
        SequentialBehaviour seq = new SequentialBehaviour();
        seq.addSubBehaviour(new TimeoutBehaviour(this, 2000));
        seq.addSubBehaviour(new RequesterBehaviour(this, initialRequest) {
			private static final long serialVersionUID = 1L;
			 @Override
			 protected void handleFailure(ACLMessage msg) {
			    DiggerCoordinatorAgent agent = (DiggerCoordinatorAgent) this.getAgent();
			    agent.log("The action has failed.");
			    setupBehaviours();
			 }
        	
        });
        this.addBehaviour(seq);
        
        // setup finished. When we receive the last inform, the agent itself will add
        // a behaviour to send/receive actions
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
    
    public void addDiggerAgent(DiggerAgent diggerAgent) {
    	if(this.diggerAgents == null) {
    		this.diggerAgents = new ArrayList<>();
    	}
    	this.diggerAgents.add(diggerAgent);
    }

    public List<DiggerAgent> getDiggerAgents() {
    	return this.diggerAgents;
    }
    
    public void setDiggerAgents(List<DiggerAgent> diggerAgents) {
    	this.diggerAgents = diggerAgents;
    }
}
