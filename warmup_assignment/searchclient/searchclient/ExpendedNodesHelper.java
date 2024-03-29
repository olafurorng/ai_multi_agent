package searchclient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
/**
 * Created by olafurorn on 4/16/18.
 */
public class ExpendedNodesHelper {


    public ExpendedNodesHelper() {

    }

    public ArrayList<Node> getExpandedNodes(Node parentNode, Node nodeBefore, int agentIndex) {
        ArrayList<Node> expandedNodes = new ArrayList<Node>(Command.EVERY.length);

        // skip expanding nodes if agent has finished all his goals and there is no signal
        if (Node.NUMBER_OF_AGENTS > 1) {
            // multi agent
            boolean missingGoalFound = false;
            for (Map.Entry<Coordinate, Goals> entry : Node.GOALS.entrySet()) {
                int goalRow = entry.getKey().getX();
                int goalCol = entry.getKey().getY();
                Goals currentGoal = entry.getValue();
                Box currentBox = nodeBefore.boxMap.get(new Coordinate(goalRow, goalCol));

                if (currentGoal.getColor() == Node.agentsColor[agentIndex]) {
                    if ((currentBox == null) || (currentBox != null && Character.toLowerCase(currentBox.getCharacter()) != currentGoal.getCharacter())) {
                        missingGoalFound = true;
                        break;
                    }
                }
            }

            if (!missingGoalFound && (Heuristic.agentCommunications.getTouchedBox() == null || Heuristic.agentCommunications.getTouchedBox().getColor() != Node.agentsColor[agentIndex])) {
                return expandedNodes;
            }
        }

        for (Command c : Command.EVERY) {
            // Determine applicability of action
            int newAgentRow = nodeBefore.agentsRow[agentIndex] + Command.dirToRowChange(c.dir1);
            int newAgentCol = nodeBefore.agentsCol[agentIndex] + Command.dirToColChange(c.dir1);

            if (c.actionType == Command.Type.Move) {
                // Check if there's a wall or box on the cell to which the agent is moving
                if (nodeBefore.cellIsFree(newAgentRow, newAgentCol) && parentNode.cellIsFree(newAgentRow, newAgentCol)) { // we have to check if both the nodeBefore and the parentNode has free cell
                    Node n = nodeBefore.ChildNode(parentNode, nodeBefore);
                    n.actions[agentIndex] = c;
                    n.agentsRow[agentIndex] = newAgentRow;
                    n.agentsCol[agentIndex] = newAgentCol;
                    expandedNodes.add(n);
                }
            } else if (c.actionType == Command.Type.Push) {
                // Make sure that there's actually a box to move
                if (nodeBefore.boxAt(newAgentRow, newAgentCol)
                        && Node.agentsColor[agentIndex] == nodeBefore.boxMap.get(new Coordinate(newAgentRow, newAgentCol)).getColor()) { // checking if the box and the agent has the same colors
                    int newBoxRow = newAgentRow + Command.dirToRowChange(c.dir2);
                    int newBoxCol = newAgentCol + Command.dirToColChange(c.dir2);
                    // .. and that new cell of box is free
                    if (nodeBefore.cellIsFree(newBoxRow, newBoxCol) && parentNode.cellIsFree(newBoxRow, newBoxCol)) { // we have to check if both the nodeBefore and the parentNode has free cell
                        Node n = nodeBefore.ChildNode(parentNode, nodeBefore);
                        n.actions[agentIndex] = c;
                        n.agentsRow[agentIndex] = newAgentRow;
                        n.agentsCol[agentIndex] = newAgentCol;

                        Box currentBox =  n.boxMap.get(new Coordinate(newAgentRow, newAgentCol));

                        if (Heuristic.agentCommunications.getTouchedBox() != null && Heuristic.agentCommunications.getTouchedBox().getCharacter() == currentBox.getCharacter()) {
                            int pCounter = Heuristic.agentCommunications.getPullCounter();

                            if (pCounter < 1) {
                                Heuristic.agentCommunications.removeBox();
                            }
                            else {
                                Heuristic.agentCommunications.setPullCounter(pCounter - 1);
                            }
                        }
                                             
                        n.boxMap.remove(new Coordinate(newAgentRow, newAgentCol));
                        Box box = new Box(currentBox.getCharacter(), currentBox.getAssign(), new Coordinate(newBoxRow, newBoxCol));

                        n.boxMap.put(new Coordinate(newBoxRow, newBoxCol), box);
                        
                        n.newBox.put(Integer.toString(agentIndex), new Coordinate(newBoxRow, newBoxCol));

                        expandedNodes.add(n);
                    }
                } else if (nodeBefore.boxAt(newAgentRow, newAgentCol)) {
                    // just not the same color of the agent and the box
 
                    if(Heuristic.agentCommunications.getTouchedBox() == null || 
                    (Heuristic.agentCommunications.getTouchedBox() != null && Heuristic.agentCommunications.getTouchedBox().getColor() == Node.agentsColor[agentIndex])) {
                        Heuristic.agentCommunications.onBoxWithOtherColorTouched(new Coordinate(newAgentRow, newAgentCol), nodeBefore.boxMap.get(new Coordinate(newAgentRow, newAgentCol)));                
                    }

                }
            } else if (c.actionType == Command.Type.Pull) {
                // Cell is free where agent is going
                if (nodeBefore.cellIsFree(newAgentRow, newAgentCol) && parentNode.cellIsFree(newAgentRow, newAgentCol)) { // we have to check if both the nodeBefore and the parentNode has free cell
                    int boxRow = nodeBefore.agentsRow[agentIndex] + Command.dirToRowChange(c.dir2);
                    int boxCol = nodeBefore.agentsCol[agentIndex] + Command.dirToColChange(c.dir2);
                    // .. and there's a box in "dir2" of the agent
                    if (nodeBefore.boxAt(boxRow, boxCol)
                            && Node.agentsColor[agentIndex] == nodeBefore.boxMap.get(new Coordinate(boxRow, boxCol)).getColor()) { // checking if the box and the agent has the same colors
                        Node n = nodeBefore.ChildNode(parentNode, nodeBefore);
                        n.actions[agentIndex] = c;
                        n.agentsRow[agentIndex] = newAgentRow;
                        n.agentsCol[agentIndex] = newAgentCol;

                        Box currentBox =  n.boxMap.get(new Coordinate(boxRow, boxCol));

                        if (Heuristic.agentCommunications.getTouchedBox() != null && Heuristic.agentCommunications.getTouchedBox().getCharacter() == currentBox.getCharacter()) {
                            int pCounter = Heuristic.agentCommunications.getPullCounter();
                            if (pCounter < 1) {
                                Heuristic.agentCommunications.removeBox();
                            }
                            else {
                                Heuristic.agentCommunications.setPullCounter(pCounter - 1);
                            }
                        }

                        n.boxMap.remove(new Coordinate(boxRow, boxCol));

                        Box box = new Box(currentBox.getCharacter(), currentBox.getAssign(), new Coordinate(nodeBefore.agentsRow[agentIndex], nodeBefore.agentsCol[agentIndex]));

                        n.boxMap.put(new Coordinate(nodeBefore.agentsRow[agentIndex], nodeBefore.agentsCol[agentIndex]), box);
                        n.newBox.put(Integer.toString(agentIndex), new Coordinate(nodeBefore.agentsRow[agentIndex], nodeBefore.agentsCol[agentIndex]));

                        expandedNodes.add(n);
                    } else if (nodeBefore.boxAt(boxRow, boxCol)) {
                        // just not the same color of the agent and the box
                        if(Heuristic.agentCommunications.getTouchedBox() == null || 
                        (Heuristic.agentCommunications.getTouchedBox() != null && Heuristic.agentCommunications.getTouchedBox().getColor() == Node.agentsColor[agentIndex]) ) { 
                            Heuristic.agentCommunications.onBoxWithOtherColorTouched(new Coordinate(boxRow, boxCol), nodeBefore.boxMap.get(new Coordinate(boxRow, boxCol)));                     
                        }
                      
                    }
                }
            }
        }
        return expandedNodes;
    }

}
