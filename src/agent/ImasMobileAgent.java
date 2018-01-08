package agent;

import map.Cell;

public abstract class ImasMobileAgent extends ImasAgent{

	public ImasMobileAgent(AgentType type) {
		super(type);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Cell cell;

	public void setCell(Cell cell) {
		this.cell = cell;
	}
	
	public Cell getCell() {
		return this.cell;
	}

	

}
