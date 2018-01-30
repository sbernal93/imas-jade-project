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
import behaviour.coordinator.RequestResponseBehaviour;
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
import util.MetalDiscovery;
import util.Movement;

/**
 * The Coordinator agent. This agent is responsible of:
 * - Communicating with the ProspectorCoordinator and the DiggerCoordinator
 * 	 to coordinate actions between types of agents at a global level. 
 * - Share the information given by the SystemAgent with ProspectorCoordinator and DiggerCoordinator.
 * - Gather from ProspectorCoordinator and DiggerCoordinator the list of movements
 *   and pass them to the SystemAgent.
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
    
    private boolean dcInformNewMinesFinished;

    /**
     * Builds the coordinator agent.
     */
    public CoordinatorAgent() {
        super(AgentType.COORDINATOR);
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

	public boolean isDcInformNewMinesFinished() {
		return dcInformNewMinesFinished;
	}

	public void setDcInformNewMinesFinished(boolean dcInformNewMinesFinished) {
		this.dcInformNewMinesFinished = dcInformNewMinesFinished;
	}
    

    /**
     * Agent setup method - called when it first comes on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     * Behaviours setup: 
     * - CreateDiggerCoordinatorBehaviour: for the creation of the digger coordinator
     * - CreateProspectorCoordinatorBehaviour: for the creation of the prospector coordinator
     * - BaseSearchAgentBehaviour: two of these to search for the digger coordinator, and prospector
     *   coordinator respectively
     */
    @Override
    protected void setup() {
    	//It receives the game settings and SystemAgent as Arguments
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
        
        log("Finished setup");
        
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
    }
    
    /**
     * Method called when an INFORM_STEP is received from the SystemAgent, this method 
     * notifies the other coordinators that a new step started
     */
    public void informNewStep() {
    	//movements is going to be the movements made this turn
    	this.movements = new ArrayList<>();
    	//we need both coordinators to tell us the results
    	dcInformStepFinished = false;
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
    	this.addBehaviour(seq);
    }
    
    /**
     * After receiving a notification that the step has finished, it requests the results
     * from the ProspectorCoordinator. If both the prospector coordinator and the digger
     * coordinator have finished informing the step results, it calles the 
     * communicateStepWithSystemAgent to inform the SystemAgent
     */
    public void requestStepResultFromProspectorCoordinator() {
    	this.addBehaviour(new BaseRequesterBehaviour<CoordinatorAgent>(this,
    			UtilsAgents.buildMessage(getProspectorCoordinatorAgent(), MessageContent.STEP_RESULT)) {

			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			protected void handleInform(ACLMessage msg) {
				try {
					this.getTypeAgent().log("INFORM received");
					this.getTypeAgent().addMovements((List<Movement>) msg.getContentObject());
					this.getTypeAgent().setPcInformStepFinished(true);
					this.getTypeAgent().communicateStepWithSystemAgent(MessageContent.STEP_FINISHED);
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
			}
			
		});
    }
    /**
     * After receiving a notification that the step has finished, it requests the results
     * from the DiggerCoordinator. If both the prospector coordinator and the digger
     * coordinator have finished informing the step results, it calles the 
     * communicateStepWithSystemAgent to inform the SystemAgent
     */
    public void requestStepResultFromDiggerCoordinator() {
    	this.addBehaviour(new BaseRequesterBehaviour<CoordinatorAgent>(this,
    			UtilsAgents.buildMessage(getDiggerCoordinatorAgent(), MessageContent.STEP_RESULT)) {
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			protected void handleInform(ACLMessage msg) {
				try {
					this.getTypeAgent().log("INFORM received");
					this.getTypeAgent().addMovements((List<Movement>) msg.getContentObject());
					this.getTypeAgent().setDcInformStepFinished(true);
					this.getTypeAgent().communicateStepWithSystemAgent(MessageContent.STEP_FINISHED);
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
			}
			
		});
    }
    
    /**
     * Communicates to the SystemAgent that the step finished
     * @param content
     */
    public void communicateStepWithSystemAgent(String content) {
    	if(this.isPcInformStepFinished() && this.isDcInformStepFinished()) {
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
						private static final long serialVersionUID = 1L;
	    	});
    	}
    }
    
    /**
     * The step was validated and applied by the system agent, we send the resulting 
     * movements to the coordinators so they can send them to their respective 
     * agents and check if their movement was valid
     * @param movements
     */
    public void informApplyStep(Serializable movements){
    	this.dcApplyStepFinished = false;
    	this.pcApplyStepFinished = false;
    	this.dcInformNewMinesFinished = false;
    	SequentialBehaviour seq = new SequentialBehaviour();
    	seq.addSubBehaviour(new BaseRequesterBehaviour<CoordinatorAgent>(this,
    			buildMessageApplyStep(diggerCoordinatorAgent, movements)) {

					private static final long serialVersionUID = 1L;
					
					@Override
					protected void handleInform(ACLMessage msg) {
						this.getTypeAgent().log("Inform received from DiggerCoordinator for APPLY STEP");
					}
		});
    	seq.addSubBehaviour(new BaseRequesterBehaviour<CoordinatorAgent>(this,
    			buildMessageApplyStep(prospectorCoordinatorAgent, movements)) {

					private static final long serialVersionUID = 1L;
					
					@Override
					protected void handleInform(ACLMessage msg) {
							this.getTypeAgent().log("Inform received from ProspectorCoordinator for APPLY STEP");
							
					}
		});

    	this.addBehaviour(seq);
    }
    
    /**
     * New mines where discovered, so now we send them to the DiggerCoordinator
     * @param cells
     */
    public void informNewMines( List<MetalDiscovery> cells){
    	this.addBehaviour(new BaseRequesterBehaviour<CoordinatorAgent>(this,
    			buildMessageNewMineDiggerCoordinator(this.diggerCoordinatorAgent, cells)) {
					private static final long serialVersionUID = 1L;
					
					@Override
					protected void handleInform(ACLMessage msg) {
						this.getTypeAgent().log("Got inform from DiggerCoordinator for mine discovery");
					}
    		
		});
    	
    }
    
    /**
     * If both coordinators finished applying the steps, and the digger coordinator 
     * received new mine and info and setup everything, we notify the SystemAgent that
     * the step was applied
     */
    public void applyStepFinished(){
    	if(this.dcApplyStepFinished && this.pcApplyStepFinished && this.dcInformNewMinesFinished) {
    		communicateStepWithSystemAgent(MessageContent.APPLY_STEP_FINISHED);
    	}
    }

    /**
     * Builds a message with the new mines discovered to be sent to the digger coordinator
     * @param agent
     * @param cells
     * @return
     */
    private ACLMessage buildMessageNewMineDiggerCoordinator(AID agent, List<MetalDiscovery> cells) {
 		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
 		message.clearAllReceiver();
 		message.addReceiver(agent);
 		message.setProtocol(InteractionProtocol.FIPA_REQUEST);
 		message.setReplyByDate(new Date(System.currentTimeMillis() + 20000));
 		this.log("Request message to a DiggerCoordinator agent with new mines");
         try {
         	message.setContent(MessageContent.MINE_DISCOVERY);
         	this.log("Request message content:" + message.getContent());
         	message.setContentObject((Serializable) cells);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return message;
 	}
    
    /**
     * Builds new Step message for an agent
     * @param agent
     * @return
     */
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
    
    /**
     * Builds the apply step message for an agent
     * @param agent
     * @param movements
     * @return
     */
    private ACLMessage buildMessageApplyStep(AID agent, Serializable movements) {
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.clearAllReceiver();
		message.addReceiver(agent);
		message.setProtocol(InteractionProtocol.FIPA_REQUEST);
		message.setReplyByDate(new Date(System.currentTimeMillis() + 20000));
		this.log("Request message to " + agent.getName());
        try {
        	message.setContent(MessageContent.APPLY_STEP);
        	this.log("Request message content:" + message.getContent());
        	message.setContentObject( movements);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
	}
	
}

