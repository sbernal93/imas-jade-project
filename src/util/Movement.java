package util;

import java.io.Serializable;

import agent.ImasMobileAgent;
import map.Cell;
import map.FieldCell;
import onthology.InfoAgent;
import onthology.MetalType;

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
	
	private ImasMobileAgent agent;
	private Cell oldCell;
	private Cell newCell;
	private MovementStatus status;
	private MovementType type;
	private Cell mcCell;
	private MetalDiscovery metal;
	
	public Movement(){
	}
	
	public Movement(ImasMobileAgent agent, Cell oldCell, Cell newCell) {
		super();
		this.agent = agent;
		this.oldCell = oldCell;
		this.newCell = newCell;
		this.status = MovementStatus.PROPOSAL;
		this.type = MovementType.NORMAL;
	}
	
	public Movement(ImasMobileAgent agent, Cell oldCell, Cell newCell, MovementType type) {
		super();
		this.agent = agent;
		this.oldCell = oldCell;
		this.newCell = newCell;
		this.type = type;
		this.status = MovementStatus.PROPOSAL;
	}

	
	
	public Movement(ImasMobileAgent agent, Cell oldCell, Cell newCell, MovementType type,
			MetalDiscovery metal) {
		super();
		this.agent = agent;
		this.oldCell = oldCell;
		this.newCell = newCell;
		this.status = MovementStatus.PROPOSAL;
		this.type = type;
		this.metal= metal;
	}
	
	public Movement(ImasMobileAgent agent, Cell oldCell, Cell newCell, Cell mcCell, MovementType type) {
		super();
		this.agent = agent;
		this.oldCell = oldCell;
		this.newCell = newCell;
		this.status = MovementStatus.PROPOSAL;
		this.type = type;
		this.mcCell = mcCell;
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
	
	public MovementType getType() {
		return type;
	}

	public void setType(MovementType type) {
		this.type = type;
	}

	public InfoAgent getInfoAgent() {
		return new InfoAgent(this.getAgent().getType(), this.getAgent().getAID());
	}

	@Override
	public String toString() {
		return "Movement [agent=" + agent + ", oldCell=" + oldCell + ", newCell=" + newCell + ", status=" + status
				+ ", type=" + type + "]";
	}


	public Cell getMcCell() {
		return mcCell;
	}

	public void setMcCell(Cell mcCell) {
		this.mcCell = mcCell;
	}

	public MetalDiscovery getMetal() {
		return metal;
	}

	public void setMetal(MetalDiscovery metal) {
		this.metal = metal;
	}
	
	

}
