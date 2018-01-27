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
package onthology;

/**
 * Content messages for inter-agent communication.
 */
public class MessageContent {
    
    /**
     * Message sent from Coordinator agent to System agent to get the whole
     * city information.
     */
    public static final String GET_MAP = "Get map";
    
    /**
     * MEssage sent from the system agent to the coordinator agent to start a new step
     */
    public static String NEW_STEP = "New step";
    
    
    /**
     * MEssage for requesting the step result
     */
    public static String STEP_RESULT = "Step result";
    
    public static String STEP_FINISHED = "Step finished";
}
