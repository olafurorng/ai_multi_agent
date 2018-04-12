package searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import searchclient.Memory;
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

				if (chr == '+') { // Wall.
					this.initialState.walls[row][col] = true;
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
					String boxString = row + "," + col;
					this.initialState.boxMap.put(boxString, Character.toString(chr));
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
