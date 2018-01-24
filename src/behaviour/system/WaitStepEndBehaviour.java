package behaviour.system;

import agent.SystemAgent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class WaitStepEndBehaviour extends SimpleBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean finished;
	private SystemAgent agent;
	private MessageTemplate mt;
	
	public WaitStepEndBehaviour(SystemAgent agent, MessageTemplate mt) {
		super(agent);
		this.agent = agent;
		this.mt = mt;
	}

	@Override
	public void action() {
		ACLMessage message = agent.receive(mt);
		
		if(message != null){
			//TODO: extract info from message, maybe also validate its the message we are waiting for
			finished = true;
		} else {
			block(5000);
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

}
