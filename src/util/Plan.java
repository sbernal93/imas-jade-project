package util;

import java.util.ArrayList;
import java.util.List;

/**
 * A plan for mobile agents
 * @author santiagobernal
 *
 */
public class Plan {

	private List<Movement> movements;

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
}
