package util;

import java.io.Serializable;

import agent.ImasMobileAgent;
import map.Cell;
import onthology.InfoAgent;

/**
 * Class for handling the movement of agents
 * @author santiagobernal
 *
 */
public class Movement implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public enum MovementStatus {PROPOSAL, ACCEPTED, REJECTED};
	
	private ImasMobileAgent agent;
	private Cell oldCell;
	private Cell newCell;
	private MovementStatus status;
	
	public Movement(){
	}
	
	public Movement(ImasMobileAgent agent, Cell oldCell, Cell newCell) {
		super();
		this.agent = agent;
		this.oldCell = oldCell;
		this.newCell = newCell;
		this.status = MovementStatus.PROPOSAL;
	}

	public ImasMobileAgent getAgent() {
		return agent;
	}
	public void setAgent(ImasMobileAgent agent) {
		this.agent = agent;
	}
	public Cell getOldCell() {
		return oldCell;
	}
	public void setOldCell(Cell oldCell) {
		this.oldCell = oldCell;
	}
	public Cell getNewCell() {
		return newCell;
	}
	public void setNewCell(Cell newCell) {
		this.newCell = newCell;
	}

	public MovementStatus getStatus() {
		return status;
	}

	public void setStatus(MovementStatus status) {
		this.status = status;
	}
	
	public InfoAgent getInfoAgent() {
		return new InfoAgent(this.getAgent().getType(), this.getAgent().getAID());
	}

	@Override
	public String toString() {
		return "Movement [agent=" + agent.getName() + ", oldCell=(" + oldCell.getRow() + ", " + oldCell.getCol()+ "),"
				+ " newCell=(" + newCell.getRow() + ", " + newCell.getCol() + "), status=" + status
				+ "]";
	}
	
	

}
