package searchclient;

import java.util.*;

import searchclient.ColorHelper.*;
import searchclient.Command.Type;

public class Node {

	private static final ExpendedNodesHelper EXPENDED_NODES_HELPER = new ExpendedNodesHelper();

	/**
	 * This method should be called once, in the beginning when we have loaded
	 * in the level and we know the number of columns and rows of the level
	 */
	public static void setLevelSize(final int numberOfRows, final int numberOfCol) {
		MAX_ROW = numberOfRows;
		MAX_COL = numberOfCol;


		// initilaizing static walls and goals with the level size
		walls = new boolean[MAX_ROW][MAX_COL];
		goals = new char[MAX_ROW][MAX_COL];
	}

	public static void setNumberOfAgents(final int numberOfAgents) {
		NUMBER_OF_AGENTS = numberOfAgents;
	}

	public static int MAX_ROW;
	public static int MAX_COL;
	public static int NUMBER_OF_AGENTS;

	public int[] agentsRow = new int[NUMBER_OF_AGENTS];
	public int[] agentsCol = new int[NUMBER_OF_AGENTS];
	public static Color[] agentsColor;
	//public int agentRow;
	//public int agentCol;
	//public static Color agentColor; // NOTE: nullable for SA levels

	// Arrays are indexed from the top-left of the level, with first index being row and second being column.
	// Row 0: (0,0) (0,1) (0,2) (0,3) ...
	// Row 1: (1,0) (1,1) (1,2) (1,3) ...
	// Row 2: (2,0) (2,1) (2,2) (2,3) ...
	// ...
	// (Start in the top left corner, first go down, then go right)
	// E.g. this.walls[2] is an array of booleans having size MAX_COL.
	// this.walls[row][col] is true if there's a wall at (row, col)
	//

	public static boolean[][] walls;
	public char[][] boxes = new char[MAX_ROW][MAX_COL];
	public static char[][] goals;

	public Node parent;
	public Command[] actions = new Command[NUMBER_OF_AGENTS];

	private int g;
	
	private int _hash = 0;

	public Node(Node parent) {
		Command noOpAction = new Command();
		Arrays.fill(actions, noOpAction);

		this.parent = parent;
		if (parent == null) {
			this.g = 0;
		} else {
			this.g = parent.g() + 1;
		}
	}

	public void initializeActionsForInitialState() {
		actions = new Command[NUMBER_OF_AGENTS];
		Command noOpAction = new Command();
		Arrays.fill(actions, noOpAction);
	}

	public int g() {
		return this.g;
	}

	public boolean isInitialState() {
		return this.parent == null;
	}

	public boolean isGoalState() {
		for (int row = 1; row < MAX_ROW - 1; row++) {
			for (int col = 1; col < MAX_COL - 1; col++) {
				char g = goals[row][col];
				char b = Character.toLowerCase(boxes[row][col]);
				if (g > 0 && b != g) {
					return false;
				}
			}
		}
		return true;
	}


	public ArrayList<Node> getExpandedNodes() {
		System.err.println("===============================");
		System.err.println("We are expanding node: " + this);
		System.err.println("-------------------------------");
		ArrayList<Node> allExpandedNodes = new ArrayList<Node>();
		ArrayList<Node> expandedNodesFromLastAgent = new ArrayList<Node>();
		expandedNodesFromLastAgent.add(this);
		for (int i = 0; i < NUMBER_OF_AGENTS; i++) {
			System.err.println("Expanded nodes for agent: " + i);
			ArrayList<Node> expandedNodesFromThisAgent = new ArrayList<Node>();

			for (Node n : expandedNodesFromLastAgent) {
				expandedNodesFromThisAgent.addAll(EXPENDED_NODES_HELPER.getExpandedNodes(n, i));
				if (NUMBER_OF_AGENTS >= 2) {
					// its multi agent
					expandedNodesFromThisAgent.add(n); // this is equal to no-op action for this agent

				}
			}
			for (Node n : expandedNodesFromThisAgent) {
				System.err.println(n);
			}



			allExpandedNodes.addAll(expandedNodesFromThisAgent);
			expandedNodesFromLastAgent = expandedNodesFromThisAgent;
		}

		//for (Node n : allExpandedNodes) {
		//	System.err.println(n);
		//}

		System.err.println("===============================");
		return allExpandedNodes;
	}

