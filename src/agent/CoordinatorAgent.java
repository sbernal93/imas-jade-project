/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package agent;

import onthology.GameSettings;
import behaviour.coordinator.RequesterBehaviour;
import behaviour.BaseSearchAgentBehaviour;
import behaviour.coordinator.CreateDiggerCoordinatorBehaviour;
import behaviour.coordinator.CreateProspectorCoordinatorBehaviour;
import behaviour.coordinator.RequestResponseBehaviour;
import onthology.MessageContent;
import jade.core.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.*;

/**
 * The main Coordinator agent. 
 * TODO: This coordinator agent should get the game settings from the System
 * agent every round and share the necessary information to other coordinators.
 */
public class CoordinatorAgent extends ImasAgent {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
     * Game settings in use.
     */
    private GameSettings game;
    /**
     * System agent id.
     */
    private AID systemAgent;
    
    /**
     * DiggerCoordinatorAgent id
     */
    private AID diggerCoordinatorAgent;
    
    /**
     * ProspectorCoordinatorAgent id
     */
    private AID prospectorCoordinatorAgent;

    /**
     * Builds the coordinator agent.
     */
    public CoordinatorAgent() {
        super(AgentType.COORDINATOR);
    }

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {
    	this.setGame((GameSettings) this.getArguments()[0]);
    	this.addBehaviour(new CreateDiggerCoordinatorBehaviour(this, AgentType.DIGGER_COORDINATOR));
        this.addBehaviour(new CreateProspectorCoordinatorBehaviour(this, AgentType.PROSPECTOR_COORDINATOR));
        
        this.addBehaviour(new BaseSearchAgentBehaviour(this, AgentType.DIGGER_COORDINATOR) {

			private static final long serialVersionUID = 1L;

			@Override
			public void setAgent(AID agent) {
				((CoordinatorAgent) this.getAgent()).setDiggerCoordinatorAgent(agent);
			}
		});
        this.addBehaviour(new BaseSearchAgentBehaviour(this, AgentType.PROSPECTOR_COORDINATOR) {

			private static final long serialVersionUID = 1L;

			@Override
			public void setAgent(AID agent) {
				((CoordinatorAgent) this.getAgent()).setProspectorCoordinatorAgent(agent);
			}
		});
        

        // search SystemAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.SYSTEM.toString());
        this.systemAgent = UtilsAgents.searchAgent(this, searchCriterion);
        
        log("Finished setup");
        
        //TODO: this 
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        this.addBehaviour(new RequestResponseBehaviour(this, mt));

        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.COORDINATOR.toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
            log("Registered to the DF");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " registration with DF unsucceeded. Reason: " + e.getMessage());
            doDelete();
        }

        // searchAgent is a blocking method, so we will obtain always a correct AID

        /* ********************************************************************/
        /*ACLMessage initialRequest = new ACLMessage(ACLMessage.REQUEST);
        initialRequest.clearAllReceiver();
        initialRequest.addReceiver(this.systemAgent);
        initialRequest.setProtocol(InteractionProtocol.FIPA_REQUEST);
        log("Request message to agent");
        try {
            initialRequest.setContent(MessageContent.GET_MAP);
            log("Request message content:" + initialRequest.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //we add a behaviour that sends the message and waits for an answer
        this.addBehaviour(new RequesterBehaviour(this, initialRequest));
        
       
        // add behaviours
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        this.addBehaviour(new RequestResponseBehaviour(this, mt));

*/
        // setup finished. When we receive the last inform, the agent itself will add
        // a behaviour to send/receive actions. we create the other coordinator agents
        
    }

    /**
     * Update the game settings.
     *
     * @param game current game settings.
     */
    public void setGame(GameSettings game) {
        this.game = game;
    }

    /**
     * Gets the current game settings.
     *
     * @return the current game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }

    public void setDiggerCoordinatorAgent(AID diggerCoordinatorAgent) {
    	this.diggerCoordinatorAgent = diggerCoordinatorAgent;
    }
    
    public void setProspectorCoordinatorAgent(AID prospectorCoordinatorAgent) {
    	this.prospectorCoordinatorAgent = prospectorCoordinatorAgent;
    }
}

