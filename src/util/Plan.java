package util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import agent.ImasMobileAgent;

/**
 * A plan for mobile agents
 * @author santiagobernal
 *
 */
public class Plan implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ImasMobileAgent agent;
	private List<Movement> movements;

	public Plan(ImasMobileAgent agent, List<Movement> movements) {
		super();
		this.agent = agent;
		this.movements = movements;
	}

	public List<Movement> getMovements() {
		return movements;
	}

	public void setMovements(List<Movement> movements) {
		this.movements = movements;
	}
	
	public void addMovement(Movement movement) {
		if(this.movements == null) {
			this.movements = new ArrayList<>();
		}
		this.movements.add(movement);
	}
	
	public void deleteMovement(Movement movement) {
		if(this.movements != null) {
			this.movements.remove(movement);
		}
	}

	public ImasMobileAgent getAgent() {
		return agent;
	}

	public void setAgent(ImasMobileAgent agent) {
		this.agent = agent;
	}
	
}
