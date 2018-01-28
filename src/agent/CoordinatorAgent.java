/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package agent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import behaviour.BaseRequesterBehaviour;
import behaviour.BaseSearchAgentBehaviour;
import behaviour.coordinator.CreateDiggerCoordinatorBehaviour;
import behaviour.coordinator.CreateProspectorCoordinatorBehaviour;
import behaviour.coordinator.ProspectorCoordinatorRequesterBehaviour;
import behaviour.coordinator.RequestResponseBehaviour;
import behaviour.coordinator.WaitProspectorCoordinatorResponseBehaviour;
import behaviour.prospector.coordinator.ProspectorNewStepRequesterBehaviour;
import jade.core.AID;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames.InteractionProtocol;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import onthology.GameSettings;
import onthology.MessageContent;
import util.Movement;

/**
 * The main Coordinator agent. 
 * TODO: This coordinator agent should get the game settings from the System
 * agent every round and share the necessary information to other coordinators.
 */
public class CoordinatorAgent extends ImasAgent {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
     * Game settings in use.
     */
    private GameSettings game;
    /**
     * System agent id.
     */
    private AID systemAgent;
    
    /**
     * DiggerCoordinatorAgent id
     */
	private AID diggerCoordinatorAgent;
    
    /**
     * ProspectorCoordinatorAgent id
     */
    private AID prospectorCoordinatorAgent;
    
    private List<Movement> movements;
    
    private boolean dcInformStepFinished;
    
    private boolean pcInformStepFinished;
    
    private boolean dcApplyStepFinished;
    
    private boolean pcApplyStepFinished;

    /**
     * Builds the coordinator agent.
     */
    public CoordinatorAgent() {
        super(AgentType.COORDINATOR);
    }

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {
    	this.setGame((GameSettings) this.getArguments()[0]);
    	this.setSystemAgent((AID) this.getArguments()[1]);
    	
    	this.addBehaviour(new CreateDiggerCoordinatorBehaviour(this, AgentType.DIGGER_COORDINATOR));
        this.addBehaviour(new CreateProspectorCoordinatorBehaviour(this, AgentType.PROSPECTOR_COORDINATOR));
        
        this.addBehaviour(new BaseSearchAgentBehaviour(this, AgentType.DIGGER_COORDINATOR) {

			private static final long serialVersionUID = 1L;

			@Override
			public void setAgent(AID agent) {
				((CoordinatorAgent) this.getAgent()).setDiggerCoordinatorAgent(agent);
			}
		});
        this.addBehaviour(new BaseSearchAgentBehaviour(this, AgentType.PROSPECTOR_COORDINATOR) {

			private static final long serialVersionUID = 1L;

			@Override
			public void setAgent(AID agent) {
				((CoordinatorAgent) this.getAgent()).setProspectorCoordinatorAgent(agent);
			}
		});
        

        // search SystemAgent
        //commented out, agent is passed as parameter
        /*
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.SYSTEM.toString());
        this.systemAgent = UtilsAgents.searchAgent(this, searchCriterion);*/
        
        log("Finished setup");
        
        //TODO: this 
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        this.addBehaviour(new RequestResponseBehaviour(this, mt));

        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.COORDINATOR.toString());
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

        // searchAgent is a blocking method, so we will obtain always a correct AID

