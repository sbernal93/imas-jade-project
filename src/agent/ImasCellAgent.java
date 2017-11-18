package agent;

import map.Cell;

public class ImasCellAgent extends ImasAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Cell cell;
	
	public ImasCellAgent(AgentType type) {
		this(type, null);
	}
	
	public ImasCellAgent(AgentType type, Cell cell) {
		super(type);
		this.cell = cell;
	}

	public void setCell(Cell cell) {
		this.cell = cell;
	}
	
	public Cell getCell() {
		return this.cell;
	}


}
