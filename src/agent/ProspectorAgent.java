package agent;

import java.io.Serializable;

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
import onthology.GameSettings;
import onthology.Movement;

public class ProspectorAgent extends ImasMobileAgent {

	public ProspectorAgent() {
		super(AgentType.PROSPECTOR);
	}

	private GameSettings game;
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
        System.out.println("Prospector agent setup finished");
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        this.addBehaviour(new RequestResponseBehaviour(this, mt));


    }
    

	public Movement newStepResult() {
		//TODO: setup movement, should be taken from a plan
		return new Movement(this, getCell(), getCell());
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public GameSettings getGame() {
		return game;
	}

	public void setGame(GameSettings game) {
		this.game = game;
	}
	
	public void setProspectorCoordinatorAgent(AID prospectorCoordinatorAgent) {
		this.prospectorCoordinatorAgent = prospectorCoordinatorAgent;
	}


}