        /* ********************************************************************/
        /*ACLMessage initialRequest = new ACLMessage(ACLMessage.REQUEST);
        initialRequest.clearAllReceiver();
        initialRequest.addReceiver(this.systemAgent);
        initialRequest.setProtocol(InteractionProtocol.FIPA_REQUEST);
        log("Request message to agent");
        try {
            initialRequest.setContent(MessageContent.GET_MAP);
            log("Request message content:" + initialRequest.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //we add a behaviour that sends the message and waits for an answer
        this.addBehaviour(new RequesterBehaviour(this, initialRequest));
        
       
        // add behaviours
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        this.addBehaviour(new RequestResponseBehaviour(this, mt));

*/
        // setup finished. When we receive the last inform, the agent itself will add
        // a behaviour to send/receive actions. we create the other coordinator agents
        
    }
    
    public void informNewStep() {
    	this.movements = new ArrayList<>();
    	//dcInformStepFinished = false;
    	dcInformStepFinished = true;
    	pcInformStepFinished = false;
    	//Sends messages to both coordinator agents to start a new step and waits for proposed movements,
    	//once an INFORM message is received, then gets the movements from the message content
    	SequentialBehaviour seq = new SequentialBehaviour();
    	seq.addSubBehaviour(new BaseRequesterBehaviour<CoordinatorAgent>(this,
    			buildMessageForCoordinatorsAgent(getDiggerCoordinatorAgent())) {

					private static final long serialVersionUID = 1L;
					
					@Override
					protected void handleInform(ACLMessage msg) {
							((CoordinatorAgent) this.getAgent()).log("Inform received from DiggerCoordinator");
							//((CoordinatorAgent) this.getAgent()).addMovements((List<Movement>) msg.getContentObject());

						super.handleInform(msg);
					}
		});
    	seq.addSubBehaviour(new BaseRequesterBehaviour<CoordinatorAgent>(this,
    			buildMessageForCoordinatorsAgent(getProspectorCoordinatorAgent())) {

					private static final long serialVersionUID = 1L;
					
					@Override
					protected void handleInform(ACLMessage msg) {
							this.getTypeAgent().log("Inform received from ProspectorCoordinator");
					}
		});
    	//seq.addSubBehaviour(new ProspectorCoordinatorRequesterBehaviour(this, buildMessageForCoordinatorsAgent(getProspectorCoordinatorAgent())));

    	this.addBehaviour(seq);
    }
    
    public void requestStepResultFromProspectorCoordinator() {
    	this.addBehaviour(new BaseRequesterBehaviour<CoordinatorAgent>(this,
    			UtilsAgents.buildMessage(getProspectorCoordinatorAgent(), MessageContent.STEP_RESULT)) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			protected void handleInform(ACLMessage msg) {
				try {
					this.getTypeAgent().log("INFORM received");
					this.getTypeAgent().addMovements((List<Movement>) msg.getContentObject());
					this.getTypeAgent().setPcInformStepFinished(true);
					if(this.getTypeAgent().isPcInformStepFinished() && this.getTypeAgent().isDcInformStepFinished()) {
						this.getTypeAgent().communicateStepWithSystemAgent(MessageContent.STEP_FINISHED);
					}
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
			}
			
		});
    	//TODO: make one of this methods for digger coordinator, the last one to execute should trigger the 
    	//message to be sent to the system agent
    }
    
    public void communicateStepWithSystemAgent(String content) {
    	ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.clearAllReceiver();
		message.addReceiver(getSystemAgent());
		message.setProtocol(InteractionProtocol.FIPA_REQUEST);
		message.setReplyByDate(new Date(System.currentTimeMillis() + 20000));
		this.log("Request message to System agent");
        try {
        	message.setContent(content);
        	this.log("Request message content:" + message.getContent());
        	message.setContentObject((Serializable) this.getMovements());
        } catch (Exception e) {
            e.printStackTrace();
        }
    	this.addBehaviour(new BaseRequesterBehaviour<CoordinatorAgent>(this, message) {

					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;
    		
    	});
    }
    
    private ACLMessage buildMessageForCoordinatorsAgent(AID agent) {
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.clearAllReceiver();
		message.addReceiver(agent);
		message.setProtocol(InteractionProtocol.FIPA_REQUEST);
		message.setReplyByDate(new Date(System.currentTimeMillis() + 20000));
		this.log("Request message to a Coordinator agent");
        try {
        	message.setContent(MessageContent.NEW_STEP);
        	this.log("Request message content:" + message.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
	}
    
    public void informApplyStep(List<Movement> movements){
    	this.dcApplyStepFinished = true;
    	//this.dcApplyStepFinished = false;
    	this.pcApplyStepFinished = false;
    	SequentialBehaviour seq = new SequentialBehaviour();
    	//TODO:
    	/*seq.addSubBehaviour(new BaseRequesterBehaviour<CoordinatorAgent>(this,
    			UtilsAgents.buildMessage(this.getDiggerCoordinatorAgent(), MessageContent.APPLY_STEP)) {

					private static final long serialVersionUID = 1L;
					
					@Override
					protected void handleInform(ACLMessage msg) {
							((CoordinatorAgent) this.getAgent()).log("Inform received from DiggerCoordinator for APPLY STEP");
					}
		});*/
    	seq.addSubBehaviour(new BaseRequesterBehaviour<CoordinatorAgent>(this,
    			buildMessageApplyStepForProspectorCoordinator(prospectorCoordinatorAgent, movements)) {

					private static final long serialVersionUID = 1L;
					
					@Override
					protected void handleInform(ACLMessage msg) {
							this.getTypeAgent().log("Inform received from ProspectorCoordinator for APPLY STEP");
							
					}
		});

    	this.addBehaviour(seq);
    }
    
    private ACLMessage buildMessageApplyStepForProspectorCoordinator(AID agent, List<Movement> movements) {
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.clearAllReceiver();
		message.addReceiver(agent);
		message.setProtocol(InteractionProtocol.FIPA_REQUEST);
		message.setReplyByDate(new Date(System.currentTimeMillis() + 20000));
		this.log("Request message to a ProspectorCoordinator agent");
        try {
        	message.setContent(MessageContent.APPLY_STEP);
        	this.log("Request message content:" + message.getContent());
        	message.setContentObject((Serializable) movements);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
	}
    
    
    public void informNewMines(){
    	//TODO informs the digger coordinator of new mines, 
    }

    public void applyStepFinished(){
    	if(this.dcApplyStepFinished && this.pcApplyStepFinished) {
    		communicateStepWithSystemAgent(MessageContent.APPLY_STEP_FINISHED);
    	}
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

    public void setDiggerCoordinatorAgent(AID diggerCoordinatorAgent) {
    	this.diggerCoordinatorAgent = diggerCoordinatorAgent;
    }
    
    public void setProspectorCoordinatorAgent(AID prospectorCoordinatorAgent) {
    	this.prospectorCoordinatorAgent = prospectorCoordinatorAgent;
    }
    
    public void setSystemAgent(AID systemAgent) {
    	this.systemAgent = systemAgent;
    }

	public AID getSystemAgent() {
		return systemAgent;
	}

	public AID getDiggerCoordinatorAgent() {
		return diggerCoordinatorAgent;
	}

	public AID getProspectorCoordinatorAgent() {
		return prospectorCoordinatorAgent;
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

	public boolean isDcInformStepFinished() {
		return dcInformStepFinished;
	}

	public void setDcInformStepFinished(boolean dcInformStepFinished) {
		this.dcInformStepFinished = dcInformStepFinished;
	}

	public boolean isPcInformStepFinished() {
		return pcInformStepFinished;
	}

	public void setPcInformStepFinished(boolean pcInformStepFinished) {
		this.pcInformStepFinished = pcInformStepFinished;
	}

	public boolean isDcApplyStepFinished() {
		return dcApplyStepFinished;
	}

	public void setDcApplyStepFinished(boolean dcApplyStepFinished) {
		this.dcApplyStepFinished = dcApplyStepFinished;
	}

	public boolean isPcApplyStepFinished() {
		return pcApplyStepFinished;
	}

	public void setPcApplyStepFinished(boolean pcApplyStepFinished) {
		this.pcApplyStepFinished = pcApplyStepFinished;
	}
    
}

