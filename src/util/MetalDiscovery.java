package util;

import java.io.Serializable;

import map.FieldCell;
import onthology.MetalType;

public class MetalDiscovery implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private FieldCell cell;
	private MetalType type;
	private int amount;
	
	public FieldCell getCell() {
		return cell;
	}
	public void setCell(FieldCell cell) {
		this.cell = cell;
	}
	public MetalType getType() {
		return type;
	}
	public void setType(MetalType type) {
		this.type = type;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	
}
