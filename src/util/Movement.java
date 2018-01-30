package util;

import java.io.Serializable;

import agent.AgentType;
import jade.core.AID;
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
	
	private AgentType agentType;
	private AID agent;
	private Cell oldCell;
	private Cell newCell;
	private MovementStatus status;
	private MovementType type;
	private Cell mcCell;
	private MetalDiscovery metal;
	
	public Movement(){
	}
	
	public Movement(AgentType atype, AID agent, Cell oldCell, Cell newCell) {
		super();
		this.agent = agent;
		this.oldCell = oldCell;
		this.newCell = newCell;
		this.agentType = atype;
		this.status = MovementStatus.PROPOSAL;
		this.type = MovementType.NORMAL;
	}
	
	public Movement(AID agent, Cell oldCell, Cell newCell, MovementType type, AgentType atype) {
		super();
		this.agent = agent;
		this.oldCell = oldCell;
		this.newCell = newCell;
		this.type = type;
		this.agentType = atype;
		this.status = MovementStatus.PROPOSAL;
	}

	
	
	public Movement(AID agent, Cell oldCell, Cell newCell, MovementType type,
			MetalDiscovery metal, AgentType atype) {
		super();
		this.agent = agent;
		this.oldCell = oldCell;
		this.newCell = newCell;
		this.status = MovementStatus.PROPOSAL;
		this.type = type;
		this.agentType = atype;
		this.metal= metal;
	}
	
	public Movement(AID agent, Cell oldCell, Cell newCell, Cell mcCell, MovementType type, AgentType atype) {
		super();
		this.agent = agent;
		this.oldCell = oldCell;
		this.newCell = newCell;
		this.status = MovementStatus.PROPOSAL;
		this.type = type;
		this.agentType = atype;
		this.mcCell = mcCell;
	}

	public AID getAgent() {
		return agent;
	}
	public void setAgent(AID agent) {
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
		return new InfoAgent(agentType, this.getAgent());
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

	public AgentType getAgentType() {
		return agentType;
	}

	public void setAgentType(AgentType agentType) {
		this.agentType = agentType;
	}
	
	

}
