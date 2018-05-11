package searchclient;

import java.util.*;

import searchclient.ColorHelper.*;
import searchclient.Command.Type;

public class Node {

	private static final Random RND = new Random(1);
	private static final ExpendedNodesHelper EXPENDED_NODES_HELPER = new ExpendedNodesHelper();


	/**
	 * This method should be called once, in the beginning when we have loaded
	 * in the level and we know the number of columns and rows of the level
	 */
	public static void setLevelSize(final int numberOfRows, final int numberOfCol) {
		MAX_ROW = numberOfRows;
		MAX_COL = numberOfCol;
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


	public static List<Coordinate> WALLS = new ArrayList<Coordinate>();
	public Map<Coordinate, Box> boxMap = new HashMap<Coordinate, Box>();
	public static HashMap<Coordinate, Goals> GOALS = new HashMap<Coordinate, Goals>();

	public Map<String, Coordinate> newBox = new HashMap<String, Coordinate>();

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
		ArrayList<Node> allExpendedNodes = new ArrayList<Node>();
		allExpendedNodes.addAll(EXPENDED_NODES_HELPER.getExpandedNodes(this, this, 0));

		for (int i = 1; i < NUMBER_OF_AGENTS; i++) { // we start from one as we have already expanded nodes for the first agent
			if (NUMBER_OF_AGENTS >= i+1) {
				ArrayList<Node> tempNewNodes = new ArrayList<Node>();
				for (Node n : allExpendedNodes) {
					tempNewNodes.addAll(EXPENDED_NODES_HELPER.getExpandedNodes(this, n, i));
				}
				allExpendedNodes.addAll(tempNewNodes);
				allExpendedNodes.addAll(EXPENDED_NODES_HELPER.getExpandedNodes(this, this, i));
			}
		}

		Collections.shuffle(allExpendedNodes, RND);
		return allExpendedNodes;
	}

	public boolean cellIsFree(int row, int col) {
		for (int i = 0; i < NUMBER_OF_AGENTS; i++) {
			if (agentsCol[i] == col && agentsRow[i] == row) {
				return false;
			}
		}

		Coordinate coordinate = new Coordinate(row, col);
		return !WALLS.contains(coordinate) && this.boxMap.get(coordinate) == null;
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

		Coordinate coordinate = new Coordinate(row, col);
        return  !WALLS.contains(coordinate) && this.boxMap.get(coordinate) == null && GOALS.get(coordinate) == null;

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

		Coordinate coordinate = new Coordinate(row, col);
		return this.boxMap.get(coordinate) == null && GOALS.get(coordinate) == null;
	}

	public boolean boxAt(int row, int col) {
		return this.boxMap.get(new Coordinate(row, col)) != null;
	}

	public Node ChildNode(Node parentNode, Node nodeBefore) {
		Node copy = new Node(parentNode);
    
		copy.boxMap = new HashMap<Coordinate,Box>(this.boxMap);

		System.arraycopy(this.agentsRow, 0, copy.agentsRow, 0, NUMBER_OF_AGENTS);
		System.arraycopy(this.agentsCol, 0, copy.agentsCol, 0, NUMBER_OF_AGENTS);
		if (nodeBefore != parentNode) {
			System.arraycopy(this.actions, 0, copy.actions, 0, NUMBER_OF_AGENTS);
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
		if (!(Arrays.equals(this.agentsRow, other.agentsRow) && Arrays.equals(this.agentsCol, other.agentsCol)))
			return false;
		if (!this.boxMap.equals(other.boxMap)) return false;
		return true;
	}


	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("\n");
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
		s.append("ID: " + System.identityHashCode(this));
		return s.toString();
	}

}