    public boolean cellIsFree(int row, int col) {
		for (int i = 0; i < NUMBER_OF_AGENTS; i++) {
			if (agentsCol[i] == col && agentsRow[i] == row) {
				return false;
			}
		}
		return !walls[row][col] && this.boxes[row][col] == 0;
	}

	/**
	 * Used in beginning when we fill the level with walls
	 */
    public boolean cellIsFreeAndNoGoalOrAgent(int row, int col) {
		for (int i = 0; i < NUMBER_OF_AGENTS; i++) {
			if (agentsCol[i] == col && agentsRow[i] == row) {
				return false;
			}
		}
        return !walls[row][col] && this.boxes[row][col] == 0 && goals[row][col] == 0;
    }

	/**
	 * Used in beginning when we fill the level with walls
	 */
	public boolean cellIsFreeOfGoalBoxAndAgent(int row, int col) {
		for (int i = 0; i < NUMBER_OF_AGENTS; i++) {
			if (agentsCol[i] == col && agentsRow[i] == row) {
				return false;
			}
		}
		return this.boxes[row][col] == 0 && goals[row][col] == 0;
	}

	public boolean boxAt(int row, int col) {
		return this.boxes[row][col] > 0;
	}

	public Node ChildNode(boolean copyActions) {
		Node copy = new Node(this);
		for (int row = 0; row < MAX_ROW; row++) {
			System.arraycopy(this.boxes[row], 0, copy.boxes[row], 0, MAX_COL);
		}
		System.arraycopy(this.agentsRow, 0, copy.agentsRow, 0, NUMBER_OF_AGENTS);
		System.arraycopy(this.agentsCol, 0, copy.agentsCol, 0, NUMBER_OF_AGENTS);
		if (copyActions) {
			try {
				System.arraycopy(this.actions, 0, copy.actions, 0, NUMBER_OF_AGENTS);
			}
			catch (ArrayIndexOutOfBoundsException e) {
				System.err.println("Out of bounds node is: " + this);
				System.err.println("... and actions are: " + actions.length);
				System.err.println("... and copy actions are: " + copy.actions.length);
				throw new ArrayIndexOutOfBoundsException(e.getMessage());
			}

		}
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
			result = prime * result + Arrays.hashCode(this.agentsRow);
			result = prime * result + Arrays.hashCode(this.agentsCol);
			result = prime * result + Arrays.deepHashCode(this.boxes);
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
		if (!(Arrays.equals(this.agentsRow, other.agentsRow) && Arrays.equals(this.agentsCol, other.agentsCol)))
			return false;
		if (!Arrays.deepEquals(this.boxes, other.boxes))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (int row = 0; row < MAX_ROW; row++) {
			if (!walls[row][0]) {
				break;
			}
			for (int col = 0; col < MAX_COL; col++) {
				if (boxes[row][col] > 0) {
					s.append(this.boxes[row][col]);
				} else if (goals[row][col] > 0) {
					s.append(goals[row][col]);
				} else if (walls[row][col]) {
					s.append("+");
				} else {
					boolean agentThere = false;
					for (int agentIndex = 0; agentIndex < NUMBER_OF_AGENTS; agentIndex++){
						if (agentsRow[agentIndex] == row && agentsCol[agentIndex] == col) {
							s.append(String.valueOf(agentIndex));
							agentThere = true;
							break;
						}
					}

					if (!agentThere) {
						s.append(" ");
					}
				}
			}
			s.append("\n");
		}
		return s.toString();
	}

}