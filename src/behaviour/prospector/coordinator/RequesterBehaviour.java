package behaviour.prospector.coordinator;

import agent.ProspectorCoordinatorAgent;
import behaviour.BaseRequesterBehaviour;
import jade.lang.acl.ACLMessage;

public class RequesterBehaviour extends BaseRequesterBehaviour<ProspectorCoordinatorAgent>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RequesterBehaviour(ProspectorCoordinatorAgent a, ACLMessage msg) {
		super(a, msg);
	}

	

}
