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
		int countGoalsNotFinished = 0;

		// first check wich goals are finished
		for (int i = 0; i < goalsFinished.size(); i++) {
			int col = goalCol.get(i);
			int row = goalRow.get(i);

			// check if a box is within the goal and if the box matches the goal type
			if (n.boxes[row][col] > 0
					&& Character.toLowerCase(n.boxes[row][col]) == goalChar.get(i)) {
				goalsFinished.set(i, true);
			} else {
				goalsFinished.set(i, false);
				countGoalsNotFinished++;
			}
		}

		// iterate over the goals which are unfinished and find the shortest goal
		// and calculate the length (the heurastic function) to the goal
//		int minLength = Integer.MAX_VALUE;
//		for (int i = 0; i < goalsFinished.size(); i++) {
//			if (!goalsFinished.get(i)) {
//				int width = Math.abs(n.agentCol - goalCol.get(i));
//				int height = Math.abs(n.agentRow - goalRow.get(i));
//
//				int length = width + height;
//				if (length < minLength) {
//					minLength = length;
//				}
//			}
//		}
		return countGoalsNotFinished; //*100 + minLength;
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
