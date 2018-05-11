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



        int goalsLeft = 0;
        int notRightAssigned = 0;
        int assignedDistance = 0;
        int closestBox = Integer.MAX_VALUE;
        int minLength = Integer.MAX_VALUE;


        int totalHeuristicValue = 0;


        for (int i = 0; i < Node.NUMBER_OF_AGENTS; i++) {
            totalHeuristicValue += hPerAgent(n, i);



            totalHeuristicValue++;
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
