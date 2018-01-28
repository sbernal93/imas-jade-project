package behaviour.system;

import java.util.ArrayList;
import java.util.List;

import agent.CoordinatorAgent;
import agent.SystemAgent;
import agent.UtilsAgents;
import behaviour.BaseRequesterBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import onthology.InitialGameSettings;
import onthology.MessageContent;
import util.Movement;

public class WaitApplyStepEndBehaviour extends SimpleBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int FINISHED_SIMULATION = 1;
	public static final int CONTINUE_SIMULATION = 0;
	
	
	private boolean finished;
	private SystemAgent agent;
	private MessageTemplate mt;
	
	public WaitApplyStepEndBehaviour(SystemAgent agent, MessageTemplate mt) {
		super(agent);
		this.agent = agent;
		this.mt = mt;
	}

	@Override
	public void action() {
		this.agent.log("WaitApplyStepEndBehaviour");
		ACLMessage message = agent.receive(mt);
		if(message != null){
			this.agent.log("Got message");
			//TODO: extract info from message, maybe also validate its the message we are waiting for
			//if(message.getContent().equals(MessageContent.APPLY_STEP_FINISHED)) {
				finished = true;
			//}
		} else {
			block(1000);
		}
		
	}
	
	@Override
	public void reset() {
		finished = false;
		super.reset();
	}

	@Override
	public boolean done() {
		return finished;
	}
	
	@Override
	public int onEnd() {
		reset();
		this.agent.log("WaitApplyStepEndBehaviour finished, current Step: " + this.agent.getCurrentStep() );
		
		long time = System.currentTimeMillis() - this.agent.getStepStartTime();
		this.agent.setTotalTime(this.agent.getTotalTime() + time);
		this.agent.getGui().showStatistics("Current step: " + this.agent.getCurrentStep() + ", of " 
			+ this.agent.getGame().getSimulationSteps() + ", step time: " + time 
			+ "ms, total time: " + this.agent.getTotalTime() + "ms \n");
		
		if(this.agent.getGame().getSimulationSteps() <= this.agent.getCurrentStep()) {
			this.agent.log("Simulation finished");
			return FINISHED_SIMULATION;
		}
		this.agent.log("Simulation continues");
		this.agent.setCurrentStep(this.agent.getCurrentStep() + 1);
		((InitialGameSettings) this.agent.getGame()).addElementsForThisSimulationStep();
		this.agent.updateGUI();
		this.agent.setMovementsProposed(new ArrayList<>());
		return CONTINUE_SIMULATION;
	}
	
	 public void onStart() {
	    reset();
	  }

}
