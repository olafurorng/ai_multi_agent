package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.math.BigDecimal;

import searchclient.ColorHelper.*;
import searchclient.Goals.*;
import searchclient.Box.*;
import searchclient.Command.Type;

public class Node {
	private static final Random RND = new Random(1);

	/**
	 * This method should be called once, in the beginning when we have loaded
	 * in the level and we know the number of columns and rows of the level
	 */
	public static void setLevelSize(final int numberOfRows, final int numberOfCol) {
		MAX_ROW = numberOfRows;
		MAX_COL = numberOfCol;
	}

	public static int MAX_ROW;
	public static int MAX_COL;

	public int agentRow;
	public int agentCol;
	public static Color agentColor; // NOTE: nullable for SA levels

	// Arrays are indexed from the top-left of the level, with first index being row and second being column.
	// Row 0: (0,0) (0,1) (0,2) (0,3) ...
	// Row 1: (1,0) (1,1) (1,2) (1,3) ...
	// Row 2: (2,0) (2,1) (2,2) (2,3) ...
	// ...
	// (Start in the top left corner, first go down, then go right)
	// E.g. this.walls[2] is an array of booleans having size MAX_COL.
	// this.walls[row][col] is true if there's a wall at (row, col)
	//

	public static HashMap<String, Boolean> walls = new HashMap<String, Boolean>();
	public HashMap<String, Box> boxMap = new HashMap<String, Box>();
	public static HashMap<String, Goals> goals = new HashMap<String, Goals>();

	public String newBox;

	public Node parent;
	public Command action;

	private int g;
	
	private int _hash = 0;

	public Node(Node parent) {
		this.parent = parent;
		if (parent == null) {
			this.g = 0;
		} else {
			this.g = parent.g() + 1;
		}
	}

	public int g() {
		return this.g;
	}

	public boolean isInitialState() {
		return this.parent == null;
	}

	public boolean isGoalState() {
		for (String key : goals.keySet()) {
			String[] goalArray = key.split(",");
			char g = goals.get(goalArray[0] + "," + goalArray[1]).getCharacter();						
			char b = 0;
			
			if (this.boxMap.get(goalArray[0] + "," + goalArray[1]) != null) {
				b = Character.toLowerCase(this.boxMap.get(goalArray[0] + "," + goalArray[1]).getCharacter());
			}
			
			if (g > 0 && b != g) {
				return false;
			}		
		}

		return true;
	}

	public ArrayList<Node> getExpandedNodes() {
	
		ArrayList<Node> expandedNodes = new ArrayList<Node>(Command.EVERY.length);
		for (Command c : Command.EVERY) {
			// Determine applicability of action
			int newAgentRow = this.agentRow + Command.dirToRowChange(c.dir1);
			int newAgentCol = this.agentCol + Command.dirToColChange(c.dir1);

			// System.err.println("Agent: " + newAgentCol + "," + newAgentRow);
			// System.err.println("Boxes: " + boxMap);
			if (c.actionType == Type.Move) {
				// Check if there's a wall or box on the cell to which the agent is moving
				if (this.cellIsFree(newAgentRow, newAgentCol)) {
					Node n = this.ChildNode();
					n.action = c;
					n.agentRow = newAgentRow;
					n.agentCol = newAgentCol;
			
					expandedNodes.add(n);
				}
			} else if (c.actionType == Type.Push) {
				// Make sure that there's actually a box to move
				if (this.boxAt(newAgentRow, newAgentCol)) {
					int newBoxRow = newAgentRow + Command.dirToRowChange(c.dir2);
					int newBoxCol = newAgentCol + Command.dirToColChange(c.dir2);
					// .. and that new cell of box is free
					if (this.cellIsFree(newBoxRow, newBoxCol)) {
						Node n = this.ChildNode();
						n.action = c;
						n.agentRow = newAgentRow;
						n.agentCol = newAgentCol;

						//long start = System.nanoTime();
						Box currentBox =  n.boxMap.get(newAgentRow + "," + newAgentCol);
						//System.err.println("char: " + currentBox.getCharacter() + "pri: " + currentBox.getAssign() );
						
						n.boxMap.remove(newAgentRow + "," + newAgentCol);
						Box box = new Box(currentBox.getCharacter(), currentBox.getAssign());

						n.boxMap.put(newBoxRow + "," + newBoxCol, box);
						n.newBox = newBoxRow + "," + newBoxCol;
						//System.err.println("Boxes: " + n.boxMap);
						// System.err.println("Box Push: " + newBoxRow + "," + newBoxCol);

						//long end = System.nanoTime();
						//long microseconds = (end - start) / 1000;
						//System.err.println("Timer: " + microseconds + " microseconds");
						
						expandedNodes.add(n);
					}
				}
			} else if (c.actionType == Type.Pull) {
				// Cell is free where agent is going
				if (this.cellIsFree(newAgentRow, newAgentCol)) {
					int boxRow = this.agentRow + Command.dirToRowChange(c.dir2);
					int boxCol = this.agentCol + Command.dirToColChange(c.dir2);
					// .. and there's a box in "dir2" of the agent
					if (this.boxAt(boxRow, boxCol)) {
						Node n = this.ChildNode();
						n.action = c;
						n.agentRow = newAgentRow;
						n.agentCol = newAgentCol;

						Box currentBox =  n.boxMap.get(boxRow + "," + boxCol);
						n.boxMap.remove(boxRow + "," + boxCol);
						
						Box box = new Box(currentBox.getCharacter(), currentBox.getAssign());
						
						n.boxMap.put(this.agentRow + "," + this.agentCol, box);
						n.newBox = this.agentRow + "," + this.agentCol;
						// System.err.println("Box Pull: " + this.agentRow + "," + this.agentCol);

						expandedNodes.add(n);
					}
				}
			}
		}
						
		Collections.shuffle(expandedNodes, RND);
			
		return expandedNodes;
	}

