package behaviour.system;

import agent.SystemAgent;
import agent.UtilsAgents;
import behaviour.BaseRequesterBehaviour;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
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
	
	//Waits for a message from the coordinator agent telling him the information of the step
	private static final String WAIT_STEP_FINISHED = "WAIT_STEP";
	
	//Attempts to apply a simulation step
	private static final String VALIDATES_STEP = "VALIDATES_STEP";
	
	private static final String INFORM_APPLY_STEP = "INFORM_APPLY_STEP";
	
	//Attempts to apply a simulation step
	private static final String APPLY_STEP_STATE = "APPLY_STEP";
	
	private static final String WAIT_APPLY_STEP_FINISHED = "WAIT_APPLY_STEP_FINISHED";
	
	//All steps are finished
	private static final String FINISHED_STATE = "FINISHED";
	
	public SimulationBehaviour(SystemAgent agent) {
		this.agent = agent;
		setupFSM();
	}
	
	private void setupFSM(){
		MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

		
		registerFirstState(new SetupBehaviour(this.agent), SETUP_STATE);
		registerState(new SimStepRequesterCoordinatorAgentBehaviour(this.agent), SIM_STEP_STATE);
		registerState(new WaitStepEndBehaviour(this.agent, mt), WAIT_STEP_FINISHED);
		registerState(new ValidateSimulationStepBehaviour(this.agent), VALIDATES_STEP);
		registerState(new ApplySimulationStepBehaviour(this.agent), APPLY_STEP_STATE);
		registerState(new ApplyStepRequesterCoordinatorAgentBehaviour(this.agent), INFORM_APPLY_STEP);
		registerState(new WaitApplyStepEndBehaviour(this.agent, mt), WAIT_APPLY_STEP_FINISHED);
		registerLastState(new EndSimulationBehaviour(this.agent), FINISHED_STATE);
		
		registerDefaultTransition(SETUP_STATE, SIM_STEP_STATE);
		registerDefaultTransition(SIM_STEP_STATE, WAIT_STEP_FINISHED);
		registerDefaultTransition(WAIT_STEP_FINISHED, VALIDATES_STEP);
		registerDefaultTransition(VALIDATES_STEP, APPLY_STEP_STATE);
		registerDefaultTransition(APPLY_STEP_STATE, INFORM_APPLY_STEP);
		registerDefaultTransition(INFORM_APPLY_STEP, WAIT_APPLY_STEP_FINISHED);
		registerTransition(WAIT_APPLY_STEP_FINISHED, SIM_STEP_STATE, WaitApplyStepEndBehaviour.CONTINUE_SIMULATION);
		registerTransition(WAIT_APPLY_STEP_FINISHED, FINISHED_STATE, WaitApplyStepEndBehaviour.FINISHED_SIMULATION);
		
	}
}
