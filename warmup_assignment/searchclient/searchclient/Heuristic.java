package searchclient;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import searchclient.Command.Type;
import java.util.HashMap;
import java.util.Map;
import searchclient.Goals.*;
import searchclient.NotImplementedException;

public abstract class Heuristic implements Comparator<Node> {
	int goalSize;

	public Heuristic(Node initialState) {
		// Here's a chance to pre-process the static parts of the level.
		goalSize = Node.goals.size();
	}

	public int h(Node n) {
	
		int goalsFinished = 0;

		int minLength = 0;

		if (n.action.actionType == Type.Move) {
	
			for (String key : n.boxMap.keySet()) {
				String[] boxArray = key.split(",");
				int row = Integer.parseInt(boxArray[0]);
				int col = Integer.parseInt(boxArray[1]);
				Goals currentGoal = Node.goals.get(row + "," + col);

				if (currentGoal == null) {
					int width = Math.abs(n.agentCol - row);
					int height = Math.abs(n.agentRow - col);

					int length = width + height;
				
					minLength += length * 100;
			
				}
				else if (currentGoal != null
					&& Character.toLowerCase(n.boxMap.get(row + "," + col)) == currentGoal.getCharacter()) {
					goalsFinished++;
				} 
			}
		}
		else if (n.action.actionType == Type.Push || n.action.actionType == Type.Pull) {
	
			for (Map.Entry<String, Goals> entry : Node.goals.entrySet()) {
				String key = entry.getKey();
				Goals currentGoal = entry.getValue();

				String[] goalArray = key.split(",");
				int row = Integer.parseInt(goalArray[0]);
				int col = Integer.parseInt(goalArray[1]);
				Character currentBox = n.boxMap.get(row + "," + col);

				if (currentBox != null
					&& Character.toLowerCase(currentBox) == currentGoal.getCharacter()) {
						currentGoal.setState(true);
					goalsFinished++;
				} else {
					currentGoal.setState(false);					
				}	

				if (!currentGoal.getState()) {
					int width = Math.abs(n.agentCol - col);
					int height = Math.abs(n.agentRow - row);

					int length = width + height;

					//minLength += length;
					Character currentBoxPlace = n.boxMap.get(n.newBox);
					if (Character.toLowerCase(currentBoxPlace) == currentGoal.getCharacter()) {
						minLength += length * 10 ;
					}	
					else {
						minLength += length;
					}
					
				}
			}

		}
	
		return (goalSize - goalsFinished)*100000 + minLength;
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
