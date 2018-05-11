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

	public static HashMap<Character, String> colorsMap;

	public SearchClient(BufferedReader serverMessages) throws Exception {
		// Read lines specifying colors
		String line = serverMessages.readLine();
        colorsMap = new HashMap<Character, String>();
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
		this.initialState = new Node(null);

		List<Integer> agentsRow = new ArrayList<Integer>();
		List<Integer> agentsCol = new ArrayList<Integer>();
		List<Integer> agentNumber = new ArrayList<Integer>();
		List<Color> agentsColor = new ArrayList<Color>();

		for (String serverMessageLine: lines) {
			for (int col = 0; col < serverMessageLine.length(); col++) {
				char chr = serverMessageLine.charAt(col);
				Coordinate coordinate = new Coordinate(row, col);

				if (chr == '+') { // Wall.
					Node.WALLS.add(coordinate);
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
		//WallBuilder.buildWallsSafely(this.initialState);
		WallBuilder.buildWallInEmptyCellWith3TouchingWallAnd1TouchingGoal(this.initialState);
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
			//System.err.println(leafNode.toString());
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
                	if (Node.NUMBER_OF_AGENTS == 1) {
                		// single agent
						strategy = new StrategyBestFirst(new AStarSA(client.initialState));
					} else {
                		// multi agent
						strategy = new StrategyBestFirst(new AStarMA(client.initialState));
					}

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

				String act = "[";

				for (int i = 0; i < Node.NUMBER_OF_AGENTS; i++) {
					act += n.actions[i];
					if (i != Node.NUMBER_OF_AGENTS - 1) {// if this is not the last command
						act += ",";
					}
				}
				act += "]";

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
