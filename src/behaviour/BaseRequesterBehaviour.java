package behaviour;

import agent.ImasAgent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Base class for a Requester Behaviour
 *
 */
public abstract class BaseRequesterBehaviour<T extends ImasAgent> extends AchieveREInitiator{
	
	private T agent;

	public BaseRequesterBehaviour(T a, ACLMessage msg) {
		super(a, msg);
		agent = a;
	}


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

    /**
     * Handle AGREE messages
     *
     * @param msg Message to handle
     */
    @Override
    protected void handleAgree(ACLMessage msg) {
        T agent = (T) this.getAgent();
        agent.log("AGREE received from " + ((AID) msg.getSender()).getLocalName());
    }

    /**
     * Handle INFORM messages
     *
     * @param msg Message
     */
    @Override
    protected void handleInform(ACLMessage msg) {
    	T agent = (T) this.getAgent();
        agent.log("INFORM received from " + ((AID) msg.getSender()).getLocalName());
    }
    

    /**
     * Handle NOT-UNDERSTOOD messages
     *
     * @param msg Message
     */
    @Override
    protected void handleNotUnderstood(ACLMessage msg) {
    	T agent = (T) this.getAgent();
        agent.log("This message NOT UNDERSTOOD.");
    }

    /**
     * Handle FAILURE messages
     *
     * @param msg Message
     */
    @Override
    protected void handleFailure(ACLMessage msg) {
    	T agent = (T) this.getAgent();
        agent.log("The action has failed.");

    } //End of handleFailure

    /**
     * Handle REFUSE messages
     *
     * @param msg Message
     */
    @Override
    protected void handleRefuse(ACLMessage msg) {
    	T agent = (T) this.getAgent();
        agent.log("Action refused.");
    }
    
    public T getTypeAgent() {
    	return agent;
    }
}
