package agent;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

import behaviour.BaseRequesterBehaviour;
import behaviour.prospector.coordinator.CreateProspectorAgentBehaviour;
import behaviour.prospector.coordinator.ProspectorContractNetInitiatorBehaviour;
import behaviour.prospector.coordinator.RequestResponseBehaviour;
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
import map.PathCell;
import onthology.GameSettings;
import onthology.MessageContent;
import util.MetalDiscovery;
import util.Movement;
import util.Plan;

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

	private static final long serialVersionUID = 1L;
	private GameSettings game;
	private AID coordinatorAgent;
	private boolean firstStep;
	private List<Movement> movements;
	private List<MetalDiscovery> newMines;
	private List<MetalDiscovery> discoveredMines;
	private List<AID> prospectorAgents;

	public ProspectorCoordinatorAgent() {
		super(AgentType.PROSPECTOR_COORDINATOR);
		this.firstStep = true;
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

	public List<MetalDiscovery> getNewMines() {
		return newMines;
	}

	public void setNewMines(List<MetalDiscovery> newMines) {
		this.newMines = newMines;
	}

	public List<MetalDiscovery> getDiscoveredMines() {
		return discoveredMines;
	}

	public void setDiscoveredMines(List<MetalDiscovery> discoveredMines) {
		this.discoveredMines = discoveredMines;
	}
    
	public void addNewMines(List<MetalDiscovery> mines) {
		if (this.newMines == null) {
			this.newMines = new ArrayList<>();
		}
		this.newMines.addAll(mines);
	}
	
	public void addNewMine(MetalDiscovery mine) {
		if (this.newMines == null) {
			this.newMines = new ArrayList<>();
		}
		this.newMines.add(mine);
	}
	
	public void addDiscoveredMines(List<MetalDiscovery> mines) {
		if (this.discoveredMines == null) {
			this.discoveredMines = new ArrayList<>();
		}
		this.discoveredMines.addAll(mines);
	}

	public void addDiscoveredMine(MetalDiscovery mine) {
		if (this.discoveredMines == null) {
			this.discoveredMines = new ArrayList<>();
		}
		this.discoveredMines.add(mine);
	}
	   /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {

    	this.setGame((GameSettings) this.getArguments()[0]);
    	this.setCoordinatorAgent((AID) this.getArguments()[1]);
    	this.discoveredMines = new ArrayList<>();
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

        this.addBehaviour(new CreateProspectorAgentBehaviour(this, AgentType.PROSPECTOR));
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        this.addBehaviour(new RequestResponseBehaviour(this, mt));
    }
    
    /**
     * Checks if any of the mines we have discovered are depleted, if so, removes them
     */
    private void validateDepletedMines() {
    	for(MetalDiscovery m : this.discoveredMines) {
    		if(m.getCell().isEmpty()) {
    			this.discoveredMines.remove(m);
    		}
    	}
    }
    
    /**
     * Setups the behaviour to start contract net and/or inform prospector agents of new step.
     * Cant return inform inmediatly since the behaviours created wont execute until the 
     * response is finished
     * @param seq
     */
    public void informNewStep(SequentialBehaviour seq) {
    	this.newMines = new ArrayList<>();
    	validateDepletedMines();
    	if(!firstStep) {
    		if(seq == null) {
    			seq = new SequentialBehaviour();
    		}
	    	this.movements = new ArrayList<>();
	    	for (AID agent : this.prospectorAgents) {
	    		seq.addSubBehaviour(new BaseRequesterBehaviour<ProspectorCoordinatorAgent>(this,
	    				UtilsAgents.buildMessage(agent, MessageContent.NEW_STEP)) {
							private static final long serialVersionUID = 1L;
							@Override
							protected void handleInform(ACLMessage msg) {
								this.getTypeAgent().log("Inform received from: " + msg.getSender().getName());
								try {
									this.getTypeAgent().addMovement((Movement) msg.getContentObject());
								} catch (UnreadableException e) {
									e.printStackTrace();
								}
							}
	    			
	    		});
	    	}
	    	seq.addSubBehaviour(new BaseRequesterBehaviour<ProspectorCoordinatorAgent>(this,
	    			UtilsAgents.buildMessage(this.coordinatorAgent, MessageContent.STEP_FINISHED)) {

				private static final long serialVersionUID = 1L;
			});
	    	
	    	this.addBehaviour(seq);
    	} else {
    		try {
    			this.log("Attempting contract net");
    			seq  = performContractNet();
				this.firstStep = false;
				//does contract net, then informs the agent they can start
				informNewStep(seq);
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
    public SequentialBehaviour performContractNet() throws IOException{
    	SequentialBehaviour seq = new SequentialBehaviour();
        int nResponders = this.getProspectorAgents().size();
    	List<PathCell> pathCellsToExplore = game.getPathCellsNextToFieldCells(true);
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
            ArrayList<PathCell> list = new ArrayList<PathCell>(group);
            msg.setContentObject((Serializable) list);
            seq.addSubBehaviour(new ProspectorContractNetInitiatorBehaviour(this, msg, nResponders));
    	}
    	return seq;
    }

    /**
     * Informs the prospector agents of the movement status that was attempted during this
     * simulation step. And checks back if the agent received a new mine after applying it.
     * Then, sends a message to the coordinator with the new mines (if any) and that the
     * apply step stage is finished
     * @param movements
     */
    public void informApplyStep(List<Movement> movements){
    	SequentialBehaviour seq = new SequentialBehaviour();
    	//prospectors always have movements, so we should respond to all
    	for (AID agent : this.prospectorAgents) {
    		Movement movement = movements.stream().filter(m -> m.getAgent().equals(agent)).findFirst().get();
    		seq.addSubBehaviour(new BaseRequesterBehaviour<ProspectorCoordinatorAgent>(this,
    				UtilsAgents.buildMessageWithObj(agent,MessageContent.APPLY_STEP, movement)) {
						private static final long serialVersionUID = 1L;

						@SuppressWarnings("unchecked")
						@Override
						protected void handleInform(ACLMessage msg) {
							this.getTypeAgent().log("Got inform for apply step");
							try {
								List<MetalDiscovery> newMines = (List<MetalDiscovery>) Optional.ofNullable(msg.getContentObject()).orElse(null);
								if(newMines!=null) {
									newMines.stream().forEach(mine -> {
										if(this.getTypeAgent().getDiscoveredMines().stream().noneMatch(m -> m.equals(mine))) {
											this.getTypeAgent().addDiscoveredMine(mine);
											this.getTypeAgent().addNewMine(mine);
										}
									});	
								}
							} catch (UnreadableException e) {
								e.printStackTrace();
							}
						}			
    		});
    	}
    	seq.addSubBehaviour(new BaseRequesterBehaviour<ProspectorCoordinatorAgent>(this, 
    			buildNewMinesMessage()) {
					private static final long serialVersionUID = 1L;
					
					@Override
					protected Vector<ACLMessage> prepareRequests(ACLMessage request) {
						Vector<ACLMessage> v = new Vector<>();
						v.add(buildNewMinesMessage());
						return v;
					}
		});
    	
    	seq.addSubBehaviour(new BaseRequesterBehaviour<ProspectorCoordinatorAgent>(this,
    			UtilsAgents.buildMessage(this.coordinatorAgent, MessageContent.APPLY_STEP_FINISHED)) {
			private static final long serialVersionUID = 1L;
		});
    	
    	this.addBehaviour(seq);
    }

    private ACLMessage buildNewMinesMessage() {
    	ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.clearAllReceiver();
		message.addReceiver(this.coordinatorAgent);
		message.setProtocol(InteractionProtocol.FIPA_REQUEST);
		message.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
		this.log("Request message to a coordinator agent");
        try {
        	message.setContent(MessageContent.MINE_DISCOVERY);
        	this.log("Request message content:" + message.getContent());
        	if(this.getNewMines() != null && this.getNewMines().size()>0) {
        		this.log("We got new mines");
        		message.setContentObject((Serializable) this.getNewMines());
        	} else {
        		this.log("No new mines");
        	}
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }
    

}
