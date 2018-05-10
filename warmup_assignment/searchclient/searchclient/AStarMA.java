package searchclient;

/**
 * Created by olafurorn on 5/10/18.
 */
public class AStarMA extends Heuristic {

    public AStarMA(Node initialState) {
        super(initialState);
    }

    @Override
    public int h(Node n) {
        return 1;
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
