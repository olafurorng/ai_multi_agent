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
		//boolean agentFound = false;
		this.initialState = new Node(null);

		List<Integer> agentsRow = new ArrayList<Integer>();
		List<Integer> agentsCol = new ArrayList<Integer>();
		List<Integer> agentNumber = new ArrayList<Integer>();
		List<Color> agentsColor = new ArrayList<Color>();

		for (String serverMessageLine: lines) {
			for (int col = 0; col < serverMessageLine.length(); col++) {
				char chr = serverMessageLine.charAt(col);

				if (chr == '+') { // Wall.
					this.initialState.walls[row][col] = true;
				} else if ('0' <= chr && chr <= '9') { // Agent.
					agentsRow.add(row);
					agentsCol.add(col);
					agentNumber.add(Integer.parseInt(String.valueOf(chr)));
					String colorAsString = colorsMap.get(chr);
					if (colorAsString == null) {
						colorAsString = "blue"; // defaulting to blue if not defined
					}
					Color agentColor = ColorHelper.getColorFromString(colorAsString);
					agentsColor.add(agentColor);
				} else if ('A' <= chr && chr <= 'Z') { // Box.
					this.initialState.boxes[row][col] = chr;
				} else if ('a' <= chr && chr <= 'z') { // Goal.
					this.initialState.goals[row][col] = chr;
				} else if (chr == ' ') {
					// Free space.
				} else {
					System.err.println("Error, read invalid level character: " + (int) chr);
					System.exit(1);
				}
			}

			row++;
		}

		Node.setNumberOfAgents(agentsCol.size());
		initialState.initializeActionsForInitialState();

		initialState.agentsRow = new int[agentsRow.size()];
		initialState.agentsCol = new int[agentsCol.size()];
		Node.agentsColor = new Color[agentsColor.size()];
		for (int i = 0; i < agentsRow.size(); i++) {
			initialState.agentsRow[agentNumber.get(i)] = agentsRow.get(i);
			initialState.agentsCol[agentNumber.get(i)] = agentsCol.get(i);
			Node.agentsColor[agentNumber.get(i)] = agentsColor.get(i);
		}

		// Lets relax the problem by adding edges / walls
		addingWalls(this.initialState);
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
				if (Node.walls[i][j]) {
					break;
				} else {
					Node.walls[i][j] = true;
				}
			}
		}
		// then add from the right side
		for (int i = 0; i < Node.MAX_ROW; i++) {
			for (int j = Node.MAX_COL - 1; j >= 0 ; j--) {
				if (Node.walls[i][j]) {
					break;
				} else {
					Node.walls[i][j] = true;
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

							boolean wallTop = Node.walls[i - 1][ j];
							boolean wallTopRight = Node.walls[i - 1][ j + 1];
							boolean wallRight = Node.walls[i][ j + 1];
							boolean wallBottomRight = Node.walls[i + 1][ j + 1];
							boolean wallBottom = Node.walls[i + 1][ j];
							boolean wallLeftBottom = Node.walls[i + 1][ j - 1];
							boolean wallLeft = Node.walls[i][ j - 1];
							boolean wallLeftTop = Node.walls[i - 1][ j - 1];

							/*
								Wall is said to be touching a cell, if the wall is on the left, right, top or bottom.
								Wall is said to be nearby a cell, if the wall is on the top-right, right-bottom, bottom-left or left-top
							 */
							int numberOfWallsTouching = 0;

							if (wallTop) numberOfWallsTouching++;
							if (wallRight) numberOfWallsTouching++;
							if (wallBottom) numberOfWallsTouching++;
							if (wallLeft) numberOfWallsTouching++;


							// 4 WALLS TOUCHING
							if (numberOfWallsTouching == 4) {
								// we can safely add a wall
								Node.walls[i][j] = true;
								numberOfNewBoxesThisRound++;
							}

							// 3 WALLS TOUCHING
							if (numberOfWallsTouching == 3) {
								// we can safely add a wall
								Node.walls[i][j] = true;
								numberOfNewBoxesThisRound++;
							}

							// 2 WALLS TOUCHING
							if (numberOfWallsTouching == 2) {
								// we exclude if the cell is part of a "tunnel", i.e.
								// - if the touching walls are opposite, i.e. walltop and wallbottom or wallright and wallleft
								// - if the opposite nearby cell to the two touching walls, is a wall

								// walls are top and left
								if (wallTop && wallLeft && !wallBottomRight) {
									Node.walls[i][j] = true;
									numberOfNewBoxesThisRound++;
								}

								// walls are top and right
								if (wallTop && wallRight && !wallLeftBottom) {
									Node.walls[i][j] = true;
									numberOfNewBoxesThisRound++;
								}

								// walls are right and bottom
								if (wallRight && wallBottom && !wallLeftTop) {
									Node.walls[i][j] = true;
									numberOfNewBoxesThisRound++;
								}

								// walls are bottom and left
								if (wallBottom && wallLeft && !wallTopRight) {
									Node.walls[i][j] = true;
									numberOfNewBoxesThisRound++;
								}
							}

							// 1 WALL TOUCHING
							if (numberOfWallsTouching == 1) {
								// we exclude if the cell is part of a "tunnel", i.e.
								// - if opposite nearby cell to the one touching wall, is a wall
								if (wallTop && !wallBottomRight && !wallLeftBottom) {
									Node.walls[i][j] = true;
									numberOfNewBoxesThisRound++;
								}

								if (wallRight && !wallLeftTop && !wallLeftBottom) {
									Node.walls[i][j] = true;
									numberOfNewBoxesThisRound++;
								}

								if (wallBottom && !wallTopRight && !wallLeftTop) {
									Node.walls[i][j] = true;
									numberOfNewBoxesThisRound++;
								}

								if (wallLeft && !wallBottomRight && !wallTopRight) {
									Node.walls[i][j] = true;
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
            if (iterations == 10000) {
				System.err.println(strategy.searchStatus());
				iterations = 0;
			}

			if (strategy.frontierIsEmpty()) {
				return null;
			}

			Node leafNode = strategy.getAndRemoveLeaf();

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

			System.err.println("SOLUTION ####");
			for (Node n : solution) {
				System.err.println("n: " + n);

				String act = "[";

				for (int i = 0; i < Node.NUMBER_OF_AGENTS; i++) {
					act += n.actions[i];
					if (i != Node.NUMBER_OF_AGENTS - 1) {// if this is not the last command
						act += ",";
					}
				}
				act += "]";
				System.err.println("ACT: " + act);
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
