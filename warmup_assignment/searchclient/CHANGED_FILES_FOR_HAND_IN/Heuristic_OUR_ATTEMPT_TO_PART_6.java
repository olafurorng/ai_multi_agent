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
	}

	public int h(Node n) {

		List<Integer> boxCol = new ArrayList<Integer>();
		List<Integer> boxRow = new ArrayList<Integer>();
		List<Character> boxChar = new ArrayList<Character>();
		List<Boolean> boxFinished = new ArrayList<Boolean>();
	
		for (int row = 1; row < Node.MAX_ROW - 1; row++) {
			for (int col = 1; col < Node.MAX_COL - 1; col++) {
				if (n.boxes[row][col] > 0) {
					boxCol.add(col);
					boxRow.add(row);
					boxChar.add(n.boxes[row][col]);
					boxFinished.add(false);
				}
			}
		}

		// Check which goals are finished
		for (int i = 0; i < goalsFinished.size(); i++) {
			int col = goalCol.get(i);
			int row = goalRow.get(i);

			if (n.boxes[row][col] > 0 && n.boxes[row][col] == goalChar.get(i)) {
				goalsFinished.set(i, true);
			} else {
				goalsFinished.set(i, false);
			}
		}
		// Check which boxes are finished
		for (int i = 0; i < boxFinished.size(); i++) {
			int col = boxCol.get(i);
			int row = boxRow.get(i);

			if (n.boxes[row][col] > 0 && n.boxes[row][col] == goalChar.get(i)) {
				boxFinished.set(i, true);
			} else {
				boxFinished.set(i, false);
			}
		}

		// Iterate over the goals which are unfinished and find the shortest goal
		// and calculate the lenght (the heurastic function) to the goal
		int minLength = Integer.MAX_VALUE;

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
		}
		else {		
			for (int i = 0; i < boxFinished.size(); i++) {
				if (!boxFinished.get(i)) {
					int width = Math.abs(n.agentCol - boxCol.get(i));
					int height = Math.abs(n.agentRow - boxRow.get(i));

					int length = width + height;
					if (length < minLength) {
						minLength = length;
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
		boolean isBoxBottomOfAgent = n.boxes[n.agentCol][n.agentRow + 1] > 0 && !(Node.goals[n.agentCol][n.agentRow - 0] > 0);
		
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
