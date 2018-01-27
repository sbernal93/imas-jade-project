package behaviour.prospector.coordinator;

import agent.ProspectorCoordinatorAgent;
import behaviour.BaseRequesterBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import util.Movement;

public class ProspectorNewStepRequesterBehaviour extends BaseRequesterBehaviour<ProspectorCoordinatorAgent>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ProspectorCoordinatorAgent agent;
	
	public ProspectorNewStepRequesterBehaviour(ProspectorCoordinatorAgent a, ACLMessage msg) {
		super(a, msg);
		this.agent = a;
	}

	@Override
	protected void handleInform(ACLMessage msg) {
		agent.log("Inform received from: " + msg.getSender().getName());
		try {
			agent.addMovement((Movement) msg.getContentObject());
		} catch (UnreadableException e) {
			e.printStackTrace();
		}
	}
}
