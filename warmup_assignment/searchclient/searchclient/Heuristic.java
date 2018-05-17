package searchclient;

import java.io.IOException;
import java.util.*;

import searchclient.Command.Type;

public abstract class Heuristic implements Comparator<Node> {

	public static AgentCommunications agentCommunications;

	public Heuristic(Node initialState) {
		agentCommunications = new AgentCommunications();

		// Here's a chance to pre-process the static parts of the level.
		int counter = 1;

		// Assigns goals to a boxes
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

				if (currentBox.getAssign() == 0) {
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
		
			}

			currentGoal.setAssign(counter);
			minBox.setAssign(counter);	
			counter++;
		}

		/********************************************
		 ******  DETECTING GOAL DEPENDENCIES   ******
		 *******************************************/
        for (int i = 0; i < Node.MAX_ROW; i++) {
            for (int j = 0; j < Node.MAX_COL; j++) {
				Coordinate coordinate = new Coordinate(i, j);
                if (!Node.WALLS.contains(coordinate)) {

					// Is an end of a tunnel
					boolean goalIsInTunnel = false;
					boolean isInEndOfTunnel = TunnelDetection.isGoalInEndOfTunnel(coordinate);

					if (isInEndOfTunnel) {

						if (Node.GOALS.get(coordinate) != null) {
							goalIsInTunnel = true;
						}

						// Create the tunnel and add the first cell to the tunnel object
						Tunnel tunnel = new Tunnel();
						tunnel.addCellToTunnel(coordinate, Node.GOALS.get(coordinate));
						Coordinate lastCoordinate = null;
						Coordinate thisCoordinate = coordinate;

						boolean isStillInTunnel = true;
						while (isStillInTunnel) {
							// find the next coordinate in the tunnel
							Coordinate nextCoordinate = TunnelDetection.findNextCoordinateInTunnel(thisCoordinate, lastCoordinate);
							
							if (Node.GOALS.get(nextCoordinate) != null) {
								goalIsInTunnel = true;
							}

							/*NULLABLE*/
							Goals goalAtNextCoordinate = Node.GOALS.get(nextCoordinate);

							tunnel.addCellToTunnel(nextCoordinate, goalAtNextCoordinate);

							// detect if we are at the end of the tunnel or not
							try {
								isStillInTunnel = TunnelDetection.isInMiddleOfTunnel(nextCoordinate);
							}
							catch (IllegalStateException e){
								isStillInTunnel = false;
							}

							if (isStillInTunnel) {
								lastCoordinate = thisCoordinate;
								thisCoordinate = nextCoordinate;

							} else {
								tunnel.onTunnelEndDetected();
							}
						}

						if (goalIsInTunnel && tunnel.getTunnelLength() > 3) {
							Node.TUNNELS.add(tunnel);
						}
					}
                }
            }
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

		if (n.actions[agentIndex].actionType == Type.NoOp) {	
			
			// If there are no more goals for an agent he stops
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
			// If the agent has a signal he does not want to stay still
			if (agentCommunications.getTouchedBox() != null && agentCommunications.getTouchedBox().getColor() == Node.agentsColor[agentIndex]) {
				noMove = 10000000;
			}
			
		}
		else if ((n.actions[agentIndex].actionType == Type.Move) 
		|| ((n.actions[agentIndex].actionType == Type.Push || n.actions[agentIndex].actionType == Type.Pull) 
		&& (agentCommunications.getTouchedBox() == null || (agentCommunications.getTouchedBox() != null && agentCommunications.getTouchedBox().getColor() != Node.agentsColor[agentIndex])) 
		&& n.boxMap.get(n.newBox.get(Integer.toString(agentIndex))).getAssign() == 0 )) {

			for (Coordinate coordinate : n.boxMap.keySet()) {
				int row = coordinate.getX();
				int col = coordinate.getY();
				Goals currentGoal = Node.GOALS.get(coordinate);
				Box currentBox = n.boxMap.get(new Coordinate(row, col));

				// Find closest box that is not in the right goal
				int length = 0;
				if ((currentGoal == null && currentBox.getAssign() != 0 ) || (currentGoal != null && Character.toLowerCase(currentBox.getCharacter()) != currentGoal.getCharacter())) {
					goalsLeft++;
				}

				if (currentBox.getColor() == Node.agentsColor[agentIndex]) {
					if ((currentGoal == null) || (currentGoal != null && Character.toLowerCase(currentBox.getCharacter()) != currentGoal.getCharacter())) {


						// first we check if the the row is reachable by the agent
						// lets check if the box row and agent row is in the same horizontal section
						if (!(Node.minRowAgents[agentIndex] <= row && row <= Node.maxRowAgents[agentIndex])) {
							// box row is not reachable by the agent
							continue;
						}

						// we calculate the length to the box normally
						int width = Math.abs(n.agentsCol[agentIndex] - col);
						int height = Math.abs(n.agentsRow[agentIndex] - row);
						length = width + height;
								
						if (agentCommunications.getTouchedBox() != null && agentCommunications.getTouchedBox().getColor() == Node.agentsColor[agentIndex]) {
								// a box of other color has been touched and we put high priority on going to that box
								// as that box and agent has the same color instead of e.g. moving other boxes
							if (currentBox.getCharacter() == agentCommunications.getTouchedBox().getCharacter()) {
								length += -75;
							}
						} 

						if (length < minLength ) {
							minLength = length;
						}		

					}
				}

				if (agentCommunications.getTouchedBox() == null && currentBox.getAssign() == 0) {
					minLength = 150;
				}

				// How many boxes are not in the right assigned goal
				if ((currentGoal == null && currentBox.getAssign() != 0) || (currentGoal != null && currentBox.getAssign() != currentGoal.getAssign())) {
					notRightAssigned++;
				}
			}
			
			// So moving is almost always worse then pushing and moving a box not in the right goal
			if (minLength == Integer.MAX_VALUE) {
				minLength = 20;
			}
			else {			
				minLength += 75;
			}
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
				}	

				// Minimum length to closest goal with the same character
				int length = 0;
				
				// When there is signal for a agent, he wants to pull the box
				if (agentCommunications.getTouchedBox() != null && agentCommunications.getTouchedBox().getColor() == Node.agentsColor[agentIndex] &&
				currentBoxMoving.getCharacter() == agentCommunications.getTouchedBox().getCharacter()) {										
					minLength = 2;		

					// It is better to pull a signal
					if (n.actions[agentIndex].actionType == Type.Pull) {
						minLength = -5;
					}						
				}
				else if (!currentGoal.getState()) {
					// Finds length to next goal
					if (Character.toLowerCase(currentBoxMoving.getCharacter()) == currentGoal.getCharacter()) {		
						int width = Math.abs(n.agentsCol[agentIndex] - goalCol);
						int height = Math.abs(n.agentsRow[agentIndex] - goalRow);

						length = width + height;
				
						if (length < minLength) {
							minLength = length;
						}	
					}	
					if (currentBoxMoving.getAssign() == 0) {
						minLength = 500;
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
			}
		}

		// If minlength is still integer max value it is the final stage
		if (minLength == Integer.MAX_VALUE) {
			minLength = 0;
		}


		// CALCULATE TUNNEL PENALTY
		int tunnelPenalty = 0;
		int lengthToIllegalBoxInTunnel = 0;
		int tunnelPlus = 0;

		for (Tunnel tunnel : Node.TUNNELS) {
			for (Map.Entry<Coordinate, Integer> entry : tunnel.getTunnelCells().entrySet()) {
				Coordinate coordinate = entry.getKey();
				int positionOfBox = entry.getValue();
				Box boxAtAcoordinate = n.boxMap.get(coordinate);

				// if there is box at this coordinate we check if it is allowed
				if (boxAtAcoordinate != null) {
					// we loop through the goals in the tunnel from the deepest goal
					// 1. we check if the previous goal until this position, has been solved, if
					//    we detect an unsolved goal, we check if the box is assigned to that goal
					//    and if not we give penalty
					// 2. if there is no unsolved goal before this and the box is not assigned to
					//    the first unsolved goal after, the box gets penalty
					Boolean innermostEmptyGoal = true;

					for (Map.Entry<Goals, Integer> entryGoal : tunnel.getGoalsList().entrySet()) {
						Goals goal = entryGoal.getKey();
						int positionOfGoal = entryGoal.getValue();
						Coordinate goalCoordinates = tunnel.getGoalsCoordinates().get(positionOfGoal);

						//System.err.println(goal.getCharacter());
						boolean isBoxInGoal = n.boxMap.containsKey(goalCoordinates);

						if (isBoxInGoal) {
							// now we know there is box in the goal, but lets check if it is the right box
							boolean goalIsSolved = goal.getCharacter() == Character.toLowerCase(n.boxMap.get(goalCoordinates).getCharacter());
							
							boolean skipBox = coordinate != goalCoordinates;
							if (goalIsSolved && skipBox) {
								continue;
							}
							if (goalIsSolved && innermostEmptyGoal) {					
								// everything is good, we can continue and look at the next goal
								// OK - GOAL IS SOLVED
	
								break;					
							} else {
								// there exist a goal prior to this position or at this position which is not
								// solved so we give penalty
	
								tunnelPenalty += tunnel.getTunnelLength() - positionOfBox;
	
								int height = Math.abs(n.agentsRow[agentIndex] - coordinate.getX());
								int width = Math.abs(n.agentsCol[agentIndex] - coordinate.getY());
								lengthToIllegalBoxInTunnel = width + height;
								// PENALTY FOR A UNSOLVED GOAL - WRONG BOX IN THE GOAL
								break;
							}				
						}
						else if (positionOfGoal > positionOfBox) {
							// no unsolved goal before this position, so we check if the box is assigned
							// to the next goal
							// TODO: do this, now temporarily we put penalty to get the box out of the tunnel
							tunnelPenalty += tunnel.getTunnelLength() - positionOfBox;

							int height = Math.abs(n.agentsRow[agentIndex] - coordinate.getX());
							int width = Math.abs(n.agentsCol[agentIndex] - coordinate.getY());
							lengthToIllegalBoxInTunnel = width + height;
							// PENALTY FOR A GOAL IS MORE FAR AWAY THAN POSITION
							break;
						} else if (positionOfGoal < positionOfBox) {
		
							// box is not in goal and therefore we check if the box is assigned to this goal or not
							boolean isAssignedToGoal = boxAtAcoordinate.getAssign() == goal.getAssign();
							//boolean skipBox = coordinate != goalCoordinates;

							if (isAssignedToGoal && innermostEmptyGoal ) {	
								//System.err.println("happens");
								//tunnelPlus = Math.abs(positionOfGoal - positionOfBox);
								// the box is on the correct way to the goal
								break;
							} else {
								// the box is not assigned to this goal so therefore penalty
								tunnelPenalty += tunnel.getTunnelLength() - positionOfBox;

								int height = Math.abs(n.agentsRow[agentIndex] - coordinate.getX());
								int width = Math.abs(n.agentsCol[agentIndex] - coordinate.getY());
								lengthToIllegalBoxInTunnel = width + height;
								// PENALTY FOR A WRONG BOX IN THE TUNNEL
								break;
							}				
						}

						if (innermostEmptyGoal) {

							if (isBoxInGoal) {
								// now we know there is box in the goal, but lets check if it is the right box
								boolean goalIsSolved = goal.getCharacter() == n.boxMap.get(goalCoordinates).getCharacter();
								if (!goalIsSolved) {
									innermostEmptyGoal = false;
								}
							}		
							else {
								innermostEmptyGoal = false;
							}		
						}
					}					
				}
			}
		}

		int heuristicValue = tunnelPlus*10 + tunnelPenalty*10000 + lengthToIllegalBoxInTunnel*1000 + goalsLeft*100 + (notRightAssigned*50 + assignedDistance) + minLength + noMove;

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
