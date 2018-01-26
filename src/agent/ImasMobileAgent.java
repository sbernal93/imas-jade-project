package agent;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import map.Cell;
import onthology.GameSettings;
import util.Dijkstra;
import util.Movement;
import util.Plan;
import util.Vertex;

public abstract class ImasMobileAgent extends ImasAgent{

	public ImasMobileAgent(AgentType type) {
		super(type);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Cell cell;
	private Dijkstra dijkstra;
	private GameSettings game;
	private List<Plan> plans;

	public void setCell(Cell cell) {
		this.cell = cell;
	}
	public Cell getCell() {
		return this.cell;
	}
	public GameSettings getGame() {
		return game;
	}
	public void setGame(GameSettings game) {
		this.game = game;
	}
	public List<Plan> getPlan() {
		return plans;
	}

	public void setPlan(List<Plan> plans) {
		this.plans = plans;
	}
	
	public void addPlan(Plan plan) {
		if(this.plans == null) {
			this.plans = new ArrayList<>();
		}
		this.plans.add(plan);
	}
	
	public void removePlan(Plan plan) {
		if(this.plans != null) {
			this.plans.remove(plan);
		}
	}


	/**
	 * Finds the shortest path from the current cell to the destination cell
	 * using {@link Dijkstra}
	 * @param destination
	 * @return
	 */
	public List<Movement> findShortestPath(Cell destination) {
		if(dijkstra == null) {
			dijkstra = new Dijkstra(game.buildGraphFromMap());
		}
		dijkstra.execute(new Vertex("", cell));
		List<Vertex> path = dijkstra.getPath(new Vertex("", destination));
		
		return dijkstra.getMovementsFromVertices(path, this);
	}

}
