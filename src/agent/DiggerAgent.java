package agent;

import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import map.Cell;
import onthology.GameSettings;

public class DiggerAgent extends ImasMobileAgent{

	public DiggerAgent() {
		super(AgentType.DIGGER);
	}
	
	private GameSettings game;
	  /**
     * Digger Coordinator agent id.
     */
    private AID diggerCoordinatorAgent;
	
    @Override
    protected void setup() {
    	this.setCell((Cell) this.getArguments()[1]);
    	this.setGame((GameSettings) this.getArguments()[0]);
    	
        this.setEnabledO2ACommunication(true, 1);

        // Registers the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.DIGGER.toString());
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
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.DIGGER_COORDINATOR.toString());
        this.diggerCoordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        System.out.println("Digger agent setup finished");


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
	
	

}
