package behaviour.system;

import java.util.List;

import agent.CoordinatorAgent;
import agent.SystemAgent;
import agent.UtilsAgents;
import behaviour.BaseRequesterBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import onthology.MessageContent;
import util.Movement;

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
			Object content = (Object) message.getContent();
			if(content.equals(MessageContent.STEP_FINISHED)) {
				try {
					agent.setMovementsProposed((List<Movement>) message.getContentObject());
					agent.log("Got movements :" + agent.getMovementsProposed().size());
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
				finished = true;
			}
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
