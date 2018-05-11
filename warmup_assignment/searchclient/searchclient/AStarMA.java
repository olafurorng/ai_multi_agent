package searchclient;

import java.util.Map;

/**
 * Created by olafurorn on 5/10/18.
 */
public class AStarMA extends Heuristic {

    public AStarMA(Node initialState) {
        super(initialState);
    }

    @Override
    public int h(Node n) {

        int totalHeuristicValue = 0;

        for (int i = 0; i < Node.NUMBER_OF_AGENTS; i++) { 
            totalHeuristicValue += hPerAgent(n, i);
        }

        return totalHeuristicValue;
    }

    @Override
    public int f(Node n) {
        return n.g() + this.h(n);
    }

    @Override
    public String toString() {
        return "A* evaluation for Multi agent";
    }
}
