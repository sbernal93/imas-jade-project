package behaviour.digger;

import agent.DiggerAgent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * The requester behaviour for the Digger Agent. 
 * The Digger agent must request what actions to execute (dig a mine or go to 
 * a manufacturing center) if it is empty
 * @author santiagobernal
 *
 */
public class RequesterBehaviour extends AchieveREInitiator{

	public RequesterBehaviour(DiggerAgent agent, ACLMessage msg) {
		super(agent, msg);
        agent.log("Started behaviour to deal with AGREEs");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
