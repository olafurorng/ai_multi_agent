package searchclient;

import java.util.Comparator;
import searchclient.Command.Type;

import java.util.Map;

public abstract class Heuristic implements Comparator<Node> {

	public Heuristic(Node initialState) {
		// Here's a chance to pre-process the static parts of the level.
		int counter = 1;

		// Assigns goalsto a boxes
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
		// Index zero as it is for a single agent
		return hPerAgent(n, 0); 
	}

	public int hPerAgent(Node n, int agentIndex) {

		int noMove = 0;
		int goalsLeft = 0;
		int notRightAssigned = 0;
		int assignedDistance = 0;
		int minLength = Integer.MAX_VALUE;
		//int priority = 0;
		//int closestBoxPriority = 0;

		if (n.actions[agentIndex].actionType == Type.NoOp) {	

			for (Map.Entry<Coordinate, Goals> entry : Node.GOALS.entrySet()) {
				int goalRow = entry.getKey().getX();
				int goalCol = entry.getKey().getY();
				Goals currentGoal = entry.getValue();
				Box currentBox = n.boxMap.get(new Coordinate(goalRow, goalCol));

				if (currentGoal.getColor() == Node.agentsColor[agentIndex]) {
					if ((currentBox == null) || (currentBox != null && Character.toLowerCase(currentBox.getCharacter()) != currentGoal.getCharacter())) {
						noMove = 10000000;
					}
				}
			}
		}
		else if ((n.actions[agentIndex].actionType == Type.Move) 
		|| ((n.actions[agentIndex].actionType == Type.Push || n.actions[agentIndex].actionType == Type.Pull) 
		&& n.boxMap.get(n.newBox.get(Integer.toString(agentIndex))).getAssign() == 0)) {
	
			/*int assignedGoalPriority = 0;
			for (Map.Entry<Coordinate, Goals> entry : Node.GOALS.entrySet()) {
				Goals currentGoal = entry.getValue();

				if (!currentGoal.getState()) {
					currentGoal.setFinished(currentGoal.getFinished() + 1);
					//priority += currentGoal.getPriority() * 100000;				
				} 
				if (currentGoal.getPriority() == 100) {
					assignedGoalPriority = currentGoal.getAssign();
				}
			}*/

			for (Coordinate coordinate : n.boxMap.keySet()) {
				int row = coordinate.getX();
				int col = coordinate.getY();
				Goals currentGoal = Node.GOALS.get(coordinate);
				Box currentBox = n.boxMap.get(new Coordinate(row, col));

				// Find closest box that is not in the right goal
				int length = 0;
				if ((currentGoal == null && currentBox.getAssign() != 0) || (currentGoal != null && Character.toLowerCase(currentBox.getCharacter()) != currentGoal.getCharacter() &&
					currentBox.getColor() == Node.agentsColor[agentIndex] && currentBox.getAssign() != 0)) {
					int width = Math.abs(n.agentsCol[agentIndex] - col);
					int height = Math.abs(n.agentsRow[agentIndex] - row);

					length = width + height;

					if (length < minLength ) {
						minLength = length;
					}

					goalsLeft++;
				}

				// How many boxes are not in the right assigned goal
				if ((currentGoal == null) || (currentGoal != null && currentBox.getAssign() != currentGoal.getAssign())) {
					notRightAssigned++;
				}

				/*if (currentBox.getAssign() == assignedGoalPriority ) {
					minLength = length;	
					goalsLeft = 1;
					notRightAssigned = 0;
					break;
				}*/
			}
			
			// So moving is almost always worse then pushing and moving a box not in the right goal
			minLength += 75;
		}
		else if (n.actions[agentIndex].actionType == Type.Push || n.actions[agentIndex].actionType == Type.Pull) {

			Box currentBoxMoving = n.boxMap.get(n.newBox.get(Integer.toString(agentIndex)));

			for (Map.Entry<Coordinate, Goals> entry : Node.GOALS.entrySet()) {
				int goalRow = entry.getKey().getX();
				int goalCol = entry.getKey().getY();
				Goals currentGoal = entry.getValue();
				Box currentBox = n.boxMap.get(new Coordinate(goalRow, goalCol));

				// Counts and sets finished states on goals
				if (currentBox != null && Character.toLowerCase(currentBox.getCharacter()) == currentGoal.getCharacter()) {
					currentGoal.setState(true);
				} else {
					currentGoal.setState(false);	
					goalsLeft++;	
					//currentGoal.setFinished(currentGoal.getFinished() + 1);		
				}	

				// Minimum length to closest goal with the same character
				int length = 0;
				if (!currentGoal.getState() && Character.toLowerCase(currentBoxMoving.getCharacter()) == currentGoal.getCharacter()) {
					int width = Math.abs(n.agentsCol[agentIndex] - goalCol);
					int height = Math.abs(n.agentsRow[agentIndex] - goalRow);

					length = width + height;

					if (length < minLength) {
						minLength = length;
					}					
				}

				// How many boxes are not in the right assigned goal
				if ((currentBox == null) || (currentBox != null && currentBox.getAssign() != currentGoal.getAssign())) {
					notRightAssigned++;
				}

				// Better to move to the assigned goal
				if (currentBoxMoving.getAssign() == currentGoal.getAssign()) {
					int movingBoxRow = n.newBox.get(Integer.toString(agentIndex)).getX();
					int movingBoxCol = n.newBox.get(Integer.toString(agentIndex)).getY();

					int width = Math.abs(movingBoxCol - goalCol);
					int height = Math.abs(movingBoxRow - goalRow);

					assignedDistance = width + height;
				}

				/*if (currentGoal.getPriority() == 100) {
					minLength = length;	
					goalsLeft = 1;
					notRightAssigned = 0;
					assignedDistance = 0;
					
					if (currentBox != null && Character.toLowerCase(currentBox.getCharacter()) == currentGoal.getCharacter()) {
						currentGoal.setPriority(1);
						goalsLeft = 0;
						System.err.println("Goal c happens");
					}
					break;
				} */
			}
		}

		// If minlength is still integer max value it is the final stage
		if (minLength == Integer.MAX_VALUE) {
			minLength = 0;
		}

		int heuristicValue = goalsLeft*100 + (notRightAssigned*50 + assignedDistance) + minLength + noMove;

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
