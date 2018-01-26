package agent;

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
import onthology.GameSettings;
import util.Movement;
import util.Plan;

public class DiggerAgent extends ImasMobileAgent{

	public DiggerAgent() {
		super(AgentType.DIGGER);
	}
	

	  /**
     * Digger Coordinator agent id.
     */
    private AID diggerCoordinatorAgent;
	
    @Override
    protected void setup() {
    	this.setCell((Cell) this.getArguments()[1]);
    	this.setGame((GameSettings) this.getArguments()[0]);
    	this.setDiggerCoordinatorAgent((AID) this.getArguments()[2]);
    	
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
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        this.addBehaviour(new RequestResponseBehaviour(this, mt));

        System.out.println("Digger agent setup finished");


    }
    
	public void informNewStep() {
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

}
