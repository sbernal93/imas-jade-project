package behaviour.coordinator;

import java.util.List;

import agent.CoordinatorAgent;
import agent.ProspectorCoordinatorAgent;
import behaviour.BaseRequesterBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import util.Movement;

public class ProspectorCoordinatorRequesterBehaviour extends BaseRequesterBehaviour<CoordinatorAgent>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private CoordinatorAgent agent;

	public ProspectorCoordinatorRequesterBehaviour(CoordinatorAgent a, ACLMessage msg) {
		super(a, msg);
		this.agent = a;
	}
	
	@Override
	protected void handleInform(ACLMessage msg) {
		agent.log("Inform received from ProspectorCoordinator");
	}

}
