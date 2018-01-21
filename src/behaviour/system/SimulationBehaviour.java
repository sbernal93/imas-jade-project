package behaviour.system;

import agent.AgentType;
import agent.SystemAgent;
import behaviour.BaseRequesterBehaviour;
import behaviour.BaseSearchAgentBehaviour;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import onthology.MessageContent;

/**
 * The Simulation behaviour is a FSM behaviour with the following states:
 * -SETUP: is the initial state
 * -SIM_STEP: a simulation step, notifies the coordinator agent that a new step should be started 
 * (or if the previous step attempt failed) and waits for the coordinator agents response
 * -APPLY_STEP: validates and applies the actions received from the CoordinatorAgent. Determines
 * if all the simulation steps have been executed
 * -FINISHED: the simulation finished
 * 
 * The behaviour should execute the SIM_STEP, WAITING and APPLY_STEP behaviours until the amount
 * of steps reaches the specified maximum, when it does, the FINISHED state is reached 
 */
public class SimulationBehaviour extends FSMBehaviour{
	
	private SystemAgent agent;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//First step is to setup the agents and environment
	private static final String SETUP_STATE = "SETUP";
	
	//Notifies the coordinator agent to init step, waits for response
	private static final String SIM_STEP_STATE = "SIM_STEP";
	
	//Attempts to apply a simulation step
	private static final String APPLY_STEP_STATE = "APPLY_STEP";
	
	//All steps are finished
	private static final String FINISHED_STATE = "FINISHED";
	
	public SimulationBehaviour(SystemAgent agent) {
		this.agent = agent;
		setupFSM();
	}
	
	private void setupFSM(){
		BaseRequesterBehaviour<SystemAgent> simStep = new BaseRequesterBehaviour<SystemAgent>(
				this.agent, buildMessageForCoordinatorAgent()) {
			private static final long serialVersionUID = 1L;
			//TODO: implement handle of message type. INFORM messages should be handled 
			//receiving the new actions that where attempted during the simulation steps
			//this actions should be stored in the systemAgent in order to be validated
			//in the other FSM state
		};

		registerFirstState(new SetupBehaviour(this.agent), SETUP_STATE);
		registerState(simStep, SIM_STEP_STATE);
		registerState(new ApplySimulationStepBehaviour(this.agent), APPLY_STEP_STATE);
		registerLastState(new EndSimulationBehaviour(this.agent), FINISHED_STATE);
		
		registerDefaultTransition(SETUP_STATE, SIM_STEP_STATE);
		registerDefaultTransition(SIM_STEP_STATE, APPLY_STEP_STATE);
		registerTransition(APPLY_STEP_STATE, SIM_STEP_STATE, ApplySimulationStepBehaviour.CONTINUE_SIMULATION);
		registerTransition(APPLY_STEP_STATE, FINISHED_STATE, ApplySimulationStepBehaviour.FINISHED_SIMULATION);
		
	}
	
	private ACLMessage buildMessageForCoordinatorAgent() {
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.clearAllReceiver();
		message.addReceiver(this.agent.getCoordinatorAgent());
		message.setProtocol(InteractionProtocol.FIPA_REQUEST);
        this.agent.log("Request message to Coordinator agent");
        try {
        	message.setContent(MessageContent.NEW_STEP);
            this.agent.log("Request message content:" + message.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
	}
}
