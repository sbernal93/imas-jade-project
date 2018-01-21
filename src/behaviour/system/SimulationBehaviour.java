package behaviour.system;

import agent.SystemAgent;
import jade.core.behaviours.FSMBehaviour;

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

		registerFirstState(new SetupBehaviour(this.agent), SETUP_STATE);
		registerState(new SimStepRequesterCoordinatorAgentBehaviour(this.agent), SIM_STEP_STATE);
		registerState(new ApplySimulationStepBehaviour(this.agent), APPLY_STEP_STATE);
		registerLastState(new EndSimulationBehaviour(this.agent), FINISHED_STATE);
		
		registerDefaultTransition(SETUP_STATE, SIM_STEP_STATE);
		registerDefaultTransition(SIM_STEP_STATE, APPLY_STEP_STATE);
		registerTransition(APPLY_STEP_STATE, SIM_STEP_STATE, ApplySimulationStepBehaviour.CONTINUE_SIMULATION);
		registerTransition(APPLY_STEP_STATE, FINISHED_STATE, ApplySimulationStepBehaviour.FINISHED_SIMULATION);
		
	}
}
