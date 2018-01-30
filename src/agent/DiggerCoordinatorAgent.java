package agent;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import behaviour.BaseRequesterBehaviour;
import behaviour.digger.coordinator.CreateDiggerAgentBehaviour;
import behaviour.digger.coordinator.DiggerContractNetInitiatorBehaviour;
import behaviour.digger.coordinator.RequestResponseBehaviour;
import jade.core.AID;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPANames.InteractionProtocol;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import onthology.GameSettings;
import onthology.MessageContent;
import util.MetalDiscovery;
import util.Movement;

/**
 * 
 * Digger Coordinator Agent.
 * Gets the initial Game Settings from the CoordinatorAgent, 
 * initializes the Digger Agents and sets up Coalition formations 
 * when a new mine is found
 * Updates the CoordinatorAgent with the 
 * information that each Digger did each turn
 *
 */
public class DiggerCoordinatorAgent extends ImasAgent{
	
	private static final long serialVersionUID = 1L;
	
	private GameSettings game;
	
	private AID coordinatorAgent;
	
	private List<Movement> movements;

	private List<AID> diggerAgents;

	public DiggerCoordinatorAgent() {
		super(AgentType.DIGGER_COORDINATOR);
	}

    public void setGame(GameSettings game) {
        this.game = game;
    }

    public GameSettings getGame() {
        return this.game;
    }
    
    public void addDiggerAgent(AID diggerAgent) {
    	if(this.diggerAgents == null) {
    		this.diggerAgents = new ArrayList<>();
    	}
    	this.diggerAgents.add(diggerAgent);
    }

    public List<AID> getDiggerAgents() {
    	return this.diggerAgents;
    }
    
    public void setDiggerAgents(List<AID> diggerAgents) {
    	this.diggerAgents = diggerAgents;
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

	public AID getCoordinatorAgent() {
		return coordinatorAgent;
	}

	 /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     * Setups behaviours to create digger agents and respond to FIPA Requests
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

        this.addBehaviour(new CreateDiggerAgentBehaviour(this, AgentType.DIGGER));
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        this.addBehaviour(new RequestResponseBehaviour(this, mt));
    }
    
    /**
     * A New step has started, so we inform all the diggers
     */
    public void informNewStep() {
    	this.movements = new ArrayList<>();
    	SequentialBehaviour seq = new SequentialBehaviour();
    	
    	for (AID agent : this.diggerAgents) {
    		seq.addSubBehaviour(new BaseRequesterBehaviour<DiggerCoordinatorAgent>(this,
    				UtilsAgents.buildMessage(agent, MessageContent.NEW_STEP)) {

    					private static final long serialVersionUID = 1L;
    					
    					@Override
    					protected void handleInform(ACLMessage msg) {
    						this.getTypeAgent().log("Inform received from: " + msg.getSender().getName());
    						try {
    							//a digger agent may not me moving
    							Movement mov = (Movement) Optional.ofNullable(msg.getContentObject()).orElse(null);
    							if(mov !=null ) {
    								this.getTypeAgent().addMovement((Movement) msg.getContentObject());
    							}
							} catch (UnreadableException e) {
								e.printStackTrace();
							}
    					}
    		});
    	}
    	//after receiving the information of the step from the diggers, we let the coordinator know
    	seq.addSubBehaviour(new BaseRequesterBehaviour<DiggerCoordinatorAgent>(this, 
    			UtilsAgents.buildMessage(this.coordinatorAgent, MessageContent.STEP_FINISHED)) {

					private static final long serialVersionUID = 1L;
		});
    	this.addBehaviour(seq);
    }

    /**
     * The step was applied, we inform the diggers that attempted a move, if any
     * @param list
     */
	public void informApplyStep(List<Movement> list) {
		SequentialBehaviour seq = new SequentialBehaviour();
		for(Movement movement :  list) {
			if(movement.getAgentType().equals(AgentType.DIGGER)) {
				seq.addSubBehaviour(new BaseRequesterBehaviour<DiggerCoordinatorAgent>(this, 
						UtilsAgents.buildMessageWithObj(movement.getAgent(),MessageContent.APPLY_STEP, movement)) {
							private static final long serialVersionUID = 1L;
							
							@Override
							protected void handleInform(ACLMessage msg) {
								this.getTypeAgent().log("Inform received");
								super.handleInform(msg);
							}
					
				});
			}
		}
		//after informing the diggers, we notify the coordinator that we are done
		seq.addSubBehaviour(new BaseRequesterBehaviour<DiggerCoordinatorAgent>(this,
    			UtilsAgents.buildMessage(this.coordinatorAgent, MessageContent.APPLY_STEP_FINISHED)) {
			private static final long serialVersionUID = 1L;

		});
    	this.addBehaviour(seq);	
	}

	/**
	 * A new mine was discovered, we setup a contract net and decide who is going to
	 * dig it for each mine discovered this turn
	 * @param list
	 */
	public void informNewMines(List<MetalDiscovery> list) {
		SequentialBehaviour seq = new SequentialBehaviour();
		int nResponders = this.getDiggerAgents().size();
    	for(MetalDiscovery metal : list) {
    		ACLMessage msg = new ACLMessage(ACLMessage.CFP);
            msg.setLanguage(ImasAgent.LANGUAGE);
            msg.setOntology(ImasAgent.ONTOLOGY);
            for (AID diggerAgent : this.getDiggerAgents()) {
                msg.addReceiver(diggerAgent);
            }
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            msg.setReplyByDate(new Date(System.currentTimeMillis() + 50000));
            try {
				msg.setContentObject((Serializable) metal);
			} catch (IOException e) {
				e.printStackTrace();
			}
            seq.addSubBehaviour(new DiggerContractNetInitiatorBehaviour(this, msg, nResponders, metal));
    	}
    	//after setting up plans with diggers, we inform the Coordinator agent that 
    	//we finished
		seq.addSubBehaviour(new BaseRequesterBehaviour<DiggerCoordinatorAgent>(this,
    			UtilsAgents.buildMessage(this.coordinatorAgent, MessageContent.MINE_DISCOVERY)) {
			private static final long serialVersionUID = 1L;

		});
    	
    	this.addBehaviour(seq);
		
	}
}
