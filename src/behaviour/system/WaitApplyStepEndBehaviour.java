package behaviour.system;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import agent.SystemAgent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import map.CellType;
import map.FieldCell;
import onthology.InitialGameSettings;

public class WaitApplyStepEndBehaviour extends SimpleBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int FINISHED_SIMULATION = 1;
	public static final int CONTINUE_SIMULATION = 0;
	
	
	private boolean finished;
	private SystemAgent agent;
	private MessageTemplate mt;
	
	public WaitApplyStepEndBehaviour(SystemAgent agent, MessageTemplate mt) {
		super(agent);
		this.agent = agent;
		this.mt = mt;
	}

	@Override
	public void action() {
		this.agent.log("WaitApplyStepEndBehaviour");
		ACLMessage message = agent.receive(mt);
		if(message != null){
			this.agent.log("Got message");
				finished = true;
		} else {
			block(10000);
		}
		
	}
	
	@Override
	public void reset() {
		finished = false;
		super.reset();
	}

	@Override
	public boolean done() {
		return finished;
	}
	
	@Override
	public int onEnd() {
		reset();
		this.agent.log("WaitApplyStepEndBehaviour finished, current Step: " + this.agent.getCurrentStep() );
		
		long time = System.currentTimeMillis() - this.agent.getStepStartTime();
		this.agent.setTotalTime(this.agent.getTotalTime() + time);
		this.agent.getGui().showStatistics("Current step: " + this.agent.getCurrentStep() + ", of " 
			+ this.agent.getGame().getSimulationSteps() + ", step time: " + time 
			+ "ms, total time: " + this.agent.getTotalTime() + "ms \n");
		
		if(this.agent.getGame().getSimulationSteps() <= this.agent.getCurrentStep()) {
			this.agent.log("Simulation finished");
			this.agent.getGui().showStatistics(buildStatistics());
			return FINISHED_SIMULATION;
		}
		this.agent.log("Simulation continues");
		this.agent.setCurrentStep(this.agent.getCurrentStep() + 1);
		((InitialGameSettings) this.agent.getGame()).addElementsForThisSimulationStep();
		this.agent.updateGUI();
		this.agent.setMovementsProposed(new ArrayList<>());
		return CONTINUE_SIMULATION;
	}
	
	private String buildStatistics() {
		
		List<FieldCell> allMetalsSet = 
				this.agent.getGame().getCellsOfType().get(CellType.FIELD)
				.stream()
				.filter(c -> ((FieldCell) c).getMetalTimeSet()!= null && ((FieldCell) c).getMetalTimeSet().size()>0)
				.map(c -> (FieldCell) c).collect(Collectors.toList());
		
		StringJoiner statistics = new StringJoiner("\n");
		statistics.add("Benefits: " + this.agent.getPriceObtained());
		statistics.add("Manufactured Metal: " + this.agent.getAmountOfMineTurnedIn());
		statistics.add("Average benefit for unit of metal: "
		+  (double)this.agent.getPriceObtained()/(double)this.agent.getAmountOfMineTurnedIn());
		return statistics.toString() + "\n" + metalDiscoveryStatistics(allMetalsSet);
	}
	
	private String metalDiscoveryStatistics(List<FieldCell> allMetalsSet) {
		StringJoiner statistics = new StringJoiner("\n");
		double avgSum = 0;
		double amDiscovered = 0;
		double amNotDiscovered = 0;
		double amDug = 0;
		//double amNotDug = 0;
		double avgDugSum = 0;
		
		
		for(FieldCell fc : allMetalsSet) {
			for(int i = 0; i<=fc.getMetalTimeSet().size();i++) {
				if(fc.getMetalTimeDiscovery()== null || fc.getMetalTimeDiscovery().size()<=i) {
					amNotDiscovered ++;
				} else {
					amDiscovered ++;
					avgSum += fc.getMetalTimeDiscovery().get(i) - fc.getMetalTimeSet().get(i);
					if(fc.getMetalTimeStartedDiggin()==null || fc.getMetalTimeStartedDiggin().size()<=i) {
						//amNotDug ++;
					} else {
						amDug ++;
						avgDugSum += fc.getMetalTimeStartedDiggin().get(i) - fc.getMetalTimeDiscovery().get(i);
					}
				}
				
			}
		}
		NumberFormat formatter = new DecimalFormat("#0.00");
		statistics.add("Average time for discovering metal: " + formatter.format(avgSum/amDiscovered) + "ms");
		statistics.add("Average time for digging metal: " + formatter.format(avgDugSum/amDug) + "ms");
		statistics.add("Ratio of discovered metal: " + formatter.format(amDiscovered/(amNotDiscovered + amDiscovered)));
		statistics.add("Ratio of collected metal: " + formatter.format(
				(this.agent.getAmountOfMineTurnedIn() + (this.agent.getAmountOfMineDugUp() - this.agent.getAmountOfMineTurnedIn()))
				/(amNotDiscovered + amDiscovered)));
		
		return statistics.toString();
	}
 	
	
	 public void onStart() {
	    reset();
	  }

}
