package searchclient;


import java.util.HashMap;
import java.util.Set;

/**
 * Created by olafurorn on 5/12/18.
 */
public class AgentCommunications {

    private HashMap<Coordinate, Box> touchedBoxesOfOtherColor = new HashMap<Coordinate, Box>();

    public enum Type {
        TOUCHING_BOX_OF_OTHER_COLOR
    }

    public AgentCommunications() {

    }

    public Box getTouchedBox(Coordinate coordinate) {
        return touchedBoxesOfOtherColor.get(coordinate);
    }

    public void removeBox(Coordinate coordinate) {
        touchedBoxesOfOtherColor.remove(coordinate);
    }

    public void onBoxWithOtherColorTouched(Coordinate coordinate, Box box) {
        touchedBoxesOfOtherColor.put(coordinate, box);
    }
}
