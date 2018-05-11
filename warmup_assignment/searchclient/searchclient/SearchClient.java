package searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import searchclient.Strategy.*;
import searchclient.Heuristic.*;
import searchclient.ColorHelper.*;

public class SearchClient {
	public Node initialState;

	public SearchClient(BufferedReader serverMessages) throws Exception {
		// Read lines specifying colors
		String line = serverMessages.readLine();
        HashMap<Character, String> colorsMap = new HashMap<Character, String>();
		while (line.matches("^[a-z]+:\\s*[0-9A-Z](\\s*,\\s*[0-9A-Z])*\\s*$")) {
            String[] colorParts = line.split("\\:");
            String color = colorParts[0];
            String[] agentsAndBoxes = colorParts[1].trim().split("\\,");
            for(String agentOrBox : agentsAndBoxes){
                colorsMap.put(agentOrBox.charAt(0), color);
            }

            line = serverMessages.readLine();
		}

		System.err.println(colorsMap);

		/**
		 * Lets read through the BufferReader ('serverMessages') and store it in
		 * a variable so we can iterate through the lines twice to:
		 *		1. Find the size of the level, width and height (columns and rows)
		 *         to be able to set the size of the level in `searchclient.Node` (NEW CODE)
		 *		2. Create the initial state Node (PREVIOUS EXISTING CODE)
		 */
		ArrayList<String> lines = new ArrayList<String>();
		int numberOfCol = 0;
		int numberOfRows = 0;

		while (!line.equals("")) {
			lines.add(line);

			if (line.length() > numberOfCol) {
				numberOfCol = line.length();
			}
			numberOfRows++;

			line = serverMessages.readLine();
		}

		Node.setLevelSize(numberOfRows, numberOfCol);


		// Creating the initial state Node (Previous existing code)
		int row = 0;
		boolean agentFound = false;
		this.initialState = new Node(null);

		for (String serverMessageLine: lines) {
			for (int col = 0; col < serverMessageLine.length(); col++) {
				char chr = serverMessageLine.charAt(col);
				Coordinate coordinate = new Coordinate(row, col);

				if (chr == '+') { // Wall.
					Node.WALLS.add(coordinate);
				} else if ('0' <= chr && chr <= '9') { // Agent.
					if (agentFound) {
						System.err.println("Error, not a single agent level");
						System.err.println("Also remember to pass in different agent colors when we implement this");
						System.exit(1);
					}
					agentFound = true;
					this.initialState.agentRow = row;
					this.initialState.agentCol = col;
					if (colorsMap.size() > 0) { // colors are defined in this level
                        Color agentColor = ColorHelper.getColorFromString(colorsMap.get('0')); // FIXME: CHANGE THIS WHEN WE ADD MORE AGENTS
                        Node.agentColor = agentColor;
                    }
				} else if ('A' <= chr && chr <= 'Z') { // Box.
					Box box = new Box(chr, 0);
					this.initialState.boxMap.put(coordinate, box);
				} else if ('a' <= chr && chr <= 'z') { // Goal.
					Goals goal = new Goals(chr, false, 0);
					Node.GOALS.put(coordinate, goal);
				} else if (chr == ' ') {
					// Free space.
				} else {
					System.err.println("Error, read invalid level character: " + (int) chr);
					System.exit(1);
				}
			}

			row++;
		}

		// Lets relax the problem by adding edges / walls
		// addingWalls(this.initialState);
	}

