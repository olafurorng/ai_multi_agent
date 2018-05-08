package searchclient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by olafurorn on 4/16/18.
 */
public class ExpendedNodesHelper {
    private static final Random RND = new Random(1);

    public ExpendedNodesHelper() {

    }

    public ArrayList<Node> getExpandedNodes(Node n, int agentIndex) {
        ArrayList<Node> expandedNodes = new ArrayList<Node>(Command.EVERY.length);
        for (Command c : Command.EVERY) {
            // Determine applicability of action
            int newAgentRow = n.agentsRow[agentIndex] + Command.dirToRowChange(c.dir1);
            int newAgentCol = n.agentsCol[agentIndex] + Command.dirToColChange(c.dir1);

            if (c.actionType == Command.Type.Move) {
                // Check if there's a wall or box on the cell to which the agent is moving
                if (n.cellIsFree(newAgentRow, newAgentCol)) {
                    Node newNode = n.ChildNode(agentIndex != 0);
                    newNode.actions[agentIndex] = c;
                    newNode.agentsRow[agentIndex] = newAgentRow;
                    newNode.agentsCol[agentIndex] = newAgentCol;
                    expandedNodes.add(newNode);
                }
            } else if (c.actionType == Command.Type.Push) {
                // Make sure that there's actually a box to move
                if (n.boxAt(newAgentRow, newAgentCol)) {
                    int newBoxRow = newAgentRow + Command.dirToRowChange(c.dir2);
                    int newBoxCol = newAgentCol + Command.dirToColChange(c.dir2);
                    // .. and that new cell of box is free
                    if (n.cellIsFree(newBoxRow, newBoxCol)) {
                        Node newNode = n.ChildNode(agentIndex != 0);
                        newNode.actions[agentIndex] = c;
                        newNode.agentsRow[agentIndex] = newAgentRow;
                        newNode.agentsCol[agentIndex] = newAgentCol;

                        newNode.boxes[newBoxRow][newBoxCol] = n.boxes[newAgentRow][newAgentCol];
                        newNode.boxes[newAgentRow][newAgentCol] = 0;

                        expandedNodes.add(newNode);
                    }
                }
            } else if (c.actionType == Command.Type.Pull) {
                // Cell is free where agent is going
                if (n.cellIsFree(newAgentRow, newAgentCol)) {
                    int boxRow = n.agentsRow[agentIndex] + Command.dirToRowChange(c.dir2);
                    int boxCol = n.agentsCol[agentIndex] + Command.dirToColChange(c.dir2);
                    // .. and there's a box in "dir2" of the agent
                    if (n.boxAt(boxRow, boxCol)) {
                        Node newNode = n.ChildNode(agentIndex != 0);
                        newNode.actions[agentIndex] = c;
                        newNode.agentsRow[agentIndex] = newAgentRow;
                        newNode.agentsCol[agentIndex] = newAgentCol;

                        newNode.boxes[n.agentsRow[agentIndex]][n.agentsCol[agentIndex]] = n.boxes[boxRow][boxCol];
                        newNode.boxes[boxRow][boxCol] = 0;

                        expandedNodes.add(newNode);
                    }
                }
            }
        }
        Collections.shuffle(expandedNodes, RND);
        return expandedNodes;
    }
}
