package searchclient;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;


import searchclient.NotImplementedException;

public abstract class Heuristic implements Comparator<Node> {

	List<Integer> goalCol = new ArrayList<Integer>();
	List<Integer> goalRow = new ArrayList<Integer>();
	List<Character> goalChar = new ArrayList<Character>();

	List<Boolean> goalsFinished = new ArrayList<Boolean>();

	// Boxes column|row|char|finished
	List<String> boxes = new ArrayList<String>();

	public Heuristic(Node initialState) {
		// Here's a chance to pre-process the static parts of the level.
	
		for (int row = 1; row < Node.MAX_ROW - 1; row++) {
			for (int col = 1; col < Node.MAX_COL - 1; col++) {
				if (Node.goals[row][col] > 0) {
					goalCol.add(col);
					goalRow.add(row);
					goalChar.add(Node.goals[row][col]);
					goalsFinished.add(false);
				}
			}
		}

		for (int row = 1; row < Node.MAX_ROW - 1; row++) {
			for (int col = 1; col < Node.MAX_COL - 1; col++) {
				if (initialState.boxes[row][col] > 0) {
					String boxKey = Integer.toString(col) + "," + Integer.toString(row) + ","  + initialState.boxes[row][col] + ","  + "0";
					boxes.add(boxKey);
				}
			}
		}
		System.err.println(boxes);
		System.err.println(goalCol);
	}

	public int h(Node n) {
		// first check which goals are finished
		for (int i = 0; i < goalsFinished.size(); i++) {
			int col = goalCol.get(i);
			int row = goalRow.get(i);

			// check if a box is within the goal and if the box matches the goal type
			if (n.boxes[row][col] > 0
					&& Character.toLowerCase(n.boxes[row][col]) == goalChar.get(i)) {
				goalsFinished.set(i, true);
			} else {
				goalsFinished.set(i, false);
			}
			
			//System.err.println(goalsFinished);
		}

		// Iterate over the goals which are unfinished and find the shortest goal
		// and calculate the lenght (the heurastic function) to the goal
		int minLength = Integer.MAX_VALUE;
		Boolean onlyOnce = true;
		int counter = 0;
		// Check if box is touching agent and is not in a goal
		if(isBoxTouchingAgent(n)) {
			for (int i = 0; i < goalsFinished.size(); i++) {
				if (!goalsFinished.get(i)) {
					int width = Math.abs(n.agentCol - goalCol.get(i));
					int height = Math.abs(n.agentRow - goalRow.get(i));

					int length = width + height;
					if (length < minLength) {
						minLength = length;
					}
				}
			}
			onlyOnce = true;
		}
		else {
			counter++;
			if(onlyOnce) {
				onlyOnce = false;
				
				for (int row = 1; row < Node.MAX_ROW - 1; row++) {
					for (int col = 1; col < Node.MAX_COL - 1; col++) {
						if (n.boxes[row][col] > 0) {
							String finished = "0";
		
							// Check which boxes are finished
						
							for (int i = 0; i < goalsFinished.size(); i++) {
								if (n.boxes[row][col] > 0 && n.boxes[row][col] == goalChar.get(i)) {
									finished = "1";
								}
							}
							
							String boxKey = Integer.toString(col) + "," + Integer.toString(row) + "," + n.boxes[row][col] + "," + finished;
							boxes.add(boxKey);
						}
					}
				}
			}
			for (int i = 0; i < boxes.size(); i++) {
				String[] boxArray = boxes.get(i).split(",");

				if (Integer.parseInt(boxArray[3]) != 1) {
					int width = Math.abs(n.agentCol - Integer.parseInt(boxArray[0]));
					int height = Math.abs(n.agentRow - Integer.parseInt(boxArray[1]));

					int length = width + height;
					if (length < minLength) {
						minLength = length * 100;
					}
				}
			}
		}

		return minLength;
	}

	public boolean isBoxTouchingAgent(Node n) {
		boolean isBoxLeftOfAgent = n.boxes[n.agentCol - 1][n.agentRow] > 0 && !(Node.goals[n.agentCol - 1][n.agentRow] > 0);
		boolean isBoxRightOfAgent = n.boxes[n.agentCol + 1][n.agentRow] > 0 && !(Node.goals[n.agentCol + 1][n.agentRow] > 0);
		boolean isBoxTopOfAgent = n.boxes[n.agentCol][n.agentRow - 1] > 0 && !(Node.goals[n.agentCol][n.agentRow - 1] > 0);
		boolean isBoxBottomOfAgent = n.boxes[n.agentCol][n.agentRow + 1] > 0 && !(Node.goals[n.agentCol][n.agentRow + 1] > 0);
		
		return isBoxLeftOfAgent || isBoxRightOfAgent || isBoxTopOfAgent || isBoxBottomOfAgent;
	}
	

	public abstract int f(Node n);

	@Override
	public int compare(Node n1, Node n2) {
		return this.f(n1) - this.f(n2);
	}

	public static class AStar extends Heuristic {
		public AStar(Node initialState) {
			super(initialState);
		}

		@Override
		public int f(Node n) {
			return n.g() + this.h(n);
		}

		@Override
		public String toString() {
			return "A* evaluation";
		}
	}

	public static class WeightedAStar extends Heuristic {
		private int W;

		public WeightedAStar(Node initialState, int W) {
			super(initialState);
			this.W = W;
		}

		@Override
		public int f(Node n) {
			return n.g() + this.W * this.h(n);
		}

		@Override
		public String toString() {
			return String.format("WA*(%d) evaluation", this.W);
		}
	}

	public static class Greedy extends Heuristic {
		public Greedy(Node initialState) {
			super(initialState);
		}

		@Override
		public int f(Node n) {
			return this.h(n);
		}

		@Override
		public String toString() {
			return "Greedy evaluation";
		}
	}
}
