package searchclient;

import java.util.Comparator;
import searchclient.Command.Type;

import java.util.Map;

public abstract class Heuristic implements Comparator<Node> {

	public Heuristic(Node initialState) {
		// Here's a chance to pre-process the static parts of the level.
		int counter = 1;

		for (Map.Entry<Coordinate, Goals> entry : Node.GOALS.entrySet()) {
			int minLength = Integer.MAX_VALUE;
			Box minBox = null;

			Goals currentGoal = entry.getValue();
			int goalRow = entry.getKey().getX();
			int goalCol = entry.getKey().getY();

			for (Coordinate boxKey : initialState.boxMap.keySet()) {
				int boxRow = boxKey.getX();
				int boxCol = boxKey.getY();
				Box currentBox = initialState.boxMap.get(new Coordinate(boxRow, boxCol));
		
				if (Character.toLowerCase(currentBox.getCharacter()) == currentGoal.getCharacter()) {
					int width = Math.abs(goalCol - boxCol);
					int height = Math.abs(goalRow - boxRow);
	
					int length = width + height;
	
					if (length < minLength)  {
						minLength = length;
						minBox = currentBox;
					}	
				}			
			}
			currentGoal.setAssign(counter);
			minBox.setAssign(counter);
			counter++;
		}
	}

	public int h(Node n) {
		return hPerAgent(n, 0); // index zero as it is for single agent
	}

	public int hPerAgent(Node n, int agentIndex) {

		int goalsLeft = 0;
		int notRightAssigned = 0;
		int assignedDistance = 0;
		int closestBox = Integer.MAX_VALUE;
		int minLength = Integer.MAX_VALUE;

		if (n.actions[agentIndex].actionType == Type.Move) {

			for (Coordinate coordinate : n.boxMap.keySet()) {
				int row = coordinate.getX();
				int col = coordinate.getY();
				Goals currentGoal = Node.GOALS.get(coordinate);

				// Find closest box that is not in the right goal
				if ((currentGoal == null) || (currentGoal != null && Character.toLowerCase(n.boxMap.get(new Coordinate(row, col)).getCharacter()) != currentGoal.getCharacter())) {
					int width = Math.abs(n.agentsCol[agentIndex] - col);
					int height = Math.abs(n.agentsRow[agentIndex] - row);

					int length = width + height;

					if (length < closestBox ) {
						closestBox = length;
					}

					goalsLeft++;
				}

				// Priority
				if ((currentGoal == null) || (currentGoal != null && n.boxMap.get(new Coordinate(row, col)).getAssign() != currentGoal.getAssign())) {
					notRightAssigned++;
				}
			}
		}
		else if (n.actions[agentIndex].actionType == Type.Push || n.actions[agentIndex].actionType == Type.Pull) {

			for (Map.Entry<Coordinate, Goals> entry : Node.GOALS.entrySet()) {
				Goals currentGoal = entry.getValue();

				int goalRow = entry.getKey().getX();
				int goalCol = entry.getKey().getY();
				Box currentBox = n.boxMap.get(new Coordinate(goalRow, goalCol));

				// Counts and sets finished state on goals
				if (currentBox != null && Character.toLowerCase(currentBox.getCharacter()) == currentGoal.getCharacter()) {
					currentGoal.setState(true);
				} else {
					currentGoal.setState(false);
					goalsLeft++;
				}

				Box currentBoxMoving = n.boxMap.get(n.newBox.get(Integer.toString(agentIndex)));
/*
				if (!currentGoal.getState()) {
					int width = Math.abs(n.agentsCol[agentIndex] - goalCol);
					int height = Math.abs(n.agentsRow[agentIndex] - goalRow);

					int length = width + height;

					// Better to move to the right goal character
					if (Character.toLowerCase(currentBoxMoving.getCharacter()) == currentGoal.getCharacter()) {
						if (length < minLength) {
							minLength = length;
						}
					}
				}*/

				// Right box in right goal priority
				if ((currentBox == null) || (currentBox != null && currentBox.getAssign() != currentGoal.getAssign())) {
					notRightAssigned++;
				}

				// Priority to move to the right goal

				if (currentBoxMoving.getAssign() == currentGoal.getAssign()) {
					int movingBoxRow = n.newBox.get(Integer.toString(agentIndex)).getX();
					int movingBoxCol = n.newBox.get(Integer.toString(agentIndex)).getY();

					int width2 = Math.abs(movingBoxCol - goalCol);
					int height2 = Math.abs(movingBoxRow - goalRow);

					assignedDistance = (width2 + height2) * 10;
				}
			}

		}

		if (closestBox == Integer.MAX_VALUE) {
			closestBox = 0;
		}
		if (minLength == Integer.MAX_VALUE) {
			minLength = 0;
		}

		int heuristicValue = goalsLeft*100000 + notRightAssigned*10000 + minLength * 100 + assignedDistance + closestBox * 1000;
		//System.err.println("heuristic: " + heuristicValue);
		return heuristicValue;
	}

	public abstract int f(Node n);

	@Override
	public int compare(Node n1, Node n2) {
		return this.f(n1) - this.f(n2);
	}

	public static class AStarSA extends Heuristic {
		public AStarSA(Node initialState) {
			super(initialState);
		}



		@Override
		public int f(Node n) {
			return n.g() + this.h(n);
		}

		@Override
		public String toString() {
			return "A* evaluation ofr Single Agent";
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
