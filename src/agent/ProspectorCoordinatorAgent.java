package agent;

import java.util.ArrayList;
import java.util.List;

import behaviour.prospector.coordinator.CreateProspectorAgentBehaviour;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import onthology.GameSettings;

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
	
	/**
	 * The Agent has a list of all the prospector agents that 
	 * are currently in the map
	 */
	private List<ProspectorAgent> prospectorAgents;

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
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.COORDINATOR.toString());
        this.coordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        // searchAgent is a blocking method, so we will obtain always a correct AID
        this.addBehaviour(new CreateProspectorAgentBehaviour(this, AgentType.PROSPECTOR));

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
    
    public void addProspectorAgent(ProspectorAgent prospectorAgent) {
    	if(this.prospectorAgents == null) {
    		this.prospectorAgents = new ArrayList<>();
    	}
    	this.prospectorAgents.add(prospectorAgent);
    }

    public List<ProspectorAgent> getProspectorAgents() {
    	return this.prospectorAgents;
    }
    
    public void setProspectorAgents(List<ProspectorAgent> prospectorAgents) {
    	this.prospectorAgents = prospectorAgents;
    }
	

}