	/**
	 * This is a type of Relaxed problem, more specific: Adding edges relaxed problem.
	 * Walls or edges are added to decrease the size of the level, but has rules to
	 * prevent that the level won't become unsolvable
	 *
	 * Cons:
	 * 		- Length of the solution can be longer, e.g. in SAsoko3_48.lvl
	 * 		- Level which are not squared, have many free cells, and therefore can
	 * 		  become unsolvable.
	 */
	private void addingWalls(Node initialState) {
		System.err.println("----------------------------------------");
		System.err.println("Walls before");
		System.err.println(initialState.toString());

		// we will start adding walls in free cells which are outside the level
		// lets start with adding from the left side

		for (int i = 0; i < Node.MAX_ROW; i++) {
			for (int j = 0; j < Node.MAX_COL; j++) {
				if (Node.WALLS.contains(new Coordinate(i, j))) {
					break;
				} else {
					Node.WALLS.add(new Coordinate(i,j));
				}
			}
		}
		// then add from the right side
		for (int i = 0; i < Node.MAX_ROW; i++) {
			for (int j = Node.MAX_COL - 1; j >= 0 ; j--) {
				if (Node.WALLS.contains(new Coordinate(i,j))) {
					break;
				} else {
					Node.WALLS.add(new Coordinate(i,j));
				}
			}
		}

		// count how many free cells compared to number of agents+boxes
		int numberOfNewBoxesThisRound = 1000; // initialized as some number above 0
		int iterations = 0;

		while (numberOfNewBoxesThisRound > 0) {
			numberOfNewBoxesThisRound = 0;
			for (int i = 0; i < Node.MAX_ROW; i++) {
				for (int j = 0; j < Node.MAX_COL; j++) {
					if (initialState.cellIsFreeAndNoGoalOrAgent(i,j)) {
						// now the cell is free, so we check if there is no
						// goal, box or agent around that cell
						boolean topFree = initialState.cellIsFreeOfGoalBoxAndAgent(i - 1, j);
						boolean topRightFree = initialState.cellIsFreeOfGoalBoxAndAgent(i - 1, j + 1);
						boolean rightFree = initialState.cellIsFreeOfGoalBoxAndAgent(i, j + 1);
						boolean bottomRighFree = initialState.cellIsFreeOfGoalBoxAndAgent(i + 1, j + 1);
						boolean bottomFree = initialState.cellIsFreeOfGoalBoxAndAgent(i + 1, j);
						boolean leftBottomFree = initialState.cellIsFreeOfGoalBoxAndAgent(i + 1, j - 1);
						boolean leftFree = initialState.cellIsFreeOfGoalBoxAndAgent(i, j - 1);
						boolean leftTopFree = initialState.cellIsFreeOfGoalBoxAndAgent(i - 1, j - 1);

						if (topFree && topRightFree && rightFree && bottomRighFree && bottomFree && leftBottomFree && leftFree && leftTopFree) {
							// now there is not goal, box or agent near this cell

							boolean wallTop = Node.WALLS.contains(new Coordinate(i-1, j));
							boolean wallTopRight = Node.WALLS.contains(new Coordinate(i-1, j+1));
							boolean wallRight = Node.WALLS.contains(new Coordinate(i, j+1));
							boolean wallBottomRight = Node.WALLS.contains(new Coordinate(i + 1, j+1));
							boolean wallBottom = Node.WALLS.contains(new Coordinate(i + 1, j));
							boolean wallLeftBottom = Node.WALLS.contains(new Coordinate(i + 1, j - 1));
							boolean wallLeft = Node.WALLS.contains(new Coordinate(i,j - 1));
							boolean wallLeftTop = Node.WALLS.contains(new Coordinate(i-1, j-1));

							/*
								Wall is said to be touching a cell, if the wall is on the left, right, top or bottom.
								Wall is said to be nearby a cell, if the wall is on the top-right, right-bottom, bottom-left or left-top
							 */
							int numberOfWallsTouching = 0;

							if (wallTop) numberOfWallsTouching++;
							if (wallRight) numberOfWallsTouching++;
							if (wallBottom) numberOfWallsTouching++;
							if (wallLeft) numberOfWallsTouching++;

							Coordinate coordinate = new Coordinate(i,j);


							// 4 or 3 WALLS TOUCHING
							if (numberOfWallsTouching == 4 || numberOfWallsTouching == 3) {
								// we can safely add a wall
								Node.WALLS.add(coordinate);
								numberOfNewBoxesThisRound++;
							}

							// 2 WALLS TOUCHING
							if (numberOfWallsTouching == 2) {
								// we exclude if the cell is part of a "tunnel", i.e.
								// - if the touching walls are opposite, i.e. walltop and wallbottom or wallright and wallleft
								// - if the opposite nearby cell to the two touching walls, is a wall

								// WALLS are top and left
								if (wallTop && wallLeft && !wallBottomRight) {
									Node.WALLS.add(coordinate);
									numberOfNewBoxesThisRound++;
								}

								// walls are top and right
								if (wallTop && wallRight && !wallLeftBottom) {
									Node.WALLS.add(coordinate);
									numberOfNewBoxesThisRound++;
								}

								// walls are right and bottom
								if (wallRight && wallBottom && !wallLeftTop) {
									Node.WALLS.add(coordinate);
									numberOfNewBoxesThisRound++;
								}

								// walls are bottom and left
								if (wallBottom && wallLeft && !wallTopRight) {
									Node.WALLS.add(coordinate);
									numberOfNewBoxesThisRound++;
								}
							}

							// 1 WALL TOUCHING
							if (numberOfWallsTouching == 1) {
								// we exclude if the cell is part of a "tunnel", i.e.
								// - if opposite nearby cell to the one touching wall, is a wall
								if (wallTop && !wallBottomRight && !wallLeftBottom) {
									Node.WALLS.add(coordinate);
									numberOfNewBoxesThisRound++;
								}

								if (wallRight && !wallLeftTop && !wallLeftBottom) {
									Node.WALLS.add(coordinate);
									numberOfNewBoxesThisRound++;
								}

								if (wallBottom && !wallTopRight && !wallLeftTop) {
									Node.WALLS.add(coordinate);
									numberOfNewBoxesThisRound++;
								}

								if (wallLeft && !wallBottomRight && !wallTopRight) {
									Node.WALLS.add(coordinate);
									numberOfNewBoxesThisRound++;
								}
							}

						}
					}
				}
			}

			iterations++;
		}
		System.err.println("----------------------------------------");
		System.err.println("Adding walls done, iterations: " + iterations);
		System.err.println(initialState.toString());
		System.err.println("----------------------------------------");
	}

