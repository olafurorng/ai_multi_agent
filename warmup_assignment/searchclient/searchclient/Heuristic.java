package searchclient;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import searchclient.Command.Type;
import java.util.HashMap;
import java.util.Map;
import searchclient.Goals.*;
import searchclient.Box.*;
import searchclient.NotImplementedException;

public abstract class Heuristic implements Comparator<Node> {
	int goalSize;

	public Heuristic(Node initialState) {
		// Here's a chance to pre-process the static parts of the level.
		goalSize = Node.goals.size();

		int counter = 1;

		for (Map.Entry<String, Goals> entry : Node.goals.entrySet()) {
			int minLength = Integer.MAX_VALUE;
			Box minBox = null;

			String goalKey = entry.getKey();
			Goals currentGoal = entry.getValue();
			String[] goalArray = goalKey.split(",");
			int goalRow = Integer.parseInt(goalArray[0]);
			int goalCol = Integer.parseInt(goalArray[1]);

			for (String boxKey : initialState.boxMap.keySet()) {
				String[] boxArray = boxKey.split(",");
				int boxRow = Integer.parseInt(boxArray[0]);
				int boxCol = Integer.parseInt(boxArray[1]);
				Box currentBox = initialState.boxMap.get(boxRow + "," + boxCol);
		
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

			currentGoal.setPriority(counter);
			minBox.setPriority(counter);
			counter++;
		}
	}

	public int h(Node n) {
	
		int goalsFinished = 0;
		int notRightPriorities = 0;
		int priorityDistance = 0;
		int closestBox = Integer.MAX_VALUE;
		int minLength = Integer.MAX_VALUE;

		if (n.action.actionType == Type.Move) {
	
			for (String key : n.boxMap.keySet()) {
				String[] boxArray = key.split(",");
				int row = Integer.parseInt(boxArray[0]);
				int col = Integer.parseInt(boxArray[1]);
				Goals currentGoal = Node.goals.get(row + "," + col);

				// Find closest box
				if (currentGoal == null) {
					int width = Math.abs(n.agentCol - col);
					int height = Math.abs(n.agentRow - row);

					int length = width + height;
				
					if (length < closestBox ) {
						closestBox = length;	
					}	
				}

				// Finished goals
				else if (currentGoal != null && Character.toLowerCase(n.boxMap.get(row + "," + col).getCharacter()) == currentGoal.getCharacter()) {
					goalsFinished++;
				} 
				
				// Priority
				if (currentGoal != null && n.boxMap.get(row + "," + col).getPriority() != currentGoal.getPriority()) {
					notRightPriorities++;
				}
			}
		}
		else if (n.action.actionType == Type.Push || n.action.actionType == Type.Pull) {
	
			for (Map.Entry<String, Goals> entry : Node.goals.entrySet()) {
				String key = entry.getKey();
				Goals currentGoal = entry.getValue();

				String[] goalArray = key.split(",");
				int goalRow = Integer.parseInt(goalArray[0]);
				int goalCol = Integer.parseInt(goalArray[1]);
				Box currentBox = n.boxMap.get(goalRow + "," + goalCol);

				// Counts and sets finished state on goals
				if (currentBox != null && Character.toLowerCase(currentBox.getCharacter()) == currentGoal.getCharacter()) {
					currentGoal.setState(true);
					goalsFinished++;
				} else {
					currentGoal.setState(false);					
				}	

				Box currentBoxMoving = n.boxMap.get(n.newBox);

				if (!currentGoal.getState()) {
					int width = Math.abs(n.agentCol - goalCol);
					int height = Math.abs(n.agentRow - goalRow);

					int length = width + height;

					// Better to move to the right goal character
					if (Character.toLowerCase(currentBoxMoving.getCharacter()) == currentGoal.getCharacter()) {
						if (length < minLength) {
							minLength = length;
						}
					}	
					else {
						if ((length * 10) < minLength) {
							minLength = length * 10;
						}
					}						
				}

				// Right box in right goal priority
				if (currentBox != null && currentBox.getPriority() != currentGoal.getPriority()) {
					notRightPriorities++;
				}

				// Priority to move to the right goal
				if (currentBoxMoving.getPriority() == currentGoal.getPriority()) {
					String[] movingBoxArray = n.newBox.split(",");
					int movingBoxRow = Integer.parseInt(movingBoxArray[0]);
					int movingBoxCol = Integer.parseInt(movingBoxArray[1]);

					int width2 = Math.abs(movingBoxCol - goalCol);
					int height2 = Math.abs(movingBoxRow - goalRow);

					priorityDistance += (width2 + height2) * 10;
				}
			}

		}
	
		if (closestBox == Integer.MAX_VALUE) {
			closestBox = 0;
		}
		if (minLength == Integer.MAX_VALUE) {
			minLength = 0;
		}

		return (goalSize - goalsFinished)*100000 + notRightPriorities*10000 + minLength * 100 + priorityDistance + closestBox * 1000;
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
