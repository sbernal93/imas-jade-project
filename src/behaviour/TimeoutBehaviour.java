package behaviour;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

/**
 * 
 * 
 *
 */
public class TimeoutBehaviour extends SimpleBehaviour{
	
	private long timeout;
	private long timeToWakeUp;
	private boolean finished = false;

	public TimeoutBehaviour(Agent a, long timeout) {
		super(a);
		this.timeout = timeout;
	}
	
	@Override
	public void onStart() {
		timeToWakeUp = System.currentTimeMillis() + timeout;
	}
	      
	@Override
	public void action() {
		 long dt = timeToWakeUp - System.currentTimeMillis();
	      if (dt <= 0) {
	         finished = true;
	         handleElapsedTimeout();
	      } else 
	         block(dt);
	}
	 protected void handleElapsedTimeout() 
     { } 
           
	@Override
	public boolean done() {
		return finished;
	}

}
