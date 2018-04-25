package searchclient;

import java.util.*;

import searchclient.ColorHelper.*;
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
	// E.g. this.WALLS[2] is an array of booleans having size MAX_COL.
	// this.WALLS[row][col] is true if there's a wall at (row, col)
	//

	public static List<Coordinate> WALLS = new ArrayList<Coordinate>();
	public Map<Coordinate, Box> boxMap = new HashMap<Coordinate, Box>();
	public static HashMap<Coordinate, Goals> GOALS = new HashMap<Coordinate, Goals>();

	public Coordinate newBox;

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
		for (Coordinate coordinate : GOALS.keySet()) {
			char g = GOALS.get(coordinate).getCharacter();
			char b = 0;
			
			if (this.boxMap.get(coordinate) != null) {
				b = Character.toLowerCase(this.boxMap.get(coordinate).getCharacter());
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

						Box currentBox =  n.boxMap.get(new Coordinate(newAgentRow, newAgentCol));
						
						n.boxMap.remove(new Coordinate(newAgentRow, newAgentCol));
						Box box = new Box(currentBox.getCharacter(), currentBox.getPriority());

						n.boxMap.put(new Coordinate(newBoxRow, newBoxCol), box);
						n.newBox = new Coordinate(newBoxRow, newBoxCol);
						
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

						Box currentBox =  n.boxMap.get(new Coordinate(boxRow, boxCol));
						n.boxMap.remove(new Coordinate(boxRow, boxCol));
						
						Box box = new Box(currentBox.getCharacter(), currentBox.getPriority());
						
						n.boxMap.put(new Coordinate(this.agentRow, this.agentCol), box);
						n.newBox = new Coordinate(this.agentRow, this.agentCol);
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
		Coordinate coordinate = new Coordinate(row, col);
		return !WALLS.contains(coordinate) && this.boxMap.get(coordinate) == null;
	}

    public boolean cellIsFreeAndNoGoalOrAgent(int row, int col) {
		Coordinate coordinate = new Coordinate(row, col);
        return  !WALLS.contains(coordinate) && this.boxMap.get(coordinate) == null && !(agentRow == row && agentCol == col) && GOALS.get(coordinate) == null;
    }

	public boolean cellIsFreeOfGoalBoxAndAgent(int row, int col) {
		Coordinate coordinate = new Coordinate(row, col);
		return !(agentRow == row && agentCol == col) && this.boxMap.get(coordinate) == null && GOALS.get(coordinate) == null;
	}

	private boolean boxAt(int row, int col) {
		return this.boxMap.get(new Coordinate(row, col)) != null;
	}

	private Node ChildNode() {
		Node copy = new Node(this);
    
		copy.boxMap = new HashMap<Coordinate,Box>(this.boxMap);

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
			result = prime * result + this.boxMap.hashCode();
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
		if (!this.boxMap.equals(other.boxMap)) return false;
		return true;
	}


	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (int row = 0; row < MAX_ROW; row++) {
			if (!WALLS.contains(new Coordinate(row, 0))) {
					break;
			}
			for (int col = 0; col < MAX_COL; col++) {
				Coordinate coordinate = new Coordinate(row, col);
				if (this.boxMap.get(coordinate) != null) {
					s.append(this.boxMap.get(coordinate).getCharacter());
				} else if (GOALS.get(coordinate) != null) {
					s.append(GOALS.get(coordinate).getCharacter());
				} else if (WALLS.contains(coordinate)) {
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