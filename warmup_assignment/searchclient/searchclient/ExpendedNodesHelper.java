package searchclient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
/**
 * Created by olafurorn on 4/16/18.
 */
public class ExpendedNodesHelper {


    public ExpendedNodesHelper() {

    }

    public ArrayList<Node> getExpandedNodes(Node parentNode, Node nodeBefore, int agentIndex) {
        ArrayList<Node> expandedNodes = new ArrayList<Node>(Command.EVERY.length);
        for (Command c : Command.EVERY) {
            // Determine applicability of action
            int newAgentRow = nodeBefore.agentsRow[agentIndex] + Command.dirToRowChange(c.dir1);
            int newAgentCol = nodeBefore.agentsCol[agentIndex] + Command.dirToColChange(c.dir1);

            if (c.actionType == Command.Type.Move) {
                // Check if there's a wall or box on the cell to which the agent is moving
                if (nodeBefore.cellIsFree(newAgentRow, newAgentCol)) {
                    Node n = nodeBefore.ChildNode(parentNode, nodeBefore);
                    n.actions[agentIndex] = c;
                    n.agentsRow[agentIndex] = newAgentRow;
                    n.agentsCol[agentIndex] = newAgentCol;
                    expandedNodes.add(n);
                }
            } else if (c.actionType == Command.Type.Push) {
                // Make sure that there's actually a box to move
                if (nodeBefore.boxAt(newAgentRow, newAgentCol)) {
                    int newBoxRow = newAgentRow + Command.dirToRowChange(c.dir2);
                    int newBoxCol = newAgentCol + Command.dirToColChange(c.dir2);
                    // .. and that new cell of box is free
                    if (nodeBefore.cellIsFree(newBoxRow, newBoxCol)) {
                        Node n = nodeBefore.ChildNode(parentNode, nodeBefore);
                        n.actions[agentIndex] = c;
                        n.agentsRow[agentIndex] = newAgentRow;
                        n.agentsCol[agentIndex] = newAgentCol;

                        Box currentBox =  n.boxMap.get(new Coordinate(newAgentRow, newAgentCol));

                        n.boxMap.remove(new Coordinate(newAgentRow, newAgentCol));
                        Box box = new Box(currentBox.getCharacter(), currentBox.getAssign());

                        n.boxMap.put(new Coordinate(newBoxRow, newBoxCol), box);
                        n.newBox = new Coordinate(newBoxRow, newBoxCol);

                        expandedNodes.add(n);
                    }
                }
            } else if (c.actionType == Command.Type.Pull) {
                // Cell is free where agent is going
                if (nodeBefore.cellIsFree(newAgentRow, newAgentCol)) {
                    int boxRow = nodeBefore.agentsRow[agentIndex] + Command.dirToRowChange(c.dir2);
                    int boxCol = nodeBefore.agentsCol[agentIndex] + Command.dirToColChange(c.dir2);
                    // .. and there's a box in "dir2" of the agent
                    if (nodeBefore.boxAt(boxRow, boxCol)) {
                        Node n = nodeBefore.ChildNode(parentNode, nodeBefore);
                        n.actions[agentIndex] = c;
                        n.agentsRow[agentIndex] = newAgentRow;
                        n.agentsCol[agentIndex] = newAgentCol;

                        Box currentBox =  n.boxMap.get(new Coordinate(boxRow, boxCol));
                        n.boxMap.remove(new Coordinate(boxRow, boxCol));

                        Box box = new Box(currentBox.getCharacter(), currentBox.getAssign());

                        n.boxMap.put(new Coordinate(nodeBefore.agentsRow[agentIndex], nodeBefore.agentsCol[agentIndex]), box);
                        n.newBox = new Coordinate(nodeBefore.agentsRow[agentIndex], nodeBefore.agentsCol[agentIndex]);

                        expandedNodes.add(n);
                    }
                }
            }
        }
        return expandedNodes;
    }

}