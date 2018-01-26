package agent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
        System.out.println("Prospector agent setup finished");
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        this.addBehaviour(new RequestResponseBehaviour(this, mt));


    }
    

	public void informNewStep() {
		//TODO: this method tells us that a new step has started, prospector agent should 
		//send based on his current plan, his next move to the Prospector Coordinator
	}
	
	public void applyNewStep(){
		//TODO: this method tells us that the movement was accepted and we can apply it 
		//so we apply the first movement in the current plan (which should be the first
		//plan on the list), and the agent moves 
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public void setProspectorCoordinatorAgent(AID prospectorCoordinatorAgent) {
		this.prospectorCoordinatorAgent = prospectorCoordinatorAgent;
	}



}