	public LinkedList<Node> Search(Strategy strategy) throws IOException {
		System.err.format("Search starting with strategy %s.\n", strategy.toString());
		strategy.addToFrontier(this.initialState);

		int iterations = 0;
		while (true) {

			if (strategy.frontierIsEmpty()) {
				return null;
			}

			Node leafNode = strategy.getAndRemoveLeaf();

            if (iterations == 10000) {
				System.err.println(strategy.searchStatus());
				int counter = 0;
				//int highest = 0;
				//Goals highestGoal = null;

				for (Map.Entry<Coordinate, Goals> entry : Node.GOALS.entrySet()) {
					Goals currentGoal = entry.getValue();
					int goalRow = entry.getKey().getX();
					int goalCol = entry.getKey().getY();

					Box currentBox = leafNode.boxMap.get(new Coordinate(goalRow, goalCol));

					/*if(highest < currentGoal.getFinished()) {
						highest = currentGoal.getFinished();
						highestGoal = currentGoal;
					}*/
					currentGoal.setPriority(1);
					// If goal is not finished
					if ((currentBox == null) || (currentBox != null && Character.toLowerCase(currentBox.getCharacter()) != currentGoal.getCharacter())) {
						counter++;
					}
					//System.err.println("current goal: " + currentGoal.getCharacter() + ": " + currentGoal.getFinished());

				}
				//System.err.println("highestGoal goal: " + highestGoal.getCharacter());
				//highestGoal.setPriority(100);

				System.err.println("Unfinish goals: " + counter);
				System.err.println(leafNode.toString());
				iterations = 0;
			}

			if (leafNode.isGoalState()) {
				return leafNode.extractPlan();
			}

			strategy.addToExplored(leafNode);
			for (Node n : leafNode.getExpandedNodes()) { // The list of expanded nodes is shuffled randomly; see Node.java.
				
				if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
					strategy.addToFrontier(n);
				}
			}

			iterations++;
		}
	}

	public static void main(String[] args) throws Exception {
		BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in));

		// Use stderr to print to console
		System.err.println("SearchClient initializing. I am sending this using the error output stream.");

		// Read level and create the initial state of the problem
		SearchClient client = new SearchClient(serverMessages);

        Strategy strategy;
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "-bfs":
                    strategy = new StrategyBFS();
                    break;
                case "-dfs":
                    strategy = new StrategyDFS();
                    break;
                case "-astar":
                    strategy = new StrategyBestFirst(new AStar(client.initialState));
                    break;
                case "-wastar":
                    // You're welcome to test WA* out with different values, but for the report you must at least indicate benchmarks for W = 5.
                    strategy = new StrategyBestFirst(new WeightedAStar(client.initialState, 5));
                    break;
                case "-greedy":
                    strategy = new StrategyBestFirst(new Greedy(client.initialState));
                    break;
                default:
                    strategy = new StrategyBFS();
                    System.err.println("Defaulting to BFS search. Use arguments -bfs, -dfs, -astar, -wastar, or -greedy to set the search strategy.");
            }
        } else {
            strategy = new StrategyBFS();
            System.err.println("Defaulting to BFS search. Use arguments -bfs, -dfs, -astar, -wastar, or -greedy to set the search strategy.");
        }

		LinkedList<Node> solution;
		try {
			solution = client.Search(strategy);
		} catch (OutOfMemoryError ex) {
			System.err.println("Maximum memory usage exceeded.");
			solution = null;
		}

		if (solution == null) {
			System.err.println(strategy.searchStatus());
			System.err.println("Unable to solve level.");
			System.exit(0);
		} else {
			System.err.println("\nSummary for " + strategy.toString());
			System.err.println("Found solution of length " + solution.size());
			System.err.println(strategy.searchStatus());

			for (Node n : solution) {
				String act = n.action.toString();
				System.out.println(act);
				String response = serverMessages.readLine();
				if (response.contains("false")) {
					System.err.format("Server responsed with %s to the inapplicable action: %s\n", response, act);
					System.err.format("%s was attempted in \n%s\n", act, n.toString());
					break;
				}
			}
		}
	}
}