	public boolean cellIsFree(int row, int col) {
		return walls.get(row + "," + col) == null && this.boxMap.get(row + "," + col) == null;
	}

    public boolean cellIsFreeAndNoGoalOrAgent(int row, int col) {
        return  walls.get(row + "," + col) == null && this.boxMap.get(row + "," + col) == null && !(agentRow == row && agentCol == col) && goals.get(row + "," + col) == null;
    }

	public boolean cellIsFreeOfGoalBoxAndAgent(int row, int col) {
		return !(agentRow == row && agentCol == col) && this.boxMap.get(row + "," + col) == null && goals.get(row + "," + col) == null;
	}

	private boolean boxAt(int row, int col) {
		return this.boxMap.get(row + "," + col) != null;
	}

	private Node ChildNode() {
		Node copy = new Node(this);
    
		copy.boxMap = new HashMap<String,Box>(this.boxMap);

		return copy;
	}

	public LinkedList<Node> extractPlan() {
		LinkedList<Node> plan = new LinkedList<Node>();
		Node n = this;
		while (!n.isInitialState()) {
			plan.addFirst(n);
			n = n.parent;
		}
		return plan;
	}

	@Override
	public int hashCode() {
		if (this._hash == 0) {
			final int prime = 31;
			int result = 1;
			result = prime * result + this.agentCol;
			result = prime * result + this.agentRow;
			result = prime * result + (this.boxMap != null ? this.boxMap.hashCode() : 0);
			result = prime * result + (goals != null ? goals.hashCode() : 0);
			result = prime * result + (walls != null ? walls.hashCode() : 0);
			this._hash = result;
		}
		return this._hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (this.agentRow != other.agentRow || this.agentCol != other.agentCol)
			return false;
		if (this.boxMap != null ? !this.boxMap.equals(other.boxMap) : other.boxMap != null) return false;
		return true;
	}


	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (int row = 0; row < MAX_ROW; row++) {
			if (walls.get(row + "," + 0) == null) {
					break;
			}
			for (int col = 0; col < MAX_COL; col++) {
				if (this.boxMap.get(row + "," + col) != null) {
					s.append(this.boxMap.get(row + "," + col).getCharacter());
				} else if (goals.get(row + "," + col) != null) {
					s.append(goals.get(row + "," + col).getCharacter());
				} else if (walls.get(row + "," + col) != null) {
					s.append("+");
				} else if (row == this.agentRow && col == this.agentCol) {
					s.append("0");
				} else {
					s.append(" ");
				}
			}
			s.append("\n");
		}
		return s.toString();
	}

}